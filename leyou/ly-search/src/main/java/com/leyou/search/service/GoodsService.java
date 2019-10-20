package com.leyou.search.service;



import com.leyou.common.vo.PageResult;
import com.leyou.domain.Category;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.vo.SpuVO;

import java.util.List;


public interface GoodsService {

    Goods buildGoods(SpuVO sv);

    PageResult<Goods> search(SearchRequest searchRequest);

    List<Category> findCategoriesByCid3(Long id);

    void createOrUpdateIndex(Long spuId);

    void deleteIndex(Long spuId);
}
