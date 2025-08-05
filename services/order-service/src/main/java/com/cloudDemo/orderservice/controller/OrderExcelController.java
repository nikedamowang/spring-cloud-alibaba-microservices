package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.service.excel.OrderExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单Excel导出控制器
 * 提供订单数据导出和模板下载功能
 */
@Slf4j
@RestController
@RequestMapping("/api/order/excel")
@Tag(name = "订单Excel导出", description = "订单数据导出和模板管理API")
public class OrderExcelController {

    @Autowired
    private OrderExcelExportService orderExcelExportService;

    /**
     * 导出订单数据
     */
    @PostMapping("/export")
    @Operation(summary = "导出订单数据", description = "根据查询条件导出订单数据到Excel文件")
    public ResponseEntity<byte[]> exportOrders(
            @Parameter(description = "查询条件") @RequestBody Map<String, Object> queryParams) {

        try {
            log.info("开始处理订单Excel导出请求，查询参数: {}", queryParams);

            // 执行导出
            byte[] excelBytes = orderExcelExportService.exportOrdersToExcel(queryParams);

            // 生成文件名
            String fileName = generateFileName("订单数据导出", "xlsx");

            // 设置响应头
            HttpHeaders headers = createDownloadHeaders(fileName, excelBytes.length);

            log.info("订单Excel导出完成，文件名: {}, 大小: {} bytes", fileName, excelBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            log.error("订单Excel导出异常: {}", e.getMessage(), e);

            String errorMessage = "订单导出失败: " + e.getMessage();
            return ResponseEntity.internalServerError()
                    .body(errorMessage.getBytes());
        }
    }

    /**
     * 自定义字段导出订单数据
     */
    @PostMapping("/export-custom")
    @Operation(summary = "自定义字段导出", description = "根据选择的字段导出订单数据")
    public ResponseEntity<byte[]> exportOrdersWithCustomFields(
            @Parameter(description = "导出请求参数") @RequestBody Map<String, Object> requestParams) {

        try {
            Map<String, Object> queryParams = (Map<String, Object>) requestParams.get("queryParams");
            List<String> selectedFields = (List<String>) requestParams.get("selectedFields");

            log.info("开始处理自定义字段订单Excel导出，查询参数: {}, 选择字段: {}", queryParams, selectedFields);

            // 执行自定义导出
            byte[] excelBytes = orderExcelExportService.exportOrdersWithSelectedFields(queryParams, selectedFields);

            // 生成文件名
            String fileName = generateFileName("订单数据自定义导出", "xlsx");

            // 设置响应头
            HttpHeaders headers = createDownloadHeaders(fileName, excelBytes.length);

            log.info("自定义字段订单Excel导出完成，文件名: {}", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            log.error("自定义字段订单Excel导出异常: {}", e.getMessage(), e);

            String errorMessage = "自定义导出失败: " + e.getMessage();
            return ResponseEntity.internalServerError()
                    .body(errorMessage.getBytes());
        }
    }

    /**
     * 分页导出大量订单数据
     */
    @PostMapping("/export-large")
    @Operation(summary = "大数据量分页导出", description = "分页导出大量订单数据，避免内存溢出")
    public ResponseEntity<byte[]> exportLargeOrders(
            @Parameter(description = "分页导出参数") @RequestBody Map<String, Object> requestParams) {

        try {
            Map<String, Object> queryParams = (Map<String, Object>) requestParams.get("queryParams");
            Integer pageSize = (Integer) requestParams.getOrDefault("pageSize", 1000);
            Integer maxRows = (Integer) requestParams.getOrDefault("maxRows", 10000);

            log.info("开始处理大数据量订单Excel导出，页大小: {}, 最大行数: {}", pageSize, maxRows);

            // 执行分页导出
            byte[] excelBytes = orderExcelExportService.exportOrdersWithPagination(queryParams, pageSize, maxRows);

            // 生成文件名
            String fileName = generateFileName("订单数据大量导出", "xlsx");

            // 设置响应头
            HttpHeaders headers = createDownloadHeaders(fileName, excelBytes.length);

            log.info("大数据量订单Excel导出完成，文件名: {}", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            log.error("大数据量订单Excel导出异常: {}", e.getMessage(), e);

            String errorMessage = "大数据量导出失败: " + e.getMessage();
            return ResponseEntity.internalServerError()
                    .body(errorMessage.getBytes());
        }
    }

    /**
     * 下载订单导出模板
     */
    @GetMapping("/template")
    @Operation(summary = "下载导出模板", description = "下载订单数据导出的Excel模板文件")
    public ResponseEntity<byte[]> downloadExportTemplate() {

        try {
            log.info("用户请求下载订单Excel导出模板");

            // 生成模板文件
            byte[] templateBytes = orderExcelExportService.generateExportTemplate();

            // 生成文件名
            String fileName = generateFileName("订单导出模板", "xlsx");

            // 设置响应头
            HttpHeaders headers = createDownloadHeaders(fileName, templateBytes.length);

            log.info("订单导出模板生成成功，文件名: {}", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);

        } catch (Exception e) {
            log.error("生成订单导出模板失败: {}", e.getMessage(), e);

            String errorMessage = "模板生成失败: " + e.getMessage();
            return ResponseEntity.internalServerError()
                    .body(errorMessage.getBytes());
        }
    }

    /**
     * 获取可导出的字段列表
     */
    @GetMapping("/export-fields")
    @Operation(summary = "获取导出字段列表", description = "获取订单Excel导出支持的所有字段")
    public Map<String, Object> getExportFields() {

        Map<String, Object> result = new HashMap<>();

        // 定义可导出的字段
        List<Map<String, Object>> fields = new ArrayList<>();

        fields.add(createFieldInfo("id", "订单ID", "number", true));
        fields.add(createFieldInfo("orderNumber", "订单号", "string", true));
        fields.add(createFieldInfo("userId", "用户ID", "number", true));
        fields.add(createFieldInfo("username", "用户名", "string", false));
        fields.add(createFieldInfo("productName", "商品名称", "string", true));
        fields.add(createFieldInfo("quantity", "商品数量", "number", true));
        fields.add(createFieldInfo("unitPrice", "单价", "decimal", true));
        fields.add(createFieldInfo("totalAmount", "订单总金额", "decimal", true));
        fields.add(createFieldInfo("status", "订单状态", "string", true));
        fields.add(createFieldInfo("statusDescription", "状态描述", "string", false));
        fields.add(createFieldInfo("shippingAddress", "收货地址", "string", false));
        fields.add(createFieldInfo("contactPhone", "联系电话", "string", false));
        fields.add(createFieldInfo("remark", "订单备注", "string", false));
        fields.add(createFieldInfo("createTime", "创建时间", "datetime", true));
        fields.add(createFieldInfo("updateTime", "更新时间", "datetime", false));
        fields.add(createFieldInfo("payTime", "支付时间", "datetime", false));
        fields.add(createFieldInfo("shipTime", "发货时间", "datetime", false));
        fields.add(createFieldInfo("completeTime", "完成时间", "datetime", false));

        result.put("success", true);
        result.put("fields", fields);
        result.put("totalFields", fields.size());
        result.put("message", "获取导出字段列表成功");

        return result;
    }

    /**
     * 获取导出帮助信息
     */
    @GetMapping("/export-help")
    @Operation(summary = "获取导出帮助", description = "获取Excel导出的功能说明和使用指南")
    public Map<String, Object> getExportHelp() {

        Map<String, Object> help = new HashMap<>();

        // 导出功能说明
        Map<String, String> features = new HashMap<>();
        features.put("标准导出", "根据查询条件导出所有字段的订单数据");
        features.put("自定义字段导出", "选择需要的字段进行导出，减少文件大小");
        features.put("大数据量导出", "分页处理大量数据，避免内存溢出");
        features.put("模板下载", "下载包含示例数据的导出模板");

        // 查询参数说明
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", "订单状态筛选：PENDING/PAID/SHIPPED/COMPLETED/CANCELLED");
        queryParams.put("startDate", "开始日期：YYYY-MM-DD格式");
        queryParams.put("endDate", "结束日期：YYYY-MM-DD格式");
        queryParams.put("userId", "用户ID筛选");

        // 导出限制
        Map<String, Object> limitations = new HashMap<>();
        limitations.put("maxRows", "单次最大导出行数：10000");
        limitations.put("pageSize", "分页大小：1000行/页");
        limitations.put("fileFormat", "支持格式：.xlsx");
        limitations.put("maxFileSize", "文件大小限制：50MB");

        // API接口
        Map<String, String> apis = new HashMap<>();
        apis.put("标准导出", "POST /api/order/excel/export");
        apis.put("自定义导出", "POST /api/order/excel/export-custom");
        apis.put("大数据量导出", "POST /api/order/excel/export-large");
        apis.put("模板下载", "GET /api/order/excel/template");
        apis.put("字段列表", "GET /api/order/excel/export-fields");

        help.put("success", true);
        help.put("features", features);
        help.put("queryParams", queryParams);
        help.put("limitations", limitations);
        help.put("apis", apis);

        return help;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "服务健康检查", description = "检查Excel导出服务的健康状态")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("success", true);
        health.put("status", "UP");
        health.put("service", "OrderExcelExportService");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        return health;
    }

    // 私有辅助方法

    /**
     * 生成文件名
     */
    private String generateFileName(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + "." + extension;
    }

    /**
     * 创建下载响应头
     */
    private HttpHeaders createDownloadHeaders(String fileName, int contentLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(contentLength);
        return headers;
    }

    /**
     * 创建字段信息
     */
    private Map<String, Object> createFieldInfo(String fieldName, String displayName, String dataType, boolean isDefault) {
        Map<String, Object> field = new HashMap<>();
        field.put("fieldName", fieldName);
        field.put("displayName", displayName);
        field.put("dataType", dataType);
        field.put("isDefault", isDefault);
        return field;
    }
}
