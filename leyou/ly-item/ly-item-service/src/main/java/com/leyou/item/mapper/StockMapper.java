package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.domain.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 减库存 乐观锁效果
     * @param id
     * @param num
     * @return
     */
    @Update("update tb_stock SET stock = stock - #{num} WHERE sku_id = #{id} AND stock > #{num}")
    int decreaseStock(@Param("id")Long id,@Param("num") Integer num);
}
