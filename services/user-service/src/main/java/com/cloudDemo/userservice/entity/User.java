package com.cloudDemo.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;  // 数据库中是int类型

    private String username;
    private String password;
    private String email;
    private String phone;
    private String status; // 数据库中是varchar(20)类型，默认值为'active'

    @TableField("create_time")
    @JsonIgnore
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonIgnore
    private LocalDateTime updateTime;
}
