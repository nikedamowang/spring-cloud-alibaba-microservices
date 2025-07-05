package com.cloudDemo.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudDemo.orderservice.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
