package com.cloudDemo.management.service;

import com.cloudDemo.management.dto.NacosConfigInfo;
import com.cloudDemo.management.dto.NacosServiceInstance;

import java.util.List;
import java.util.Map;

/**
 * Nacos 管理服务接口
 * 专供 AI 调用，用于获取 Nacos 配置和服务信息
 */
public interface NacosManagementService {

    /**
     * 获取指定配置的详细信息
     *
     * @param dataId    配置ID
     * @param group     配置组
     * @param namespace 命名空间
     * @return 配置信息
     */
    NacosConfigInfo getConfigInfo(String dataId, String group, String namespace);

    /**
     * 获取所有配置列表
     *
     * @param namespace 命名空间
     * @return 配置列表
     */
    List<NacosConfigInfo> getAllConfigs(String namespace);

    /**
     * 搜索配置
     *
     * @param keyword   关键词
     * @param namespace 命名空间
     * @return 匹配的配置列表
     */
    List<NacosConfigInfo> searchConfigs(String keyword, String namespace);

    /**
     * 获取指定服务的所有实例
     *
     * @param serviceName 服务名
     * @param groupName   组名
     * @param namespace   命名空间
     * @return 服务实例列表
     */
    List<NacosServiceInstance> getServiceInstances(String serviceName, String groupName, String namespace);

    /**
     * 获取所有服务列表
     *
     * @param namespace 命名空间
     * @return 服务列表
     */
    List<String> getAllServices(String namespace);

    /**
     * 获取服务健康状态
     *
     * @param serviceName 服务名
     * @param namespace   命名空间
     * @return 健康状态信息
     */
    Map<String, Object> getServiceHealth(String serviceName, String namespace);

    /**
     * 比较配置与本地模板的差异
     *
     * @param dataId    配置ID
     * @param group     配置组
     * @param namespace 命名空间
     * @return 差异信息
     */
    Map<String, Object> compareConfigWithTemplate(String dataId, String group, String namespace);

    /**
     * 获取 Nacos 服务器状态
     *
     * @return 服务器状态信息
     */
    Map<String, Object> getNacosServerStatus();
}
