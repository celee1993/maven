package com.leyou.item.service;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.Sku;
import com.leyou.domain.Spu;
import com.leyou.domain.SpuDetail;
import com.leyou.vo.SpuVO;

import java.util.List;

public interface GoodsService {
    PageResult<SpuVO> findSpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(Spu spu);

    SpuDetail findDetailBySpuId(Long spuId);

    List<Sku> findSkuListBySpuId(Long spuId);

    void updateGoods(Spu spu);

    Spu findSpuById(Long spuId);

    List<Sku> findSkuListByIds(List<Long> ids);

    Sku findSkuById(Long id);

    void decreaseStock(List<CartDto> carts);
}
