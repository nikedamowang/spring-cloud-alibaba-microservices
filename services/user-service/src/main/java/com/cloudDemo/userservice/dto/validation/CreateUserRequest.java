package com.cloudDemo.userservice.dto.validation;

import com.cloudDemo.userservice.validation.annotation.PasswordMatch;
import com.cloudDemo.userservice.validation.annotation.UniqueUsername;
import com.cloudDemo.userservice.validation.group.CreateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求DTO
 * 专门用于用户创建场景的参数校验
 */
@Data
@Schema(description = "创建用户请求")
@PasswordMatch(groups = CreateGroup.class, message = "密码和确认密码不一致")
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空", groups = CreateGroup.class)
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间", groups = CreateGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = CreateGroup.class)
    @UniqueUsername(groups = CreateGroup.class, message = "用户名已存在")
    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @NotBlank(message = "密码不能为空", groups = CreateGroup.class)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间", groups = CreateGroup.class)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$",
            message = "密码必须包含至少一个大写字母、一个小写字母和一个数字", groups = CreateGroup.class)
    @Schema(description = "密码", example = "Password123")
    private String password;

    @NotBlank(message = "确认密码不能为空", groups = CreateGroup.class)
    @Schema(description = "确认密码", example = "Password123")
    private String confirmPassword;

    @NotBlank(message = "邮箱不能为空", groups = CreateGroup.class)
    @Email(message = "邮箱格式不正确", groups = CreateGroup.class)
    @Size(max = 100, message = "邮箱长度不能超过100个字符", groups = CreateGroup.class)
    @Schema(description = "邮箱地址", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = CreateGroup.class)
    @Schema(description = "手机号", example = "13812345678")
    private String phone;
}
