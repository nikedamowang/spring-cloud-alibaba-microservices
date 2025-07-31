package com.cloudDemo.order.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单Excel导出数据模型
 * 定义订单导出Excel的列结构和格式
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
@ColumnWidth(15)
public class OrderExcelExportDto {

    /**
     * 订单ID
     */
    @ExcelProperty(value = "订单ID", index = 0)
    @ColumnWidth(12)
    private Long id;

    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号", index = 1)
    @ColumnWidth(20)
    private String orderNumber;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID", index = 2)
    @ColumnWidth(12)
    private Long userId;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名", index = 3)
    @ColumnWidth(15)
    private String username;

    /**
     * 商品名称
     */
    @ExcelProperty(value = "商品名称", index = 4)
    @ColumnWidth(25)
    private String productName;

    /**
     * 商品数量
     */
    @ExcelProperty(value = "商品数量", index = 5)
    @ColumnWidth(10)
    private Integer quantity;

    /**
     * 单价
     */
    @ExcelProperty(value = "单价(元)", index = 6)
    @ColumnWidth(12)
    private BigDecimal unitPrice;

    /**
     * 订单总金额
     */
    @ExcelProperty(value = "订单总金额(元)", index = 7)
    @ColumnWidth(15)
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @ExcelProperty(value = "订单状态", index = 8)
    @ColumnWidth(12)
    private String status;

    /**
     * 订单状态中文描述
     */
    @ExcelProperty(value = "状态描述", index = 9)
    @ColumnWidth(12)
    private String statusDescription;

    /**
     * 收货地址
     */
    @ExcelProperty(value = "收货地址", index = 10)
    @ColumnWidth(30)
    private String shippingAddress;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话", index = 11)
    @ColumnWidth(15)
    private String contactPhone;

    /**
     * 订单备注
     */
    @ExcelProperty(value = "订单备注", index = 12)
    @ColumnWidth(25)
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 13)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间", index = 14)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime updateTime;

    /**
     * 支付时间
     */
    @ExcelProperty(value = "支付时间", index = 15)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    @ExcelProperty(value = "发货时间", index = 16)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    @ExcelProperty(value = "完成时间", index = 17)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime completeTime;

    /**
     * 获取订单状态的中文描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }

        switch (status.toUpperCase()) {
            case "PENDING":
                return "待支付";
            case "PAID":
                return "已支付";
            case "SHIPPED":
                return "已发货";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
