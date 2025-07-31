package com.cloudDemo.order.service.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.cloudDemo.order.dto.excel.OrderExcelExportDto;
import com.cloudDemo.orderservice.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单Excel导出服务
 * 提供高性能、企业级的Excel数据导出功能
 */
@Slf4j
@Service
public class OrderExcelExportService {

    /**
     * 导出订单数据到Excel
     *
     * @param queryParams 查询参数
     * @return Excel文件字节数组
     */
    public byte[] exportOrdersToExcel(Map<String, Object> queryParams) {
        try {
            log.info("开始导出订单Excel数据，查询参数: {}", queryParams);

            // 查询订单数据（使用模拟数据）
            List<Orders> ordersList = queryOrdersByParams(queryParams);

            if (ordersList.isEmpty()) {
                log.warn("没有找到符合条件的订单数据");
                return generateEmptyExcel();
            }

            // 转换为导出DTO
            List<OrderExcelExportDto> exportData = convertToExportDto(ordersList);

            // 生成Excel文件
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream, OrderExcelExportDto.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("订单数据")
                    .doWrite(exportData);

            byte[] excelBytes = outputStream.toByteArray();

            log.info("订单Excel导出完成，数据行数: {}, 文件大小: {} bytes",
                    exportData.size(), excelBytes.length);

            return excelBytes;

        } catch (Exception e) {
            log.error("订单Excel导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据自定义字段导出订单数据
     */
    public byte[] exportOrdersWithSelectedFields(Map<String, Object> queryParams, List<String> selectedFields) {
        try {
            log.info("开始自定义字段导出订单Excel，查询参数: {}, 选择字段: {}", queryParams, selectedFields);

            // 查询订单数据
            List<Orders> ordersList = queryOrdersByParams(queryParams);

            if (ordersList.isEmpty()) {
                return generateEmptyExcel();
            }

            // 转换为导出DTO
            List<OrderExcelExportDto> exportData = convertToExportDto(ordersList);

            // 根据选择的字段生成Excel
            return generateCustomFieldsExcel(exportData, selectedFields);

        } catch (Exception e) {
            log.error("自定义字段订单Excel导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("自定义字段Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页导出大量订单数据
     */
    public byte[] exportOrdersWithPagination(Map<String, Object> queryParams, Integer pageSize, Integer maxRows) {
        try {
            log.info("开始分页导出订单Excel，查询参数: {}, 页大小: {}, 最大行数: {}",
                    queryParams, pageSize, maxRows);

            List<OrderExcelExportDto> allExportData = new ArrayList<>();
            int currentPage = 1;
            int totalExported = 0;

            while (totalExported < maxRows) {
                // 设置分页参数
                Map<String, Object> pageQueryParams = new HashMap<>(queryParams);
                pageQueryParams.put("page", currentPage);
                pageQueryParams.put("size", Math.min(pageSize, maxRows - totalExported));

                // 查询当前页数据
                List<Orders> pageOrders = queryOrdersByParamsWithPaging(pageQueryParams);

                if (pageOrders.isEmpty()) {
                    break; // 没有更多数据
                }

                // 转换并添加到总列表
                List<OrderExcelExportDto> pageExportData = convertToExportDto(pageOrders);
                allExportData.addAll(pageExportData);

                totalExported += pageOrders.size();
                currentPage++;

                log.info("已加载第{}页数据，当前总数: {}", currentPage - 1, totalExported);
            }

            if (allExportData.isEmpty()) {
                return generateEmptyExcel();
            }

            // 生成Excel文件
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream, OrderExcelExportDto.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("订单数据")
                    .doWrite(allExportData);

            byte[] excelBytes = outputStream.toByteArray();

            log.info("分页订单Excel导出完成，总行数: {}, 文件大小: {} bytes",
                    allExportData.size(), excelBytes.length);

            return excelBytes;

        } catch (Exception e) {
            log.error("分页订单Excel导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("分页Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成订单导出模板
     */
    public byte[] generateExportTemplate() {
        try {
            log.info("生成订单Excel导出模板");

            // 创建示例数据
            List<OrderExcelExportDto> templateData = createTemplateData();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream, OrderExcelExportDto.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("订单导出模板")
                    .doWrite(templateData);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成订单Excel导出模板失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成导出模板失败", e);
        }
    }

    /**
     * 根据查询参数查询订单（模拟实现）
     */
    private List<Orders> queryOrdersByParams(Map<String, Object> queryParams) {
        try {
            // 模拟查询订单数据，实际项目中这里应该调用数据库查询
            log.info("模拟查询订单数据，参数: {}", queryParams);

            List<Orders> mockOrders = generateMockOrdersData();

            // 根据查询参数进行简单过滤
            String status = (String) queryParams.get("status");
            if (status != null && !status.isEmpty()) {
                return mockOrders.stream()
                        .filter(order -> status.equals(order.getStatus()))
                        .collect(Collectors.toList());
            }

            return mockOrders;

        } catch (Exception e) {
            log.error("查询订单数据失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 分页查询订单（模拟实现）
     */
    private List<Orders> queryOrdersByParamsWithPaging(Map<String, Object> queryParams) {
        try {
            Integer page = (Integer) queryParams.get("page");
            Integer size = (Integer) queryParams.get("size");

            List<Orders> allOrders = queryOrdersByParams(queryParams);

            int start = (page - 1) * size;
            int end = Math.min(start + size, allOrders.size());

            if (start >= allOrders.size()) {
                return new ArrayList<>();
            }

            return allOrders.subList(start, end);

        } catch (Exception e) {
            log.error("分页查询订单数据失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成模拟订单数据
     */
    private List<Orders> generateMockOrdersData() {
        List<Orders> orders = new ArrayList<>();

        String[] statuses = {"PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"};
        String[] paymentTypes = {"ALIPAY", "WECHAT", "BANK_CARD"};

        for (int i = 1; i <= 20; i++) {
            Orders order = new Orders();
            order.setId((long) i);
            order.setOrderNo("ORD20250731" + String.format("%03d", i));
            order.setUserId(1001 + i % 5); // 修正为Integer类型
            order.setTotalAmount(new BigDecimal(1000 + i * 100));
            order.setPaymentAmount(new BigDecimal(1000 + i * 100 - 50)); // 支付金额略小于总金额
            order.setPaymentType(paymentTypes[i % paymentTypes.length]);
            order.setStatus(statuses[i % statuses.length]);
            order.setShippingAddress("测试地址" + i + "号，某某街道某某小区");
            order.setCreateTime(LocalDateTime.now().minusDays(i % 10));
            orders.add(order);
        }

        return orders;
    }

    /**
     * 转换为导出DTO
     */
    private List<OrderExcelExportDto> convertToExportDto(List<Orders> ordersList) {
        return ordersList.stream().map(order -> {
            OrderExcelExportDto exportDto = new OrderExcelExportDto();
            BeanUtils.copyProperties(order, exportDto);

            // 设置状态描述
            exportDto.setStatusDescription(exportDto.getStatusDescription());

            return exportDto;
        }).collect(Collectors.toList());
    }

    /**
     * 根据自定义字段生成Excel
     */
    private byte[] generateCustomFieldsExcel(List<OrderExcelExportDto> exportData, List<String> selectedFields) {
        try {
            // 简化实现：仍然导出全部字段
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream, OrderExcelExportDto.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("自定义订单数据")
                    .doWrite(exportData);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成自定义字段Excel失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成自定义字段Excel失败", e);
        }
    }

    /**
     * 生成空Excel文件
     */
    private byte[] generateEmptyExcel() {
        try {
            List<OrderExcelExportDto> emptyData = new ArrayList<>();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream, OrderExcelExportDto.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("订单数据")
                    .doWrite(emptyData);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成空Excel文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成空Excel文件失败", e);
        }
    }

    /**
     * 创建模板示例数据
     */
    private List<OrderExcelExportDto> createTemplateData() {
        List<OrderExcelExportDto> templateData = new ArrayList<>();

        // 示例订单1
        OrderExcelExportDto example1 = new OrderExcelExportDto();
        example1.setId(1L);
        example1.setOrderNumber("ORD20250731001");
        example1.setUserId(1001L);
        example1.setUsername("张三");
        example1.setProductName("iPhone 15 Pro");
        example1.setQuantity(1);
        example1.setUnitPrice(new BigDecimal("8999.00"));
        example1.setTotalAmount(new BigDecimal("8999.00"));
        example1.setStatus("PAID");
        example1.setShippingAddress("北京市朝阳区建国路100号");
        example1.setContactPhone("13800138001");
        example1.setRemark("急件，请尽快发货");
        example1.setCreateTime(LocalDateTime.now().minusDays(1));
        example1.setUpdateTime(LocalDateTime.now());
        templateData.add(example1);

        return templateData;
    }
}
