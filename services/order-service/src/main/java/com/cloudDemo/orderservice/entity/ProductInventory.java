package com.cloudDemo.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_inventory")
@Schema(description = "商品库存实体")
public class ProductInventory {

    @TableId(type = IdType.AUTO)
    @Schema(description = "库存ID", example = "1")
    private Long id;

    @TableField("product_id")
    @Schema(description = "商品ID", example = "PROD-001")
    private String productId;

    @TableField("product_name")
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String productName;

    @TableField("total_stock")
    @Schema(description = "总库存", example = "1000")
    private Integer totalStock;

    @TableField("available_stock")
    @Schema(description = "可用库存", example = "850")
    private Integer availableStock;

    @TableField("reserved_stock")
    @Schema(description = "预扣库存", example = "100")
    private Integer reservedStock;

    @TableField("sold_stock")
    @Schema(description = "已售库存", example = "50")
    private Integer soldStock;

    @Schema(description = "库存状态", example = "NORMAL", allowableValues = {"NORMAL", "LOW_STOCK", "OUT_OF_STOCK", "DISABLED"})
    private String status;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;
}
