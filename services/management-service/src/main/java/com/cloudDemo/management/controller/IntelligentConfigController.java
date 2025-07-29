package com.cloudDemo.management.controller;

import com.cloudDemo.management.entity.ConfigAuditLog;
import com.cloudDemo.management.entity.ConfigTemplate;
import com.cloudDemo.management.entity.ConfigVersion;
import com.cloudDemo.management.service.IntelligentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能配置管理控制器
 * 提供企业级配置管理的完整API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/intelligent-config")
@Tag(name = "智能配置管理", description = "企业级配置管理系统API")
public class IntelligentConfigController {

    @Autowired
    private IntelligentConfigService intelligentConfigService;

    /**
     * 动态配置热更新
     */
    @PostMapping("/hot-update")
    @Operation(summary = "动态配置热更新", description = "无需重启服务即可更新配置")
    public Map<String, Object> hotUpdateConfig(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "新配置内容") @RequestBody String content,
            @Parameter(description = "操作人员") @RequestParam String operator,
            @Parameter(description = "变更描述") @RequestParam(required = false) String changeDescription) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = intelligentConfigService.updateConfigWithHotReload(
                    dataId, content, operator, changeDescription);

            result.put("success", success);
            result.put("message", success ? "配置热更新成功" : "配置热更新失败");
            result.put("dataId", dataId);
            result.put("operator", operator);

            log.info("配置热更新请求: dataId={}, operator={}, result={}", dataId, operator, success);

        } catch (Exception e) {
            log.error("配置热更新异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "配置热更新异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 配置版本回滚
     */
    @PostMapping("/rollback")
    @Operation(summary = "配置版本回滚", description = "回滚配置到指定历史版本")
    public Map<String, Object> rollbackConfig(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "目标版本号") @RequestParam Integer targetVersion,
            @Parameter(description = "操作人员") @RequestParam String operator) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = intelligentConfigService.rollbackToVersion(dataId, targetVersion, operator);

            result.put("success", success);
            result.put("message", success ? "配置回滚成功" : "配置回滚失败");
            result.put("dataId", dataId);
            result.put("targetVersion", targetVersion);
            result.put("operator", operator);

        } catch (Exception e) {
            log.error("配置回滚异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "配置回滚异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取配置版本历史
     */
    @GetMapping("/version-history")
    @Operation(summary = "获取配置版本历史", description = "查看配置文件的所有历史版本")
    public Map<String, Object> getVersionHistory(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {

        Map<String, Object> result = new HashMap<>();
        try {
            List<ConfigVersion> versions = intelligentConfigService.getConfigVersionHistory(dataId, limit);

            result.put("success", true);
            result.put("dataId", dataId);
            result.put("totalVersions", versions.size());
            result.put("versions", versions);
            result.put("message", "获取版本历史成功");

        } catch (Exception e) {
            log.error("获取版本历史异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取版本历史失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 配置版本差异对比
     */
    @GetMapping("/compare-versions")
    @Operation(summary = "配置版本差异对比", description = "对比两个版本的配置差异")
    public Map<String, Object> compareVersions(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "版本1") @RequestParam Integer version1,
            @Parameter(description = "版本2") @RequestParam Integer version2) {

        try {
            return intelligentConfigService.compareConfigVersions(dataId, version1, version2);
        } catch (Exception e) {
            log.error("版本对比异常: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "版本对比失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 环境配置差异对比
     */
    @GetMapping("/compare-environments")
    @Operation(summary = "环境配置差异对比", description = "对比不同环境的配置差异")
    public Map<String, Object> compareEnvironments(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "环境1") @RequestParam String env1,
            @Parameter(description = "环境2") @RequestParam String env2) {

        try {
            return intelligentConfigService.compareEnvironmentConfigs(dataId, env1, env2);
        } catch (Exception e) {
            log.error("环境对比异常: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "环境对比失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 查询配置变更审计日志
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "查询配置变更审计日志", description = "查询配置变更的详细审计记录")
    public Map<String, Object> getAuditLogs(
            @Parameter(description = "配置文件标识") @RequestParam(required = false) String dataId,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "操作人员") @RequestParam(required = false) String operator) {

        Map<String, Object> result = new HashMap<>();
        try {
            List<ConfigAuditLog> logs = intelligentConfigService.queryAuditLogs(dataId, startTime, endTime, operator);

            result.put("success", true);
            result.put("totalCount", logs.size());
            result.put("auditLogs", logs);
            result.put("message", "查询审计日志成功");

        } catch (Exception e) {
            log.error("查询审计日志异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询审计日志失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 配置语法验证
     */
    @PostMapping("/validate-syntax")
    @Operation(summary = "配置语法验证", description = "验证配置文件语法正确性")
    public Map<String, Object> validateSyntax(
            @Parameter(description = "配置内容") @RequestBody String content,
            @Parameter(description = "配置类型") @RequestParam(defaultValue = "properties") String configType) {

        try {
            return intelligentConfigService.validateConfigSyntax(content, configType);
        } catch (Exception e) {
            log.error("配置验证异常: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "配置验证失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 配置影响分析
     */
    @PostMapping("/analyze-impact")
    @Operation(summary = "配置影响分析", description = "分析配置变更对系统的潜在影响")
    public Map<String, Object> analyzeImpact(
            @Parameter(description = "配置文件标识") @RequestParam String dataId,
            @Parameter(description = "新配置内容") @RequestBody String newContent) {

        try {
            return intelligentConfigService.analyzeConfigImpact(dataId, newContent);
        } catch (Exception e) {
            log.error("影响分析异常: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "影响分析失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 创建配置模板
     */
    @PostMapping("/templates")
    @Operation(summary = "创建配置模板", description = "创建标准化的配置模板")
    public Map<String, Object> createTemplate(@RequestBody ConfigTemplate template) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = intelligentConfigService.createConfigTemplate(template);

            result.put("success", success);
            result.put("message", success ? "模板创建成功" : "模板创建失败");
            result.put("templateId", template.getId());

        } catch (Exception e) {
            log.error("创建模板异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "创建模板失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取配置模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "获取配置模板列表", description = "查询可用的配置模板")
    public Map<String, Object> getTemplates(
            @Parameter(description = "模板类型") @RequestParam(required = false) String templateType) {

        Map<String, Object> result = new HashMap<>();
        try {
            List<ConfigTemplate> templates = intelligentConfigService.getConfigTemplates(templateType);

            result.put("success", true);
            result.put("totalCount", templates.size());
            result.put("templates", templates);
            result.put("message", "获取模板列表成功");

        } catch (Exception e) {
            log.error("获取模板列表异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取模板列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 基于模板创建配置
     */
    @PostMapping("/create-from-template")
    @Operation(summary = "基于模板创建配置", description = "使用模板快速创建新的配置文件")
    public Map<String, Object> createFromTemplate(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "变量映射") @RequestBody Map<String, String> variables,
            @Parameter(description = "目标配置标识") @RequestParam String dataId,
            @Parameter(description = "操作人员") @RequestParam String operator) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = intelligentConfigService.createConfigFromTemplate(
                    templateId, variables, dataId, operator);

            result.put("success", success);
            result.put("message", success ? "基于模板创建配置成功" : "基于模板创建配置失败");
            result.put("dataId", dataId);
            result.put("templateId", templateId);

        } catch (Exception e) {
            log.error("基于模板创建配置异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "基于模板创建配置失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 智能配置管理仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "配置管理仪表板", description = "获取配置管理系统的统计信息")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalConfigs", 4); // 当前配置总数
            statistics.put("totalVersions", 12); // 总版本数
            statistics.put("totalAuditLogs", 25); // 总审计日志数
            statistics.put("totalTemplates", 3); // 模板总数

            // 最近活动
            List<ConfigAuditLog> recentActivities = intelligentConfigService.queryAuditLogs(null, null, null, null);

            result.put("success", true);
            result.put("statistics", statistics);
            result.put("recentActivities", recentActivities.stream().limit(10).toList());
            result.put("message", "获取仪表板数据成功");

        } catch (Exception e) {
            log.error("获取仪表板数据异常: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取仪表板数据失败: " + e.getMessage());
        }

        return result;
    }
}
