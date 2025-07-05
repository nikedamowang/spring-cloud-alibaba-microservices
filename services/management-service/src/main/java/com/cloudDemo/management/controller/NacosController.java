package com.cloudDemo.management.controller;

import com.cloudDemo.management.dto.NacosConfigInfo;
import com.cloudDemo.management.dto.NacosServiceInstance;
import com.cloudDemo.management.service.NacosManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos 管理控制器
 * 专供 AI 调用的 REST API 接口
 */
@RestController
@RequestMapping("/api/nacos")
public class NacosController {

    private final NacosManagementService nacosManagementService;

    public NacosController(NacosManagementService nacosManagementService) {
        this.nacosManagementService = nacosManagementService;
    }

    /**
     * 获取指定配置信息
     * AI 可以调用此接口获取特定配置的详细信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(
            @RequestParam String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            NacosConfigInfo config = nacosManagementService.getConfigInfo(dataId, group, namespace);
            if (config != null) {
                response.put("success", true);
                response.put("data", config);
                response.put("message", "配置获取成功");
            } else {
                response.put("success", false);
                response.put("message", "配置不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取配置失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有配置列表
     * AI 可以调用此接口查看所有可用的配置
     */
    @GetMapping("/configs")
    public ResponseEntity<Map<String, Object>> getAllConfigs(
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<NacosConfigInfo> configs = nacosManagementService.getAllConfigs(namespace);
            response.put("success", true);
            response.put("data", configs);
            response.put("count", configs.size());
            response.put("message", "配置列表获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取配置列表失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 搜索配置
     * AI 可以根据关键词搜索相关配置
     */
    @GetMapping("/configs/search")
    public ResponseEntity<Map<String, Object>> searchConfigs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<NacosConfigInfo> configs = nacosManagementService.searchConfigs(keyword, namespace);
            response.put("success", true);
            response.put("data", configs);
            response.put("count", configs.size());
            response.put("keyword", keyword);
            response.put("message", "配置搜索完成");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索配置失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取服务实例信息
     * AI 可以调用此接口查看服务的运行状态
     */
    @GetMapping("/service/instances")
    public ResponseEntity<Map<String, Object>> getServiceInstances(
            @RequestParam String serviceName,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String groupName,
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<NacosServiceInstance> instances = nacosManagementService.getServiceInstances(serviceName, groupName, namespace);
            response.put("success", true);
            response.put("data", instances);
            response.put("count", instances.size());
            response.put("serviceName", serviceName);
            response.put("message", "服务实例获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取服务实例失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有服务列表
     * AI 可以调用此接口查看系统中注册的所有服务
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getAllServices(
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<String> services = nacosManagementService.getAllServices(namespace);
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());
            response.put("message", "服务列表获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取服务列表失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取服务健康状态
     * AI 可以调用此接口检查服务的健康状况
     */
    @GetMapping("/service/health")
    public ResponseEntity<Map<String, Object>> getServiceHealth(
            @RequestParam String serviceName,
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> healthInfo = nacosManagementService.getServiceHealth(serviceName, namespace);
            response.put("success", true);
            response.put("data", healthInfo);
            response.put("message", "服务健康状态获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取服务健康状态失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 比较配置与本地模板的差异
     * AI 可以调用此接口检查配置是否与预期一致
     */
    @GetMapping("/config/compare")
    public ResponseEntity<Map<String, Object>> compareConfig(
            @RequestParam String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
            @RequestParam(defaultValue = "") String namespace) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> compareResult = nacosManagementService.compareConfigWithTemplate(dataId, group, namespace);
            response.put("success", true);
            response.put("data", compareResult);
            response.put("message", "配置比较完成");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "配置比较失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取 Nacos 服务器状态
     * AI 可以调用此接口检查 Nacos 服务器的运��状态
     */
    @GetMapping("/server/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> serverStatus = nacosManagementService.getNacosServerStatus();
            response.put("success", true);
            response.put("data", serverStatus);
            response.put("message", "Nacos 服务器状态获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取服务器状态失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取 API 使用说明
     * AI 可以调用此接口了解所有可用的功能
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getApiHelp() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> apis = new HashMap<>();

        apis.put("GET /api/nacos/config", "获取指定配置信息，参数：dataId, group, namespace");
        apis.put("GET /api/nacos/configs", "获取所有配置列表，参数：namespace");
        apis.put("GET /api/nacos/configs/search", "搜索配置，参数：keyword, namespace");
        apis.put("GET /api/nacos/service/instances", "获取服务实例，参数：serviceName, groupName, namespace");
        apis.put("GET /api/nacos/services", "获取所有服务列表，参数：namespace");
        apis.put("GET /api/nacos/service/health", "获取服务健康状态，参数：serviceName, namespace");
        apis.put("GET /api/nacos/config/compare", "比较配置与模板，参数：dataId, group, namespace");
        apis.put("GET /api/nacos/server/status", "获取Nacos服务器状态");

        response.put("success", true);
        response.put("apis", apis);
        response.put("description", "Nacos管理服务API，专供AI调用，仅提供只读功能");
        response.put("note", "所有API均为只读操作，不提供修改功能以确保系统安全");

        return ResponseEntity.ok(response);
    }
}
