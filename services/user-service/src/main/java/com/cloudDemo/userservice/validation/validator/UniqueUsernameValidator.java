package com.cloudDemo.userservice.validation.validator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import com.cloudDemo.userservice.validation.annotation.UniqueUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户名唯一性校验器
 * 检查用户名在数据库中是否已存在
 */
@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
        // 初始化方法，可以获取注解的参数
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.trim().isEmpty()) {
            return true; // 空值由其他校验注解处理
        }

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            long count = userMapper.selectCount(queryWrapper);
            return count == 0; // 如果数量为0，说明用户名不存在，校验通过
        } catch (Exception e) {
            // 如果数据库查询出现异常，默认校验通过，避免影响正常流程
            return true;
        }
    }
}
