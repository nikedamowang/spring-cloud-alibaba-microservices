package com.cloudDemo.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Nacos 配置信息 DTO
 */
public class NacosConfigInfo {

    private String dataId;
    private String group;
    private String namespace;
    private String content;
    private String type;
    private String md5;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    private String description;

    // 构造方法
    public NacosConfigInfo() {}

    public NacosConfigInfo(String dataId, String group, String namespace) {
        this.dataId = dataId;
        this.group = group;
        this.namespace = namespace;
    }

    // Getter 和 Setter 方法
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NacosConfigInfo{" +
                "dataId='" + dataId + '\'' +
                ", group='" + group + '\'' +
                ", namespace='" + namespace + '\'' +
                ", type='" + type + '\'' +
                ", md5='" + md5 + '\'' +
                ", lastModified=" + lastModified +
                ", description='" + description + '\'' +
                '}';
    }
}
