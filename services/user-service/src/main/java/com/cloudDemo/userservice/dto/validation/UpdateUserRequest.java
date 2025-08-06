package com.cloudDemo.userservice.dto.validation;

import com.cloudDemo.userservice.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 更新用户请求DTO
 * 专门用于用户更新场景的参数校验
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @NotNull(message = "用户ID不能为空", groups = UpdateGroup.class)
    @Positive(message = "用户ID必须是正整数", groups = UpdateGroup.class)
    @Schema(description = "用户ID", example = "1")
    private Integer id;

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间", groups = UpdateGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = UpdateGroup.class)
    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Email(message = "邮箱格式不正确", groups = UpdateGroup.class)
    @Size(max = 100, message = "邮箱长度不能超过100个字符", groups = UpdateGroup.class)
    @Schema(description = "邮箱地址", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = UpdateGroup.class)
    @Schema(description = "手机号", example = "13812345678")
    private String phone;

    @Pattern(regexp = "^(active|inactive|suspended)$", message = "用户状态只能是active、inactive或suspended", groups = UpdateGroup.class)
    @Schema(description = "用户状态", example = "active")
    private String status;
}
