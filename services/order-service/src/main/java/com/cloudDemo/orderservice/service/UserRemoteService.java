package com.cloudDemo.orderservice.service;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.api.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户远程调用服务
 */
@Service
public class UserRemoteService {

    @DubboReference(version = "1.0.0", timeout = 5000, check = false)
    private UserService userService;

    /**
     * 根据用户ID获取用户信息
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
     */
    public boolean userExists(Long userId) {
        try {
            return userService.userExists(userId);
        } catch (Exception e) {
            System.err.println("远程调用用户服务失败: " + e.getMessage());
            return false;
        }
    }
}
