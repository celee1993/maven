package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.*;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import com.leyou.vo.SpuVO;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 分页查询SPU属性
     *
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @Override
    public PageResult<SpuVO> findSpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page, rows);

        //过滤

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            //过滤条件
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        //解析分页助手
        PageInfo<Spu> info = new PageInfo<>(list);
        long total = info.getTotal();//总数
        Integer pages = info.getPages();//总页数

        //将list里面的对象Spu封装为SpuVo对象 同时解析 类名和品牌名
        List<SpuVO> list_vo = parseToVo(list);
        return new PageResult<>(total, pages.longValue(), list_vo);
    }

     /**
     * 新增商品
     * @param spu
     */
    @Transactional
    @Override
    public void saveGoods(Spu spu) {
        //封装SPU表信息
        Date date = new Date();
        spu.setCreateTime(date);
        spu.setLastUpdateTime(date);
        spu.setValid(true);
        spu.setSaleable(true);
        int insert = spuMapper.insert(spu);
        if (insert != 1) {
            //新增失败
            throw new LyException(ExceptionEnum.SPU_SAVE_ERROR);
        }
        //添加SPU_DETAIL表
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        int insert1 = spuDetailMapper.insert(spuDetail);
        if (insert1 != 1) {
            //新增失败
            throw new LyException(ExceptionEnum.SPU_DETAIL_SAVE_ERROR);
        }
        //封装SKU信息以及库存表信息
        saveSkuAndStock(spu);

        //发送MQ消息
        sendMqMessage("insert",spu.getId());
    }

    /**
     * 根据spuId查询 商品详情SPU_DETAIL
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail findDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail==null) {
            //查询失败
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据spuId查询SKU集合
     * @param spuId
     * @return
     */
    @Override
    public List<Sku> findSkuListBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if (skus==null) {
            //查询失败
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        //查询库存 封装进sku
        //先得到skuID的数组集合然后进行批量查询Stock
        List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            throw new LyException(ExceptionEnum.STOCK_NOT_FOUND);
        }
        //将库存批stocks量封装进skus
        loadStock(skuIds, skus);
        return skus;

    }

    /**
     * 将库存批stocks量封装进skus
     * @param skuIds
     * @param skus
     */
    private void loadStock(List<Long> skuIds, List<Sku> skus) {
        List<Stock> stocks = stockMapper.selectByIdList(skuIds);
        //将stocks由list转换为Map(skuId,stock)
        Map<Long, Integer> stock_map = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
//      将Map封装进skus
        skus.stream().forEach(s->s.setStock(stock_map.get(s.getId()).longValue()));
    }

    /**
     * 更新商品
     * @param spu
     */
    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new LyException(ExceptionEnum.GOODS_ID_CAN_NOT_BE_NULL);
        }
        //查询sku判断存在不存在对应的sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //根据skuId删除stock
            List<Long> skuIds = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(skuIds);
        }
        //spu修改
        spu.setLastUpdateTime(new Date());//更新修改时间

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count!=1) {
            //新增失败
            throw new LyException(ExceptionEnum.SPU_UPDATE_ERROR);
        }
        //detail修改
        SpuDetail spuDetail = spu.getSpuDetail();
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count!=1) {
            //新增失败
            throw new LyException(ExceptionEnum.SPU_DETAIL_UPDATE_ERROR);
        }
        //添加sku和stock
        saveSkuAndStock(spu);
        //发送mq消息
        sendMqMessage("update",spu.getId());
    }

    /**
     * 根据spuId查询单个Spu
     * @param spuId
     * @return
     */
    @Override
    public Spu findSpuById(Long spuId) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu == null) {
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        //查询sku
        List<Sku> skus = findSkuListBySpuId(spuId);
        //查询detail
        SpuDetail detail = findDetailBySpuId(spuId);
        spu.setSkus(skus);
        spu.setSpuDetail(detail);
        return spu;
    }



    /**
     * 根据skuId集合批量查询sku集合
     * @param ids
     * @return
     */
    @Override
    public List<Sku> findSkuListByIds(List<Long> ids) {
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        //将库存批stocks量封装进skus
        loadStock(ids,skuList);
        return skuList;
    }

    /**
     * 根据skuId查询sku信息
     * @param id
     * @return
     */
    @Override
    public Sku findSkuById(Long id) {
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if (sku == null) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        //封装库存
        Stock stock = stockMapper.selectByPrimaryKey(id);
        sku.setStock(Long.valueOf(String.valueOf(stock.getStock())));
        return sku;
    }


    /**
     * 减库存
     * @param carts
     */
    @Override
    @Transactional
    public void decreaseStock(List<CartDto> carts) {
        for (CartDto cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }


    /**
     * 工具方法
     * 对象Spu封装为SpuVo对象 同时解析 类名和品牌名
     * @param list
     * @return
     */
    private List<SpuVO> parseToVo(List<Spu> list) {
        List<SpuVO> list_vo = new ArrayList<>();
        for (Spu spu : list) {
            //处理分类名称
            List<Category> categories = categoryService.findByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            //拼接三级类名
            List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
            SpuVO sv = new SpuVO();
//            categories.stream().map(category -> category.getName()).collect(Collectors.toList());
            sv.setCname(StringUtils.join(names, "/"));
            sv.setId(spu.getId());
            sv.setTitle(spu.getTitle());
            sv.setSubTitle(spu.getSubTitle());
            sv.setCid2(spu.getCid2());
            sv.setCid1(spu.getCid1());
            sv.setCreateTime(spu.getCreateTime());
            sv.setBname(brandService.findBrandById(spu.getBrandId()).getName());
            sv.setBrandId(spu.getBrandId());
            sv.setCid3(spu.getCid3());
            list_vo.add(sv);
        }
        return list_vo;
    }

    /**
     * 工具方法
     * 根据spu封装sku和stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        List<Sku> skus = spu.getSkus();
        List<Stock> stocks = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setLastUpdateTime(spu.getLastUpdateTime());
            sku.setCreateTime(spu.getCreateTime());
            skuMapper.insert(sku);
            Stock stock = new Stock();
            //先封装stock集合
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock().intValue());
            stocks.add(stock);
        }
        //批量添加stock表
        int count = stockMapper.insertList(stocks);
        if (count!=stocks.size()) {
            //新增失败
            throw new LyException(ExceptionEnum.STOCK_SAVE_ERROR);
        }
    }

    /**
     * 商品增删改查发送MQ消息
     * @param type
     * @param
     */
    private void sendMqMessage(String type,Long spuId) {
        try {
            amqpTemplate.convertAndSend("item."+type,spuId);
        } catch (AmqpException e) {
            log.error("商品发送消息异常商品ID:["+spuId+"],消息类型：["+type+"]",e);
        }
    }
}

