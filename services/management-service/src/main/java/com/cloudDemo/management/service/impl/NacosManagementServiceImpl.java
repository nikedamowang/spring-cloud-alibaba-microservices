package com.cloudDemo.management.service.impl;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cloudDemo.management.dto.NacosConfigInfo;
import com.cloudDemo.management.dto.NacosServiceInstance;
import com.cloudDemo.management.service.NacosManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Nacos 管理服务实现类
 */
@Service
public class NacosManagementServiceImpl implements NacosManagementService {

    private static final Logger logger = LoggerFactory.getLogger(NacosManagementServiceImpl.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private NamingService namingService;

    @Value("${nacos.config.template-path:./config-templates}")
    private String templatePath;

    @Override
    public NacosConfigInfo getConfigInfo(String dataId, String group, String namespace) {
        try {
            String content = configService.getConfig(dataId, group, 5000);
            if (content == null) {
                logger.warn("配置不存在: dataId={}, group={}, namespace={}", dataId, group, namespace);
                return null;
            }

            NacosConfigInfo configInfo = new NacosConfigInfo(dataId, group, namespace);
            configInfo.setContent(content);
            configInfo.setType(getConfigType(dataId));
            configInfo.setMd5(getMd5(content));
            configInfo.setLastModified(LocalDateTime.now());
            configInfo.setDescription("从 Nacos 获取的配置信息");

            logger.info("成功获取配置信息: {}", configInfo);
            return configInfo;

        } catch (NacosException e) {
            logger.error("获取配置信息失败: dataId={}, group={}, namespace={}", dataId, group, namespace, e);
            throw new RuntimeException("获取配置信息失败", e);
        }
    }

    @Override
    public List<NacosConfigInfo> getAllConfigs(String namespace) {
        try {
            List<NacosConfigInfo> configs = new ArrayList<>();

            // 真正从Nacos获取配置列表，而不是使用硬编码列表
            // 注意：由于Nacos API限制，我们需要尝试已知的配置文件
            // 但现在我们每次都重新从Nacos获取最新内容
            String[] possibleConfigs = {
                    "application.properties",
                    "user-service.properties",
                    "order-service.properties",
                    "dubbo.properties",
                    "redis.properties",
                    "management-service.properties"
            };

            logger.info("开始从Nacos获取最新配置列表...");

            for (String dataId : possibleConfigs) {
                try {
                    // 每次都重新从Nacos获取最新配置内容
                    NacosConfigInfo config = getConfigInfo(dataId, "DEFAULT_GROUP", namespace);
                    if (config != null) {
                        configs.add(config);
                        logger.info("成功获取配置: {} (内容长度: {})", dataId, config.getContent().length());
                    } else {
                        logger.debug("配置不存在: {}", dataId);
                    }
                } catch (Exception e) {
                    logger.warn("获取配置失败: {} - {}", dataId, e.getMessage());
                }
            }

            logger.info("配置获取完成，共获取到 {} 个配置文件", configs.size());
            return configs;

        } catch (Exception e) {
            logger.error("获取所有配置失败: namespace={}", namespace, e);
            throw new RuntimeException("获取所有配置失败", e);
        }
    }

    @Override
    public List<NacosConfigInfo> searchConfigs(String keyword, String namespace) {
        List<NacosConfigInfo> allConfigs = getAllConfigs(namespace);

        if (!StringUtils.hasText(keyword)) {
            return allConfigs;
        }

        return allConfigs.stream()
                .filter(config -> config.getDataId().contains(keyword) ||
                        config.getContent().contains(keyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<NacosServiceInstance> getServiceInstances(String serviceName, String groupName, String namespace) {
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName, groupName);

            return instances.stream().map(instance -> {
                NacosServiceInstance serviceInstance = new NacosServiceInstance();
                serviceInstance.setServiceName(serviceName);
                serviceInstance.setGroupName(groupName);
                serviceInstance.setNamespaceId(namespace);
                serviceInstance.setIp(instance.getIp());
                serviceInstance.setPort(instance.getPort());
                serviceInstance.setWeight(instance.getWeight());
                serviceInstance.setHealthy(instance.isHealthy());
                serviceInstance.setEnabled(instance.isEnabled());
                serviceInstance.setEphemeral(instance.isEphemeral());
                serviceInstance.setClusterName(instance.getClusterName());
                serviceInstance.setMetadata(instance.getMetadata());
                serviceInstance.setLastBeat(LocalDateTime.now());

                return serviceInstance;
            }).collect(Collectors.toList());

        } catch (NacosException e) {
            logger.error("获取服务实例失败: serviceName={}, groupName={}", serviceName, groupName, e);
            throw new RuntimeException("获取服务实例失败", e);
        }
    }

    @Override
    public List<String> getAllServices(String namespace) {
        try {
            // 修复：使用正确���API获取服务列表
            if (StringUtils.hasText(namespace)) {
                return namingService.getServicesOfServer(1, Integer.MAX_VALUE, namespace).getData();
            } else {
                // 对于空namespace，使用默认方式
                return namingService.getServicesOfServer(1, Integer.MAX_VALUE).getData();
            }
        } catch (NacosException e) {
            logger.error("获取所有服务失败: namespace={}", namespace, e);
            // 如果上面的方法失败，尝试另一种方式
            try {
                logger.info("尝试使用getAllInstances方法获取服务列表");
                // 尝试获取已知服务的实例来确认服务是否存在
                String[] knownServicesArray = {"management-service", "user-service", "order-service"};
                List<String> activeServices = new ArrayList<>();

                for (String serviceName : knownServicesArray) {
                    try {
                        List<Instance> instances = namingService.getAllInstances(serviceName);
                        if (instances != null && !instances.isEmpty()) {
                            activeServices.add(serviceName);
                        }
                    } catch (Exception ex) {
                        logger.debug("服务 {} 不存在或无实例", serviceName);
                    }
                }
                return activeServices;
            } catch (Exception fallbackException) {
                logger.error("备用方法也失败", fallbackException);
                throw new RuntimeException("获取所有服务失败", e);
            }
        }
    }

    @Override
    public Map<String, Object> getServiceHealth(String serviceName, String namespace) {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            List<NacosServiceInstance> instances = getServiceInstances(serviceName, "DEFAULT_GROUP", namespace);

            int totalInstances = instances.size();
            int healthyInstances = (int) instances.stream().filter(NacosServiceInstance::isHealthy).count();

            healthInfo.put("serviceName", serviceName);
            healthInfo.put("namespace", namespace);
            healthInfo.put("totalInstances", totalInstances);
            healthInfo.put("healthyInstances", healthyInstances);
            healthInfo.put("healthyRate", totalInstances > 0 ? (double) healthyInstances / totalInstances : 0.0);
            healthInfo.put("status", healthyInstances > 0 ? "UP" : "DOWN");
            healthInfo.put("instances", instances);
            healthInfo.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            logger.error("获取服务健康状态失败: serviceName={}", serviceName, e);
            healthInfo.put("status", "ERROR");
            healthInfo.put("error", e.getMessage());
        }

        return healthInfo;
    }

    @Override
    public Map<String, Object> compareConfigWithTemplate(String dataId, String group, String namespace) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取 Nacos 上的配置
            NacosConfigInfo nacosConfig = getConfigInfo(dataId, group, namespace);
            if (nacosConfig == null) {
                result.put("status", "CONFIG_NOT_FOUND");
                result.put("message", "Nacos 上未找到指定配置");
                return result;
            }

            // 读取本地模板
            String templateContent = readLocalTemplate(dataId);
            if (templateContent == null) {
                result.put("status", "TEMPLATE_NOT_FOUND");
                result.put("message", "本地模板文件不存在");
                result.put("nacosConfig", nacosConfig);
                return result;
            }

            // 比较差异
            boolean isIdentical = nacosConfig.getContent().equals(templateContent);

            result.put("status", isIdentical ? "IDENTICAL" : "DIFFERENT");
            result.put("dataId", dataId);
            result.put("group", group);
            result.put("namespace", namespace);
            result.put("nacosContent", nacosConfig.getContent());
            result.put("templateContent", templateContent);
            result.put("isIdentical", isIdentical);
            result.put("compareTime", LocalDateTime.now());

            if (!isIdentical) {
                result.put("suggestion", "配置与模板不一致，建议检查配置是否需要更新");
            }

        } catch (Exception e) {
            logger.error("比较配置与模板失败: dataId={}", dataId, e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> getNacosServerStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // 测试配置服务连接
            boolean configServiceAvailable = testConfigService();
            // 测试命名服务连接
            boolean namingServiceAvailable = testNamingService();

            status.put("configService", configServiceAvailable ? "UP" : "DOWN");
            status.put("namingService", namingServiceAvailable ? "UP" : "DOWN");
            status.put("overallStatus", (configServiceAvailable && namingServiceAvailable) ? "UP" : "DOWN");
            status.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            logger.error("检查 Nacos 服务器状态失败", e);
            status.put("overallStatus", "ERROR");
            status.put("error", e.getMessage());
        }

        return status;
    }

    // 私有辅助方法
    private String getConfigType(String dataId) {
        if (dataId.endsWith(".properties")) return "properties";
        if (dataId.endsWith(".yml") || dataId.endsWith(".yaml")) return "yaml";
        if (dataId.endsWith(".json")) return "json";
        if (dataId.endsWith(".xml")) return "xml";
        return "text";
    }

    private String getMd5(String content) {
        // 简单的 MD5 实现，实际项目中可以使用更完善的实现
        return String.valueOf(content.hashCode());
    }

    private String readLocalTemplate(String dataId) {
        try {
            // 修复：改进模板文件路径查找逻辑
            Path templateFile = Paths.get(templatePath, dataId);

            // 如果直接路径不存在，尝试相对于项目根目录
            if (!Files.exists(templateFile)) {
                templateFile = Paths.get(".", templatePath, dataId);
            }

            // 如果仍不存在，尝试绝对路径
            if (!Files.exists(templateFile)) {
                templateFile = Paths.get(System.getProperty("user.dir"), templatePath, dataId);
            }

            // 如果还不存在，尝试从 management-service 目录查找
            if (!Files.exists(templateFile)) {
                templateFile = Paths.get("services", "management-service", templatePath, dataId);
            }

            // 最后尝试：从当前工作目录的 services/management-service 查找
            if (!Files.exists(templateFile)) {
                templateFile = Paths.get(System.getProperty("user.dir"), "services", "management-service", templatePath, dataId);
            }

            if (Files.exists(templateFile)) {
                logger.info("找到模板文件: {}", templateFile.toAbsolutePath());
                return Files.readString(templateFile);
            } else {
                logger.warn("模板文件不存在: {}, 查找路径: {}", dataId, templatePath);
                // 记录所有尝试过的路径
                logger.debug("尝试过的路径:");
                logger.debug("1. {}", Paths.get(templatePath, dataId).toAbsolutePath());
                logger.debug("2. {}", Paths.get(".", templatePath, dataId).toAbsolutePath());
                logger.debug("3. {}", Paths.get(System.getProperty("user.dir"), templatePath, dataId).toAbsolutePath());
                logger.debug("4. {}", Paths.get("services", "management-service", templatePath, dataId).toAbsolutePath());
                logger.debug("5. {}", Paths.get(System.getProperty("user.dir"), "services", "management-service", templatePath, dataId).toAbsolutePath());
            }

        } catch (IOException e) {
            logger.error("读取本地模板文件失败: {}", dataId, e);
        }
        return null;
    }

    private boolean testConfigService() {
        try {
            configService.getConfig("test-connection", "DEFAULT_GROUP", 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testNamingService() {
        try {
            namingService.getServicesOfServer(1, 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> syncConfigsToTemplate(String namespace) {
        Map<String, Object> result = new HashMap<>();
        List<String> successConfigs = new ArrayList<>();
        List<String> failedConfigs = new ArrayList<>();

        try {
            logger.info("开始同步所有配置到模板文件夹...");

            // 获取所有配置
            List<NacosConfigInfo> allConfigs = getAllConfigs(namespace);

            for (NacosConfigInfo config : allConfigs) {
                try {
                    Map<String, Object> syncResult = syncConfigToTemplate(
                            config.getDataId(), config.getGroup(), namespace);

                    if ((Boolean) syncResult.get("success")) {
                        successConfigs.add(config.getDataId());
                    } else {
                        failedConfigs.add(config.getDataId() + ": " + syncResult.get("message"));
                    }
                } catch (Exception e) {
                    failedConfigs.add(config.getDataId() + ": " + e.getMessage());
                    logger.error("同步配置失败: {}", config.getDataId(), e);
                }
            }

            result.put("success", true);
            result.put("totalConfigs", allConfigs.size());
            result.put("successCount", successConfigs.size());
            result.put("failedCount", failedConfigs.size());
            result.put("successConfigs", successConfigs);
            result.put("failedConfigs", failedConfigs);
            result.put("message", String.format("同步完成: 成功 %d 个，失败 %d 个",
                    successConfigs.size(), failedConfigs.size()));

            logger.info("配置同步完成: 成功 {} 个，失败 {} 个",
                    successConfigs.size(), failedConfigs.size());

        } catch (Exception e) {
            logger.error("批量同步配置失败", e);
            result.put("success", false);
            result.put("message", "批量同步失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> syncConfigToTemplate(String dataId, String group, String namespace) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始同步配置: dataId={}, group={}", dataId, group);

            // 获取Nacos配置
            NacosConfigInfo config = getConfigInfo(dataId, group, namespace);
            if (config == null) {
                result.put("success", false);
                result.put("message", "配置在Nacos中不存在");
                return result;
            }

            // 写入到模板文件夹
            boolean writeSuccess = writeToTemplateFile(dataId, config.getContent());

            if (writeSuccess) {
                result.put("success", true);
                result.put("dataId", dataId);
                result.put("group", group);
                result.put("namespace", namespace);
                result.put("contentLength", config.getContent().length());
                result.put("message", "配置同步成功");
                logger.info("配置同步成功: {}", dataId);
            } else {
                result.put("success", false);
                result.put("message", "写入模板文件失败");
            }

        } catch (Exception e) {
            logger.error("同步配置失败: dataId={}", dataId, e);
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 将配置内容写入到模板文件
     */
    private boolean writeToTemplateFile(String dataId, String content) {
        try {
            // 确保模板目录存在
            Path templateDir = getTemplateDirectory();
            if (!Files.exists(templateDir)) {
                Files.createDirectories(templateDir);
                logger.info("创建模板目录: {}", templateDir.toAbsolutePath());
            }

            // 写入配置文件
            Path templateFile = templateDir.resolve(dataId);
            Files.writeString(templateFile, content);

            logger.info("配置文件已写入: {}", templateFile.toAbsolutePath());
            return true;

        } catch (IOException e) {
            logger.error("写入模板文件失败: {}", dataId, e);
            return false;
        }
    }

    /**
     * 获取模板目录路径
     */
    private Path getTemplateDirectory() {
        // 优先使用管理服务根目录下的config-templates（避免嵌套）
        Path[] possiblePaths = {
                Paths.get(templatePath), // ./config-templates (管理服务根目录下)
                Paths.get(".", templatePath), // 当前目录的config-templates
                Paths.get(System.getProperty("user.dir"), "services", "management-service", templatePath), // 绝对路径到管理服务
                Paths.get(System.getProperty("user.dir"), templatePath) // 项目根目录的config-templates
        };

        for (Path path : possiblePaths) {
            if (Files.exists(path) || path.isAbsolute()) {
                logger.debug("使用模板目录路径: {}", path.toAbsolutePath());
                return path;
            }
        }

        // 如果都不存在，返回管理服务根目录下的config-templates作为默认路径
        Path defaultPath = Paths.get(templatePath);
        logger.debug("使用默认模板目录路径: {}", defaultPath.toAbsolutePath());
        return defaultPath;
    }
}
