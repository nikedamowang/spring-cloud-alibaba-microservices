package com.cloudDemo.userservice.validation.validator;

import com.cloudDemo.userservice.validation.annotation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 密码确认校验器
 * 实现密码和确认密码一致性校验
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.password();
        this.confirmPasswordField = constraintAnnotation.confirmPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        Object password = beanWrapper.getPropertyValue(passwordField);
        Object confirmPassword = beanWrapper.getPropertyValue(confirmPasswordField);

        if (password == null || confirmPassword == null) {
            return true; // 让其他校验注解处理空值情况
        }

        return password.equals(confirmPassword);
    }
}
