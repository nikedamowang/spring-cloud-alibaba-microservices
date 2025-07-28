package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "库存初始化请求DTO")
public class InventoryInitializeRequest {

    @NotBlank(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "TEST-001", required = true)
    private String productId;

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "测试商品", required = true)
    private String productName;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    @Schema(description = "初始库存数量", example = "100", required = true)
    private Integer totalStock;
}
