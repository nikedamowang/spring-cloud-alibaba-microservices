package com.cloudDemo.userservice.validation.annotation;

import com.cloudDemo.userservice.validation.validator.PasswordMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 密码确认校验注解
 * 用于校验密码和确认密码是否一致
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface PasswordMatch {

    String message() default "密码和确认密码不一致";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 密码字段名
     */
    String password() default "password";

    /**
     * 确认密码字段名
     */
    String confirmPassword() default "confirmPassword";
}
