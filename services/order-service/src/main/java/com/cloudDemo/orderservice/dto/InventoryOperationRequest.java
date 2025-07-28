package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "库存操作请求DTO")
public class InventoryOperationRequest {

    @NotBlank(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "TEST-001", required = true)
    private String productId;

    @NotNull(message = "操作数量不能为空")
    @Min(value = 1, message = "操作数量必须大于0")
    @Schema(description = "操作数量", example = "10", required = true)
    private Integer quantity;
}
