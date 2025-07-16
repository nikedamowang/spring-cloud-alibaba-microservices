package com.cloudDemo.orderservice.service;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.api.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户远程调用服务
 * 配置了多种负载均衡策略
 */
@Service
public class UserRemoteService {

    /**
     * 用户查询服务 - 使用轮询负载均衡
     * 适用于查询类操作，保证负载均匀分布
     */
    @DubboReference(
            version = "1.0.0",
            timeout = 5000,
            check = false,
            loadbalance = "roundrobin",  // 轮询负载均衡
            cluster = "failover",        // 失败自动切换
            retries = 2                  // 重试2次
    )
    private UserService userService;

    /**
     * 用户验证服务 - 使用最少活跃调用负载均衡
     * 适用于响应时间敏感的操作
     */
    @DubboReference(
            version = "1.0.0",
            timeout = 3000,
            check = false,
            loadbalance = "leastactive", // 最少活跃调用负载均衡
            cluster = "failfast",        // 快速失败
            retries = 0                  // 不重试
    )
    private UserService userValidationService;

    /**
     * 根据用户ID获取用户信息
     * 使用轮询负载均衡策略
     */
    public UserDTO getUserById(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            // 记录日志或处理异常
            System.err.println("远程调用用户服务失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户名获取用户信息
     * 使用轮询负载均衡策略
     */
    public UserDTO getUserByUsername(String username) {
        try {
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            System.err.println("远程调用用户服务失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有用户列表
     * 使用轮询负载均衡策略
     */
    public List<UserDTO> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (Exception e) {
            System.err.println("远程调用用户服务失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查用户是否存在
     * 使用轮询负载均衡策略
     */
    public boolean userExists(Long userId) {
        try {
            return userService.userExists(userId);
        } catch (Exception e) {
            System.err.println("远程调用用户服务失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 用户登录验证
     * 使用最少活跃调用负载均衡策略，响应更快
     * 通过用户名获取用户信息来实现验证逻辑
     */
    public UserDTO validateUser(String username, String password) {
        try {
            // 通过用户名获取用户信息
            UserDTO user = userValidationService.getUserByUsername(username);

            // 简单的验证逻辑（实际项目中应该在user-service中进行密码验证）
            if (user != null) {
                // 这里只是演示负载均衡，实际的密码验证应该在user-service中完成
                System.out.println("用户验证请求 - 用户名: " + username);
                return user;
            }
            return null;
        } catch (Exception e) {
            System.err.println("用户验证服务调用失败: " + e.getMessage());
            return null;
        }
    }
}
