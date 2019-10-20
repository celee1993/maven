package com.leyou.search.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.respository.GoodsRepository;
import com.leyou.search.service.GoodsService;
import com.leyou.vo.SpuVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;
    /**
     * 添加索引库封装goods
     * @param sv
     * @return
     */
    public Goods buildGoods(SpuVO sv) {
        //查询分类名称
//        List<Category> categories = categoryClient.findCategoryListByIds(
//                Arrays.asList(sv.getCid1(), sv.getCid2(), sv.getCid3()));
//        if (CollectionUtils.isEmpty(categories)) {
//            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
//        }
//        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
//        //查询品牌
//        Brand brand = brandClient.findBrandsById(sv.getBrandId());
//        String bname = brand.getName();
//        if (bname == null) {
//            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
//        }
           //搜索字段
//        String all = sv.getTitle() + StringUtils.join(categoryNames, " ")+bname;
        String all = sv.getTitle() + sv.getCname() + sv.getBname();
        //查询skus
        List<Sku> skus = goodsClient.findSku(sv.getId());
        //对skus进行处理 使返回结果不要太多 同时处理价格
        List<Map<String, Object>> skus_new = new ArrayList<>();
        Set<Long> prices = new HashSet<>();
        for (Sku sku : skus) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("image", StringUtils.isBlank(sku.getImages())?" ":StringUtils.substringBefore(sku.getImages(),","));
            map.put("price", sku.getPrice());
            skus_new.add(map);
            prices.add(sku.getPrice());
        }
        //得到价格集合
//        Set<Long> peices = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());

        //查询商品规格参数
        List<SpecParam> params = specificationClient.findParamsList(null, sv.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.GROUP_PARAM_NOT_FOUND);
        }
        //规格详情
        SpuDetail detail = goodsClient.findDetail(sv.getId());
        //获取通用参数
        Map<Long, String> generic = JsonUtils.parseMap(detail.getGenericSpec(), Long.class, String.class);
        //获取特有参数
        Map<Long, List<String>> special = JsonUtils.nativeRead(detail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>(){});
        //组合可搜索的规格参数
        Map<String, Object> specs = new HashMap<>();//key是规格参数名字，value是规格详情的值，包括通用参数和特有参数
        for (SpecParam param : params) {
            String key = param.getName();
//            判断通用参数还是特有参数
            Object value;
            //判断是通用参数
            if (param.getGeneric()) {
                //共有参数
                value = generic.get(param.getId());
                if (param.getNumeric()) {
                    //判断是否为数字类型 处理成段
                    value = chooseSegment(value.toString(), param);
                }
            }else{
                value = special.get(param.getId());
            }
            //填充key和value
            specs.put(key, value);
        }

        Goods goods = new Goods();
        goods.setBrandId(sv.getBrandId());
        goods.setCid1(sv.getCid1());
        goods.setCid2(sv.getCid2());
        goods.setCid3(sv.getCid3());
        goods.setCreateTime(sv.getCreateTime());
        goods.setSubTitle(sv.getSubTitle());
        goods.setId(sv.getId());
        goods.setAll(all);//搜索字段 包含标题、分类、品牌、规格等
        goods.setPrice(prices);//所有sku的价格集合
        goods.setSkus(JsonUtils.serialize(skus_new));//所有sku集合的json格式
        goods.setSpecs(specs);//所有可搜索的规格参数
        return goods;
    }

    /**
     * 根据条件搜索索引库
     * @return
     * @param searchRequest
     */
    @Override
    public PageResult<Goods> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //1.分页查询
        queryBuilder.withPageable(PageRequest.of(page-1, size));
        //2.条件查询
        QueryBuilder basicQuery=null;//基础查询条件
        if (StringUtils.isNotBlank(key)) {
            basicQuery = buildBasicQuery(searchRequest);
        }
        queryBuilder.withQuery(basicQuery);
        //3.结果过滤掉不必要的字段
        String[] strs = {"id","subTitle","skus","specs"};
        queryBuilder.withSourceFilter(new FetchSourceFilter(strs,null));
        //4.聚合分类和品牌
        //4.1聚合分类
        String categoryAgg = "category";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("cid3"));
        //4.2聚合品牌
        String brandAgg = "brand";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
        //5.查询
        AggregatedPage<Goods> search = template.queryForPage(queryBuilder.build(), Goods.class);
        //6.解析结果
        // 6.1解析分页结果
        List<Goods> content = search.getContent();
        if (CollectionUtils.isEmpty(content)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        long totalElements = search.getTotalElements();
        int totalPages = search.getTotalPages();
        List<Goods> goodsList = search.getContent();
        //6.2解析聚合结果
        Aggregations aggs = search.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAgg));
        List<Brand> brands =parseBrandAgg(aggs.get(brandAgg));

        //7.完成规格参数聚合
        List<Map<String, Object>> specs = null;
        //7.1判断商品分类存在 并且数量为1 再进行聚合参数
        if (!CollectionUtils.isEmpty(categories)&&categories.size() == 1) {
            //符合条件后在基础查询结果的基础上进行聚合 并且在分类ID基础上
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(totalElements,Integer.toUnsignedLong(totalPages),goodsList,categories,brands,specs);
    }

    /**
     * 通过cid3查询多级分类集合
     * @param id
     * @return
     */
    @Override
    public List<Category> findCategoriesByCid3(Long id) {
        return categoryClient.findCategoriesByCid3(id);
    }

    /**
     * 接收到商品修改或新增服务传来的消息对索引库进行修改或新增
     * @param spuId
     */
    @Override
    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.findSpuById(spuId);
//        构建goods
        SpuVO spuVO = parseVo(spu);
        Goods goods = buildGoods(spuVO);
        //存入索引库
        goodsRepository.save(goods);
    }

    /**
     * 接收到商品删除服务传来的消息对索引库进行删除
     * @param spuId
     */
    @Override
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }

    /**
     * 构建过滤项条件
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //过滤条件
        Map<String, String> filter = searchRequest.getFilter();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
               key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
//        for (String key : filter.keySet()) {
//            String value = filter.get(key);
//            //添加所有过滤条件
//            //判断key是不是分类或者品牌
//            if (!"cid3".equals(key) && !"brandId".equals(key)) {
//                key = "specs." + key + ".keyword";
//            }
//            boolQuery.filter(QueryBuilders.termQuery(key,value));
//        }
        queryBuilder.filter(boolQueryBuilder);
        return queryBuilder;
    }

    /**
     * 创建聚合
     * @param id
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> buildSpecificationAgg(Long id,QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        //1.查询可以聚合的规格参数
        List<SpecParam> paramsList = specificationClient.findParamsList(null, id, true);
        //2.聚合
        //2.1获取原生条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.2将基础查询条件传入
        queryBuilder.withQuery(basicQuery);
        //2.3开始聚合
        for (SpecParam param : paramsList) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        //3.返回结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //3.1解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : paramsList) {
            String name = param.getName();
            StringTerms terms= aggs.get(name);
            //获取到属性值集合
            List<String> stringList = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", stringList);
            specs.add(map);
        }


        return specs;
    }

    /**
     * 解析出品牌对象集合
     * @param terms
     * @return
     */
    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            return brandClient.findBrandsByIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析出分类对象集合
     * @param terms
     * @return
     */
    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            return categoryClient.findCategoryListByIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理数据
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 将spu转化为Spuvo
     * @param spu
     * @return
     */
    private SpuVO parseVo(Spu spu) {
        SpuVO spuVO = new SpuVO();
        spuVO.setId(spu.getId());
        spuVO.setBrandId(spu.getBrandId());
        spuVO.setBname(brandClient.findBrandsById(spu.getBrandId()).getName());
        List<String> categories = categoryClient.findCategoriesByCid3(spu.getCid3()).stream().map(Category::getName).collect(Collectors.toList());
        spuVO.setCname(StringUtils.join(categories,"/"));
        spuVO.setSubTitle(spu.getSubTitle());
        spuVO.setCreateTime(spu.getCreateTime());
        spuVO.setCid1(spu.getCid1());
        spuVO.setCid3(spu.getCid3());
        spuVO.setCid2(spu.getCid2());
        spuVO.setTitle(spu.getTitle());
        return spuVO;
    }
}
