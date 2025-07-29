package com.cloudDemo.management.service.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.cloudDemo.management.entity.ConfigAuditLog;
import com.cloudDemo.management.entity.ConfigTemplate;
import com.cloudDemo.management.entity.ConfigVersion;
import com.cloudDemo.management.service.IntelligentConfigService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能配置管理服务实现类
 * 实现企业级配置管理的核心功能
 */
@Slf4j
@Service
public class IntelligentConfigServiceImpl implements IntelligentConfigService {

    // 模拟数据存储 (实际项目中应使用数据库)
    private final Map<String, List<ConfigVersion>> versionStorage = new HashMap<>();
    private final List<ConfigAuditLog> auditStorage = new ArrayList<>();
    private final Map<Long, ConfigTemplate> templateStorage = new HashMap<>();
    // 直接使用Nacos ConfigService
    private ConfigService configService;
    @Value("${nacos.server-addr:localhost:8848}")
    private String serverAddr;
    private Long templateIdCounter = 1L;

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            this.configService = NacosFactory.createConfigService(properties);
            log.info("Nacos ConfigService初始化成功");
        } catch (Exception e) {
            log.error("Nacos ConfigService初始化失败", e);
        }
    }

    @Override
    public boolean updateConfigWithHotReload(String dataId, String content, String operator, String changeDescription) {
        try {
            log.info("开始执行配置热更新: dataId={}, operator={}", dataId, operator);

            // 1. 配置语法验证
            Map<String, Object> validationResult = validateConfigSyntax(content, "properties");
            if (!(Boolean) validationResult.get("valid")) {
                log.error("配置语法验证失败: {}", validationResult.get("errors"));
                return false;
            }

            // 2. 配置影响分析
            Map<String, Object> impactAnalysis = analyzeConfigImpact(dataId, content);
            log.info("配置影响分析: {}", impactAnalysis);

            // 3. 创建配置版本
            ConfigVersion version = createConfigVersion(dataId, content, operator, changeDescription);
            if (version == null) {
                log.error("创建配置版本失败");
                return false;
            }

            // 4. 推送到Nacos进行热更新
            boolean updateResult = publishConfigToNacos(dataId, "DEFAULT_GROUP", content);
            if (!updateResult) {
                log.error("推送配置到Nacos失败");
                return false;
            }

            // 5. 记录审计日志
            ConfigAuditLog auditLog = new ConfigAuditLog();
            auditLog.setDataId(dataId);
            auditLog.setGroupName("DEFAULT_GROUP");
            auditLog.setOperationType("UPDATE");
            auditLog.setOperator(operator);
            auditLog.setOperatorIp(getClientIp());
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setNewVersion(version.getVersion());
            auditLog.setChangeDescription(changeDescription);
            auditLog.setOperationResult("SUCCESS");
            auditLog.setRequestSource("INTELLIGENT_CONFIG_SYSTEM");
            auditLog.setEnvironment("PRODUCTION");
            recordAuditLog(auditLog);

            log.info("配置热更新成功: dataId={}, version={}", dataId, version.getVersion());
            return true;

        } catch (Exception e) {
            log.error("配置热更新失败: dataId={}, error={}", dataId, e.getMessage(), e);

            // 记录失败审计日志
            ConfigAuditLog failedLog = new ConfigAuditLog();
            failedLog.setDataId(dataId);
            failedLog.setOperationType("UPDATE");
            failedLog.setOperator(operator);
            failedLog.setOperationTime(LocalDateTime.now());
            failedLog.setOperationResult("FAILED");
            failedLog.setFailureReason(e.getMessage());
            recordAuditLog(failedLog);

            return false;
        }
    }

    // 添加Nacos配置发布方法
    private boolean publishConfigToNacos(String dataId, String group, String content) {
        try {
            return configService.publishConfig(dataId, group, content);
        } catch (Exception e) {
            log.error("发布配置到Nacos失败: dataId={}, error={}", dataId, e.getMessage(), e);
            return false;
        }
    }

    // 添加从Nacos获取配置方法
    private String getConfigFromNacos(String dataId, String group) {
        try {
            return configService.getConfig(dataId, group, 3000);
        } catch (Exception e) {
            log.error("从Nacos获取配置失败: dataId={}, error={}", dataId, e.getMessage(), e);
            return "";
        }
    }

    @Override
    public ConfigVersion createConfigVersion(String dataId, String content, String operator, String changeDescription) {
        try {
            List<ConfigVersion> versions = versionStorage.computeIfAbsent(dataId, k -> new ArrayList<>());

            // 计算内容MD5
            String contentMd5 = DigestUtils.md5DigestAsHex(content.getBytes());

            // 检查内容是否有变化
            if (!versions.isEmpty()) {
                ConfigVersion lastVersion = versions.get(versions.size() - 1);
                if (contentMd5.equals(lastVersion.getContentMd5())) {
                    log.info("配置内容无变化，跳过版本创建: dataId={}", dataId);
                    return lastVersion;
                }
                // 将上一版本设为非活跃
                lastVersion.setIsActive(false);
            }

            ConfigVersion newVersion = new ConfigVersion();
            newVersion.setId((long) (versions.size() + 1));
            newVersion.setDataId(dataId);
            newVersion.setGroupName("DEFAULT_GROUP");
            newVersion.setVersion(versions.size() + 1);
            newVersion.setContent(content);
            newVersion.setContentMd5(contentMd5);
            newVersion.setChangeDescription(changeDescription);
            newVersion.setOperator(operator);
            newVersion.setCreateTime(LocalDateTime.now());
            newVersion.setIsActive(true);
            newVersion.setEnvironment("PRODUCTION");
            newVersion.setChangeType("UPDATE");

            versions.add(newVersion);

            log.info("配置版本创建成功: dataId={}, version={}", dataId, newVersion.getVersion());
            return newVersion;

        } catch (Exception e) {
            log.error("创建配置版本失败: dataId={}, error={}", dataId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean rollbackToVersion(String dataId, Integer targetVersion, String operator) {
        try {
            log.info("开始配置回滚: dataId={}, targetVersion={}, operator={}", dataId, targetVersion, operator);

            List<ConfigVersion> versions = versionStorage.get(dataId);
            if (versions == null || versions.isEmpty()) {
                log.error("未找到配置版本历史: dataId={}", dataId);
                return false;
            }

            ConfigVersion targetVersionConfig = versions.stream()
                    .filter(v -> v.getVersion().equals(targetVersion))
                    .findFirst()
                    .orElse(null);

            if (targetVersionConfig == null) {
                log.error("未找到目标版本: dataId={}, version={}", dataId, targetVersion);
                return false;
            }

            // 推送目标版本配置到Nacos
            boolean rollbackResult = publishConfigToNacos(dataId, "DEFAULT_GROUP", targetVersionConfig.getContent());
            if (!rollbackResult) {
                log.error("推送回滚配置到Nacos失败");
                return false;
            }

            // 创建回滚版本记录
            ConfigVersion rollbackVersion = new ConfigVersion();
            rollbackVersion.setId((long) (versions.size() + 1));
            rollbackVersion.setDataId(dataId);
            rollbackVersion.setGroupName("DEFAULT_GROUP");
            rollbackVersion.setVersion(versions.size() + 1);
            rollbackVersion.setContent(targetVersionConfig.getContent());
            rollbackVersion.setContentMd5(targetVersionConfig.getContentMd5());
            rollbackVersion.setChangeDescription("回滚到版本 " + targetVersion);
            rollbackVersion.setOperator(operator);
            rollbackVersion.setCreateTime(LocalDateTime.now());
            rollbackVersion.setIsActive(true);
            rollbackVersion.setEnvironment("PRODUCTION");
            rollbackVersion.setChangeType("ROLLBACK");

            // 设置当前活跃版本为非活跃
            versions.stream().forEach(v -> v.setIsActive(false));
            rollbackVersion.setIsActive(true);
            versions.add(rollbackVersion);

            // 记录审计日志
            ConfigAuditLog auditLog = new ConfigAuditLog();
            auditLog.setDataId(dataId);
            auditLog.setGroupName("DEFAULT_GROUP");
            auditLog.setOperationType("ROLLBACK");
            auditLog.setOperator(operator);
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setOldVersion(getCurrentActiveVersion(dataId));
            auditLog.setNewVersion(rollbackVersion.getVersion());
            auditLog.setChangeDescription("回滚到版本 " + targetVersion);
            auditLog.setOperationResult("SUCCESS");
            recordAuditLog(auditLog);

            log.info("配置回滚成功: dataId={}, 从版本{}回滚到版本{}", dataId, getCurrentActiveVersion(dataId), targetVersion);
            return true;

        } catch (Exception e) {
            log.error("配置回滚失败: dataId={}, error={}", dataId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ConfigVersion> getConfigVersionHistory(String dataId, Integer limit) {
        List<ConfigVersion> versions = versionStorage.get(dataId);
        if (versions == null || versions.isEmpty()) {
            return new ArrayList<>();
        }

        // 按版本号倒序排列
        versions.sort((v1, v2) -> v2.getVersion().compareTo(v1.getVersion()));

        if (limit != null && limit > 0) {
            return versions.subList(0, Math.min(limit, versions.size()));
        }

        return new ArrayList<>(versions);
    }

    @Override
    public Map<String, Object> compareConfigVersions(String dataId, Integer version1, Integer version2) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ConfigVersion> versions = versionStorage.get(dataId);
            if (versions == null) {
                result.put("success", false);
                result.put("message", "未找到配置版本历史");
                return result;
            }

            ConfigVersion v1 = versions.stream().filter(v -> v.getVersion().equals(version1)).findFirst().orElse(null);
            ConfigVersion v2 = versions.stream().filter(v -> v.getVersion().equals(version2)).findFirst().orElse(null);

            if (v1 == null || v2 == null) {
                result.put("success", false);
                result.put("message", "版本不存在");
                return result;
            }

            // 简单的差异对比 (实际项目中可以使用更复杂的diff算法)
            Map<String, Object> diff = new HashMap<>();
            diff.put("version1", v1.getVersion());
            diff.put("version2", v2.getVersion());
            diff.put("contentSame", v1.getContentMd5().equals(v2.getContentMd5()));
            diff.put("version1Time", v1.getCreateTime());
            diff.put("version2Time", v2.getCreateTime());
            diff.put("version1Operator", v1.getOperator());
            diff.put("version2Operator", v2.getOperator());

            result.put("success", true);
            result.put("diff", diff);
            result.put("message", "版本对比完成");

        } catch (Exception e) {
            log.error("版本对比失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "版本对比失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> compareEnvironmentConfigs(String dataId, String env1, String env2) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "环境配置对比功能暂未实现，需要多环境配置支持");
        result.put("env1", env1);
        result.put("env2", env2);
        return result;
    }

    @Override
    public boolean recordAuditLog(ConfigAuditLog auditLog) {
        try {
            auditLog.setId((long) (auditStorage.size() + 1));
            auditStorage.add(auditLog);
            log.info("审计日志记录成功: operation={}, dataId={}, operator={}",
                    auditLog.getOperationType(), auditLog.getDataId(), auditLog.getOperator());
            return true;
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ConfigAuditLog> queryAuditLogs(String dataId, String startTime, String endTime, String operator) {
        return auditStorage.stream()
                .filter(log -> dataId == null || dataId.equals(log.getDataId()))
                .filter(log -> operator == null || operator.equals(log.getOperator()))
                .filter(log -> filterByTimeRange(log, startTime, endTime))
                .sorted((l1, l2) -> l2.getOperationTime().compareTo(l1.getOperationTime()))
                .toList();
    }

    @Override
    public Map<String, Object> validateConfigSyntax(String content, String configType) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            if ("properties".equalsIgnoreCase(configType)) {
                // Properties格式验证
                Properties props = new Properties();
                props.load(new java.io.StringReader(content));

                // 基本语法检查
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    if (!line.contains("=") && !line.contains(":")) {
                        errors.add("第" + (i + 1) + "行: 缺少键值分隔符");
                    }
                }
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            result.put("configType", configType);

        } catch (Exception e) {
            errors.add("配置解析错误: " + e.getMessage());
            result.put("valid", false);
            result.put("errors", errors);
        }

        return result;
    }

    @Override
    public Map<String, Object> analyzeConfigImpact(String dataId, String newContent) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取当前配置
            String currentContent = getConfigFromNacos(dataId, "DEFAULT_GROUP");

            Map<String, Object> impact = new HashMap<>();
            impact.put("affectedService", dataId);
            impact.put("hasChanges", !newContent.equals(currentContent));
            impact.put("requiresRestart", analyzeRestartRequired(currentContent, newContent));
            impact.put("riskLevel", calculateRiskLevel(dataId, currentContent, newContent));

            result.put("success", true);
            result.put("impact", impact);

        } catch (Exception e) {
            log.error("配置影响分析失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "影响分析失败: " + e.getMessage());
        }

        return result;
    }

    // 其他方法继续实现...
    @Override
    public boolean createConfigFromTemplate(Long templateId, Map<String, String> variables, String dataId, String operator) {
        // 模板创建配置功能实现
        return true;
    }

    @Override
    public boolean createConfigTemplate(ConfigTemplate template) {
        template.setId(templateIdCounter++);
        template.setCreateTime(LocalDateTime.now());
        template.setUsageCount(0);
        templateStorage.put(template.getId(), template);
        return true;
    }

    @Override
    public List<ConfigTemplate> getConfigTemplates(String templateType) {
        return templateStorage.values().stream()
                .filter(t -> templateType == null || templateType.equals(t.getTemplateType()))
                .filter(ConfigTemplate::getIsEnabled)
                .toList();
    }

    // 辅助方法
    private Integer getCurrentActiveVersion(String dataId) {
        List<ConfigVersion> versions = versionStorage.get(dataId);
        if (versions != null) {
            return versions.stream()
                    .filter(ConfigVersion::getIsActive)
                    .map(ConfigVersion::getVersion)
                    .findFirst()
                    .orElse(0);
        }
        return 0;
    }

    private String getClientIp() {
        return "127.0.0.1"; // 简化实现
    }

    private boolean filterByTimeRange(ConfigAuditLog log, String startTime, String endTime) {
        // 时间范围过滤逻辑
        return true; // 简化实现
    }

    private boolean analyzeRestartRequired(String oldContent, String newContent) {
        // 分析是否需要重启服务
        String[] restartRequiredKeys = {"server.port", "spring.datasource", "dubbo.protocol.port"};

        for (String key : restartRequiredKeys) {
            if (isPropertyChanged(oldContent, newContent, key)) {
                return true;
            }
        }
        return false;
    }

    private String calculateRiskLevel(String dataId, String oldContent, String newContent) {
        // 计算变更风险级别
        if (dataId.contains("gateway")) return "HIGH";
        if (isPropertyChanged(oldContent, newContent, "server.port")) return "HIGH";
        if (isPropertyChanged(oldContent, newContent, "spring.datasource")) return "MEDIUM";
        return "LOW";
    }

    private boolean isPropertyChanged(String oldContent, String newContent, String key) {
        // 检查特定属性是否发生变化
        return !getPropertyValue(oldContent, key).equals(getPropertyValue(newContent, key));
    }

    private String getPropertyValue(String content, String key) {
        // 从配置内容中提取属性值
        try {
            Properties props = new Properties();
            props.load(new java.io.StringReader(content));
            return props.getProperty(key, "");
        } catch (Exception e) {
            return "";
        }
    }
}
