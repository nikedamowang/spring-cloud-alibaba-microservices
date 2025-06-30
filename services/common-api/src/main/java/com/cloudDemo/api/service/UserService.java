package com.cloudDemo.api.service;

import com.cloudDemo.api.dto.UserDTO;
import java.util.List;

/**
 * 用户服务接口 - 供远程调用使用
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserDTO getUserByUsername(String username);

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    List<UserDTO> getAllUsers();

    /**
     * 检查用户是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean userExists(Long userId);
}
