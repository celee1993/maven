package com.leyou.search.respository;

import com.leyou.common.vo.PageResult;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.GoodsService;
import com.leyou.vo.SpuVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsService goodsService;
    /**
     * 创建索引库
     */
    @Test
    public void CreateIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    /**
     * 查询数据库所有的SPU构建成商品 存入索引库
     */
    @Test
    public void loadGoods() {
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            //查询spu
            PageResult<SpuVO> result = goodsClient.findSpuByPage(page, rows, true, null);
            //当前页的数据
            List<SpuVO> vos = result.getItems();
            //SPUVO对象批量构建Goods
            List<Goods> goods = vos.stream().map(goodsService::buildGoods).collect(Collectors.toList());
//            List<Goods> goods1 = vos.stream().map(vo -> goodsService.buildGoods(vo)).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(goods);
            size = goods.size();
        } while (size == 100);
    }
}