package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "压力测试请求DTO")
public class StressTestRequest {

    @NotBlank(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "TEST001", required = true)
    private String productId;

    @NotNull(message = "线程数不能为空")
    @Min(value = 1, message = "线程数必须大于0")
    @Schema(description = "并发线程数", example = "5", required = true)
    private Integer threadCount;

    @NotNull(message = "每线程操作数不能为空")
    @Min(value = 1, message = "每线程操作数必须大于0")
    @Schema(description = "每线程扣减次数", example = "3", required = true)
    private Integer operationsPerThread;
}
