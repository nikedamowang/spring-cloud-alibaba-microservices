package com.cloudDemo.userservice.validation.annotation;

import com.cloudDemo.userservice.validation.validator.UniqueUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 用户名唯一性校验注解
 * 用于校验用户名在数据库中是否已存在
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {

    String message() default "用户名已存在";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
