package com.cloudDemo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 在线设备数量
     */
    private Integer deviceCount;

    /**
     * 当前活跃设备信息
     */
    private String activeDevice;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 在线状态
     */
    private Boolean online;
}
