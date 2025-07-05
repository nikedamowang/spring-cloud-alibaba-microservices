package com.cloudDemo.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.api.dto.AuthRequest;
import com.cloudDemo.api.dto.AuthResponse;
import com.cloudDemo.api.dto.Result;
import com.cloudDemo.api.util.JwtUtil;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务
 */
@Slf4j
@Service
public class AuthService {

    private static final String TOKEN_PREFIX = "user:token:";
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";
    private static final String USER_INFO_PREFIX = "user:info:";
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录
     */
    public Result<AuthResponse> login(AuthRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            // 查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", request.getUsername());
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证密码（直接使用明文密码比较）
            if (!request.getPassword().equals(user.getPassword())) {
                return Result.error("密码错误");
            }

            // 检查用户状态 - 修复：status现在是String类型
            if (!"active".equals(user.getStatus())) {
                return Result.error("用户已被禁用");
            }

            // 生成token - 修复：id现在是Integer类型
            String token = JwtUtil.generateToken(String.valueOf(user.getId()), user.getUsername());
            String refreshToken = JwtUtil.generateRefreshToken(String.valueOf(user.getId()));

            // 将token存储到Redis中，设置过期时间
            redisTemplate.opsForValue().set(TOKEN_PREFIX + user.getId(), token, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(), refreshToken, 7, TimeUnit.DAYS);

            // 缓存用户信息到Redis
            redisTemplate.opsForValue().set(USER_INFO_PREFIX + user.getId(), user, 24, TimeUnit.HOURS);

            // 构建响应
            AuthResponse response = new AuthResponse(
                    token,
                    refreshToken,
                    user.getId().longValue(), // 修复：将Integer转换为Long
                    user.getUsername(),
                    JwtUtil.getExpirationTime()
            );

            log.info("用户登录成功: {}", user.getUsername());
            return Result.success(response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户注销
     */
    public Result<String> logout(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return Result.error("Token不能为空");
            }

            // 解析token获取用户ID
            String userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的Token");
            }

            // 从Redis中删除token和用户信息
            redisTemplate.delete(TOKEN_PREFIX + userId);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
            redisTemplate.delete(USER_INFO_PREFIX + userId);

            log.info("用户注销成功: userId={}", userId);
            return Result.success("注销成功");

        } catch (Exception e) {
            log.error("用户注销失败", e);
            return Result.error("注销失败：" + e.getMessage());
        }
    }

    /**
     * 验证token是否有效
     */
    public Result<User> validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return Result.error("Token不能为空");
            }

            // 验证JWT token
            if (!JwtUtil.validateToken(token)) {
                return Result.error("Token无效或已过期");
            }

            // 获取用户ID
            String userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("Token中没有用户信息");
            }

            // 检查Redis中是否存在该token
            String cachedToken = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
            if (!token.equals(cachedToken)) {
                return Result.error("Token已失效");
            }

            // 从Redis获取用户信息
            User user = (User) redisTemplate.opsForValue().get(USER_INFO_PREFIX + userId);
            if (user == null) {
                // 如果Redis中没有，从数据库查询 - 修复：使用Integer.valueOf
                user = userMapper.selectById(Integer.valueOf(userId));
                if (user != null) {
                    // 重新缓存到Redis
                    redisTemplate.opsForValue().set(USER_INFO_PREFIX + userId, user, 24, TimeUnit.HOURS);
                }
            }

            if (user == null) {
                return Result.error("用户不存在");
            }

            return Result.success(user);

        } catch (Exception e) {
            log.error("Token验证失败", e);
            return Result.error("Token验证失败：" + e.getMessage());
        }
    }

    /**
     * 刷新token
     */
    public Result<AuthResponse> refreshToken(String refreshToken) {
        try {
            if (!StringUtils.hasText(refreshToken)) {
                return Result.error("RefreshToken不能为空");
            }

            // 验证refresh token
            if (!JwtUtil.validateToken(refreshToken)) {
                return Result.error("RefreshToken无效或已过期");
            }

            String userId = JwtUtil.getUserIdFromToken(refreshToken);
            if (userId == null) {
                return Result.error("RefreshToken中没有用户信息");
            }

            // 检查Redis中的refresh token
            String cachedRefreshToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
            if (!refreshToken.equals(cachedRefreshToken)) {
                return Result.error("RefreshToken已失效");
            }

            // 获取用户信息 - 修复：使用Integer.valueOf
            User user = userMapper.selectById(Integer.valueOf(userId));
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 生成新的token
            String newToken = JwtUtil.generateToken(userId, user.getUsername());
            String newRefreshToken = JwtUtil.generateRefreshToken(userId);

            // 更新Redis中的token
            redisTemplate.opsForValue().set(TOKEN_PREFIX + userId, newToken, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, newRefreshToken, 7, TimeUnit.DAYS);

            AuthResponse response = new AuthResponse(
                    newToken,
                    newRefreshToken,
                    user.getId().longValue(), // 修复：将Integer转换为Long
                    user.getUsername(),
                    JwtUtil.getExpirationTime()
            );

            return Result.success(response);

        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error("刷新Token失败：" + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    public Result<String> register(AuthRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
                return Result.error("用户名长度必须在3-20个字符之间");
            }

            if (request.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6个字符");
            }

            // 检查用户名是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", request.getUsername());
            User existingUser = userMapper.selectOne(queryWrapper);

            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword()); // 直接存储明文密码
            user.setStatus("active"); // 修复：使用String类型，默认为active状态
            user.setCreateTime(java.time.LocalDateTime.now());
            user.setUpdateTime(java.time.LocalDateTime.now());

            // 保存到数据库
            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("用户注册成功: {}", user.getUsername());
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }

        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }
}
