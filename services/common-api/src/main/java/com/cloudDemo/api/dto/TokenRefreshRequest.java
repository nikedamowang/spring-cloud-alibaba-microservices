package com.cloudDemo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Token刷新请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * 设备信息
     */
    private String deviceInfo;
}
