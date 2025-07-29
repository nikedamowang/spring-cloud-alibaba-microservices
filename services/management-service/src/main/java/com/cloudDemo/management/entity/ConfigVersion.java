package com.cloudDemo.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置版本管理实体类
 * 用于记录配置文件的版本历史，支持版本回滚功能
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("config_version")
public class ConfigVersion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置文件唯一标识 (dataId)
     */
    @TableField("data_id")
    private String dataId;

    /**
     * 配置组名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 版本号 (自动递增)
     */
    @TableField("version")
    private Integer version;

    /**
     * 配置内容
     */
    @TableField("content")
    private String content;

    /**
     * 配置内容MD5值，用于快速比较
     */
    @TableField("content_md5")
    private String contentMd5;

    /**
     * 变更描述
     */
    @TableField("change_description")
    private String changeDescription;

    /**
     * 操作人员
     */
    @TableField("operator")
    private String operator;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 是否为当前活跃版本
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 配置环境 (dev/test/prod)
     */
    @TableField("environment")
    private String environment;

    /**
     * 变更类型 (CREATE/UPDATE/ROLLBACK)
     */
    @TableField("change_type")
    private String changeType;
}
