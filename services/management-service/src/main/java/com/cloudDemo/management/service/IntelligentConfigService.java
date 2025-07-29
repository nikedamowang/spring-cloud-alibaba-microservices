package com.cloudDemo.management.service;

import com.cloudDemo.management.entity.ConfigAuditLog;
import com.cloudDemo.management.entity.ConfigTemplate;
import com.cloudDemo.management.entity.ConfigVersion;

import java.util.List;
import java.util.Map;

/**
 * 智能配置管理服务接口
 * 提供配置版本管理、审计、模板化等企业级配置管理功能
 */
public interface IntelligentConfigService {

    /**
     * 动态配置热更新
     *
     * @param dataId            配置文件标识
     * @param content           新配置内容
     * @param operator          操作人员
     * @param changeDescription 变更描述
     * @return 更新结果
     */
    boolean updateConfigWithHotReload(String dataId, String content, String operator, String changeDescription);

    /**
     * 配置版本管理 - 创建新版本
     *
     * @param dataId            配置标识
     * @param content           配置内容
     * @param operator          操作人员
     * @param changeDescription 变更描述
     * @return 版本信息
     */
    ConfigVersion createConfigVersion(String dataId, String content, String operator, String changeDescription);

    /**
     * 配置回滚到指定版本
     *
     * @param dataId        配置标识
     * @param targetVersion 目标版本号
     * @param operator      操作人员
     * @return 回滚结果
     */
    boolean rollbackToVersion(String dataId, Integer targetVersion, String operator);

    /**
     * 获取配置版本历史
     *
     * @param dataId 配置标识
     * @param limit  返回数量限制
     * @return 版本历史列表
     */
    List<ConfigVersion> getConfigVersionHistory(String dataId, Integer limit);

    /**
     * 配置差异对比
     *
     * @param dataId   配置标识
     * @param version1 版本1
     * @param version2 版本2
     * @return 差异对比结果
     */
    Map<String, Object> compareConfigVersions(String dataId, Integer version1, Integer version2);

    /**
     * 环境配置差异对比
     *
     * @param dataId 配置标识
     * @param env1   环境1
     * @param env2   环境2
     * @return 环境差异对比结果
     */
    Map<String, Object> compareEnvironmentConfigs(String dataId, String env1, String env2);

    /**
     * 配置变更审计日志记录
     *
     * @param auditLog 审计日志信息
     * @return 记录结果
     */
    boolean recordAuditLog(ConfigAuditLog auditLog);

    /**
     * 查询配置变更审计日志
     *
     * @param dataId    配置标识
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param operator  操作人员
     * @return 审计日志列表
     */
    List<ConfigAuditLog> queryAuditLogs(String dataId, String startTime, String endTime, String operator);

    /**
     * 基于模板创建配置
     *
     * @param templateId 模板ID
     * @param variables  变量映射
     * @param dataId     目标配置标识
     * @param operator   操作人员
     * @return 创建结果
     */
    boolean createConfigFromTemplate(Long templateId, Map<String, String> variables, String dataId, String operator);

    /**
     * 配置模板管理 - 创建模板
     *
     * @param template 模板信息
     * @return 创建结果
     */
    boolean createConfigTemplate(ConfigTemplate template);

    /**
     * 获取配置模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    List<ConfigTemplate> getConfigTemplates(String templateType);

    /**
     * 配置语法验证
     *
     * @param content    配置内容
     * @param configType 配置类型 (properties/yaml/json)
     * @return 验证结果
     */
    Map<String, Object> validateConfigSyntax(String content, String configType);

    /**
     * 配置影响分析
     *
     * @param dataId     配置标识
     * @param newContent 新配置内容
     * @return 影响分析结果
     */
    Map<String, Object> analyzeConfigImpact(String dataId, String newContent);
}
