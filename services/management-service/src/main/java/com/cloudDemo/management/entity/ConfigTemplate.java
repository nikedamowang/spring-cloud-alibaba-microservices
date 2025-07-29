package com.cloudDemo.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置模板管理实体类
 * 用于管理标准化的配置模板，支持快速创建新服务配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("config_template")
public class ConfigTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型 (SERVICE/GATEWAY/DATABASE/REDIS等)
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 模板描述
     */
    @TableField("template_description")
    private String templateDescription;

    /**
     * 模板内容 (支持变量占位符)
     */
    @TableField("template_content")
    private String templateContent;

    /**
     * 模板变量定义 (JSON格式)
     * 例如: {"serviceName": "用户服务名称", "port": "服务端口"}
     */
    @TableField("template_variables")
    private String templateVariables;

    /**
     * 适用环境 (ALL/DEV/TEST/PROD)
     */
    @TableField("environment")
    private String environment;

    /**
     * 模板版本
     */
    @TableField("template_version")
    private String templateVersion;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 使用次数统计
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 模板标签 (用于分类和搜索)
     */
    @TableField("tags")
    private String tags;
}
