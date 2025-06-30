package com.cloudDemo.userservice.service.impl;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.api.service.UserService;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类 - 暴露为Dubbo服务
 */
@DubboService(version = "1.0.0")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null;
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                Long.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
