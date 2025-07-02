package com.cloudDemo.gateway.controller;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Nacos信息查询控制器
 * 提供实时的Nacos服务和配置信息查询功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/nacos")
public class NacosInfoController {

    @Autowired
    private NamingService namingService;

    @Autowired
    private ConfigService configService;

    // 从配置文件读取Nacos服务器地址
    @Value("${spring.cloud.nacos.discovery.server-addr:localhost:8848}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:public}")
    private String nacosNamespace;

    /**
     * 获取所有注册的服务信息
     */
    @GetMapping("/services")
    public Map<String, Object> getServices(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "100") int pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取服务列表
            ListView<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize);
            List<String> serviceNames = serviceList.getData();

            Map<String, Object> services = new HashMap<>();

            for (String serviceName : serviceNames) {
                try {
                    // 获取每个服务的实例信息
                    List<Instance> instances = namingService.getAllInstances(serviceName);

                    List<Map<String, Object>> instanceList = new ArrayList<>();
                    for (Instance instance : instances) {
                        Map<String, Object> instanceInfo = new HashMap<>();
                        instanceInfo.put("ip", instance.getIp());
                        instanceInfo.put("port", instance.getPort());
                        instanceInfo.put("healthy", instance.isHealthy());
                        instanceInfo.put("enabled", instance.isEnabled());
                        instanceInfo.put("weight", instance.getWeight());
                        instanceInfo.put("metadata", instance.getMetadata());
                        instanceList.add(instanceInfo);
                    }

                    Map<String, Object> serviceInfo = new HashMap<>();
                    serviceInfo.put("instances", instanceList);
                    serviceInfo.put("instanceCount", instances.size());

                    services.put(serviceName, serviceInfo);

                } catch (NacosException e) {
                    log.warn("Failed to get instances for service: {}, error: {}", serviceName, e.getMessage());
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("error", "Failed to get instances: " + e.getMessage());
                    services.put(serviceName, errorInfo);
                }
            }

            result.put("services", services);
            result.put("totalServices", serviceList.getCount());
            result.put("success", true);

        } catch (NacosException e) {
            log.error("Failed to get service list", e);
            result.put("success", false);
            result.put("error", "Failed to get service list: " + e.getMessage());
        }

        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取指定服务的详细信息
     */
    @GetMapping("/services/{serviceName}")
    public Map<String, Object> getServiceDetail(@PathVariable String serviceName) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Instance> instances = namingService.getAllInstances(serviceName);

            List<Map<String, Object>> instanceList = new ArrayList<>();
            for (Instance instance : instances) {
                Map<String, Object> instanceInfo = new HashMap<>();
                instanceInfo.put("instanceId", instance.getInstanceId());
                instanceInfo.put("ip", instance.getIp());
                instanceInfo.put("port", instance.getPort());
                instanceInfo.put("healthy", instance.isHealthy());
                instanceInfo.put("enabled", instance.isEnabled());
                instanceInfo.put("weight", instance.getWeight());
                instanceInfo.put("clusterName", instance.getClusterName());
                instanceInfo.put("serviceName", instance.getServiceName());
                instanceInfo.put("metadata", instance.getMetadata());
                instanceList.add(instanceInfo);
            }

            result.put("serviceName", serviceName);
            result.put("instances", instanceList);
            result.put("instanceCount", instances.size());
            result.put("success", true);

        } catch (NacosException e) {
            log.error("Failed to get service detail for: {}", serviceName, e);
            result.put("success", false);
            result.put("error", "Failed to get service detail: " + e.getMessage());
        }

        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取配置信息
     */
    @GetMapping("/configs")
    public Map<String, Object> getConfigs(@RequestParam(required = false) String group) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 预定义的一些常用配置
            List<Map<String, String>> configList = Arrays.asList(
                new HashMap<String, String>(){{
                    put("dataId", "user-service.properties");
                    put("group", "DEFAULT_GROUP");
                }},
                new HashMap<String, String>(){{
                    put("dataId", "order-service.properties");
                    put("group", "DEFAULT_GROUP");
                }},
                new HashMap<String, String>(){{
                    put("dataId", "gateway-routes.yml");
                    put("group", "GATEWAY_GROUP");
                }},
                new HashMap<String, String>(){{
                    put("dataId", "common-config.yml");
                    put("group", "COMMON_GROUP");
                }}
            );

            Map<String, Object> configs = new HashMap<>();

            for (Map<String, String> configItem : configList) {
                String dataId = configItem.get("dataId");
                String configGroup = configItem.get("group");

                // 如果指定了group参数，只返回该group的配置
                if (group != null && !group.equals(configGroup)) {
                    continue;
                }

                try {
                    String content = configService.getConfig(dataId, configGroup, 3000);

                    Map<String, Object> configInfo = new HashMap<>();
                    configInfo.put("dataId", dataId);
                    configInfo.put("group", configGroup);
                    configInfo.put("content", content);
                    configInfo.put("contentLength", content != null ? content.length() : 0);

                    String configKey = configGroup + "/" + dataId;
                    configs.put(configKey, configInfo);

                } catch (NacosException e) {
                    log.warn("Failed to get config: {} from group: {}, error: {}",
                            dataId, configGroup, e.getMessage());

                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("dataId", dataId);
                    errorInfo.put("group", configGroup);
                    errorInfo.put("error", "Failed to get config: " + e.getMessage());

                    String configKey = configGroup + "/" + dataId;
                    configs.put(configKey, errorInfo);
                }
            }

            result.put("configs", configs);
            result.put("success", true);

        } catch (Exception e) {
            log.error("Failed to get configs", e);
            result.put("success", false);
            result.put("error", "Failed to get configs: " + e.getMessage());
        }

        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取指定配置的详细信息
     */
    @GetMapping("/configs/{group}/{dataId}")
    public Map<String, Object> getConfigDetail(@PathVariable String group,
                                              @PathVariable String dataId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String content = configService.getConfig(dataId, group, 3000);

            result.put("dataId", dataId);
            result.put("group", group);
            result.put("content", content);
            result.put("contentLength", content != null ? content.length() : 0);
            result.put("success", true);

        } catch (NacosException e) {
            log.error("Failed to get config detail: {}/{}", group, dataId, e);
            result.put("success", false);
            result.put("error", "Failed to get config: " + e.getMessage());
        }

        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取完整的Nacos状态快照
     */
    @GetMapping("/snapshot")
    public Map<String, Object> getFullSnapshot() {
        Map<String, Object> result = new HashMap<>();

        // 获取服务信息
        Map<String, Object> servicesResult = getServices(1, 100);

        // 获取配置信息
        Map<String, Object> configsResult = getConfigs(null);

        // 获取Nacos连接信息
        Map<String, Object> nacosInfo = new HashMap<>();
        nacosInfo.put("serverAddr", nacosServerAddr);
        nacosInfo.put("namespace", nacosNamespace);

        result.put("services", servicesResult.get("services"));
        result.put("configs", configsResult.get("configs"));
        result.put("nacosInfo", nacosInfo);
        result.put("timestamp", System.currentTimeMillis());
        result.put("success", true);

        return result;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试Nacos连接
            namingService.getServicesOfServer(1, 1);

            result.put("status", "UP");
            result.put("nacos", "Connected");
            result.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("nacos", "Disconnected");
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }

        return result;
    }
}
