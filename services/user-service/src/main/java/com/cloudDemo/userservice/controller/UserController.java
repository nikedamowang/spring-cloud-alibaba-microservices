package com.cloudDemo.userservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public List<User> list() {
        return userMapper.selectList(new QueryWrapper<>());
    }
}

