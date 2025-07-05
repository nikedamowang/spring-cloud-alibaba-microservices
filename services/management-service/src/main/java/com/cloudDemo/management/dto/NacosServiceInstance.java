package com.cloudDemo.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Nacos 服务实例信息 DTO
 */
public class NacosServiceInstance {

    private String serviceName;
    private String groupName;
    private String namespaceId;
    private String ip;
    private int port;
    private double weight;
    private boolean healthy;
    private boolean enabled;
    private boolean ephemeral;
    private String clusterName;
    private Map<String, String> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastBeat;

    // 构造方法
    public NacosServiceInstance() {}

    // Getter 和 Setter 方法
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getLastBeat() {
        return lastBeat;
    }

    public void setLastBeat(LocalDateTime lastBeat) {
        this.lastBeat = lastBeat;
    }

    @Override
    public String toString() {
        return "NacosServiceInstance{" +
                "serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", healthy=" + healthy +
                ", enabled=" + enabled +
                '}';
    }
}
