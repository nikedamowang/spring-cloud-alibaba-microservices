package com.cloudDemo.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置变更审计日志实体类
 * 记录所有配置变更操作的详细审计信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("config_audit_log")
public class ConfigAuditLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置文件标识
     */
    @TableField("data_id")
    private String dataId;

    /**
     * 配置组名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 操作类型 (CREATE/UPDATE/DELETE/ROLLBACK/VIEW)
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作人员
     */
    @TableField("operator")
    private String operator;

    /**
     * 操作人员IP地址
     */
    @TableField("operator_ip")
    private String operatorIp;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 变更前版本号
     */
    @TableField("old_version")
    private Integer oldVersion;

    /**
     * 变更后版本号
     */
    @TableField("new_version")
    private Integer newVersion;

    /**
     * 变更描述
     */
    @TableField("change_description")
    private String changeDescription;

    /**
     * 操作结果 (SUCCESS/FAILED)
     */
    @TableField("operation_result")
    private String operationResult;

    /**
     * 失败原因
     */
    @TableField("failure_reason")
    private String failureReason;

    /**
     * 请求来源
     */
    @TableField("request_source")
    private String requestSource;

    /**
     * 环境标识
     */
    @TableField("environment")
    private String environment;

    /**
     * 影响的服务列表
     */
    @TableField("affected_services")
    private String affectedServices;
}
