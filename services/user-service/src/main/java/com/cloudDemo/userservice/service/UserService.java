package com.cloudDemo.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    public LoginResult login(String username, String password) {
        try {
            // 查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                return LoginResult.failure("用户不存在");
            }

            // 验证密码 - 直接比较明文密码（练习项目简化处理）
            if (!password.equals(user.getPassword())) {
                return LoginResult.failure("密码错误");
            }

            // 检查用户状态
            if (!"active".equals(user.getStatus())) {
                return LoginResult.failure("用户已被禁用");
            }

            // 生成Token
            String roles = "user"; // 默认角色，实际项目可以从用户角色表查询
            String token = authService.generateUserToken(user.getId(), user.getUsername(), roles);

            // 更新最后登录时间
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("User login successful: {} (ID: {})", username, user.getId());

            return LoginResult.success(token, user);

        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            return LoginResult.failure("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户注销
     */
    public boolean logout(String token) {
        return authService.logout(token);
    }

    /**
     * 通过用户ID注销
     */
    public boolean logoutByUserId(Integer userId) {
        return authService.logoutByUserId(userId);
    }

    /**
     * 用户注册
     */
    public RegisterResult register(String username, String password, String email, String phone) {
        try {
            // 检查用户名是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User existUser = userMapper.selectOne(queryWrapper);

            if (existUser != null) {
                return RegisterResult.failure("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (email != null && !email.isEmpty()) {
                QueryWrapper<User> emailQuery = new QueryWrapper<>();
                emailQuery.eq("email", email);
                User emailUser = userMapper.selectOne(emailQuery);
                if (emailUser != null) {
                    return RegisterResult.failure("邮箱已被注册");
                }
            }

            // 创建新用户 - 直接存储明文密码（练习项目简化处理）
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password); // 直接存储明文密码
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setStatus("active");
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUpdateTime(LocalDateTime.now());

            int result = userMapper.insert(newUser);

            if (result > 0) {
                log.info("User registered successfully: {} (ID: {})", username, newUser.getId());
                return RegisterResult.success(newUser);
            } else {
                return RegisterResult.failure("用户注册失败");
            }

        } catch (Exception e) {
            log.error("Registration failed for user: {}", username, e);
            return RegisterResult.failure("注册失败：" + e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    public AuthService.UserSession validateToken(String token) {
        return authService.validateToken(token);
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        return authService.refreshToken(token);
    }

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    /**
     * 密码处理方法（已移除加密，直接返回原密码）
     * 保留方法以防其他地方调用，但不再进行加密处理
     */
    private String encodePassword(String password) {
        // 练习项目简化：直接返回原密码，不进行加密
        return password;
    }

    /**
     * 获取在线用户数量
     */
    public long getOnlineUserCount() {
        return authService.getOnlineUserCount();
    }

    /**
     * 登录结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginResult {
        private boolean success;
        private String message;
        private String token;
        private User user;

        public static LoginResult success(String token, User user) {
            return LoginResult.builder()
                    .success(true)
                    .message("登录成功")
                    .token(token)
                    .user(user)
                    .build();
        }

        public static LoginResult failure(String message) {
            return LoginResult.builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }

    /**
     * 注册结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RegisterResult {
        private boolean success;
        private String message;
        private User user;

        public static RegisterResult success(User user) {
            return RegisterResult.builder()
                    .success(true)
                    .message("注册成功")
                    .user(user)
                    .build();
        }

        public static RegisterResult failure(String message) {
            return RegisterResult.builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
