package com.cloudDemo.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudDemo.orderservice.entity.ProductInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductInventoryMapper extends BaseMapper<ProductInventory> {

    /**
     * 使用乐观锁更新库存
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @param version   当前版本号
     * @return 更新行数
     */
    @Update("UPDATE product_inventory SET " +
            "available_stock = available_stock - #{quantity}, " +
            "reserved_stock = reserved_stock + #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId} AND version = #{version} " +
            "AND available_stock >= #{quantity}")
    int reserveStockWithVersion(@Param("productId") String productId,
                                @Param("quantity") Integer quantity,
                                @Param("version") Integer version);

    /**
     * 确认库存扣减（从预扣转为已售）
     */
    @Update("UPDATE product_inventory SET " +
            "reserved_stock = reserved_stock - #{quantity}, " +
            "sold_stock = sold_stock + #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId} AND version = #{version}")
    int confirmStockReduction(@Param("productId") String productId,
                              @Param("quantity") Integer quantity,
                              @Param("version") Integer version);

    /**
     * 释放预扣库存
     */
    @Update("UPDATE product_inventory SET " +
            "available_stock = available_stock + #{quantity}, " +
            "reserved_stock = reserved_stock - #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId} AND version = #{version}")
    int releaseReservedStock(@Param("productId") String productId,
                             @Param("quantity") Integer quantity,
                             @Param("version") Integer version);
}
