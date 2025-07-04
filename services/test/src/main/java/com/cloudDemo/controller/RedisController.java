package com.cloudDemo.controller;

import com.cloudDemo.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisService redisService;

    /**
     * 设置键值对
     */
    @PostMapping("/set")
    public Map<String, Object> setValue(@RequestParam String key, @RequestParam String value) {
        Map<String, Object> result = new HashMap<>();
        try {
            redisService.set(key, value);
            result.put("success", true);
            result.put("message", "值设置成功");
            result.put("key", key);
            result.put("value", value);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 设置带过期时间的键值对
     */
    @PostMapping("/setex")
    public Map<String, Object> setValueWithExpire(@RequestParam String key,
                                                  @RequestParam String value,
                                                  @RequestParam long seconds) {
        Map<String, Object> result = new HashMap<>();
        try {
            redisService.set(key, value, seconds, TimeUnit.SECONDS);
            result.put("success", true);
            result.put("message", "值设置成功，过期时间: " + seconds + " 秒");
            result.put("key", key);
            result.put("value", value);
            result.put("expire", seconds);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取值
     */
    @GetMapping("/get")
    public Map<String, Object> getValue(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Object value = redisService.get(key);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
            result.put("exists", value != null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除键
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteKey(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Boolean deleted = redisService.delete(key);
            result.put("success", true);
            result.put("key", key);
            result.put("deleted", deleted);
            result.put("message", deleted ? "删除成功" : "键不存在");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查键是否存在
     */
    @GetMapping("/exists")
    public Map<String, Object> keyExists(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Boolean exists = redisService.hasKey(key);
            result.put("success", true);
            result.put("key", key);
            result.put("exists", exists);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 递增计数器
     */
    @PostMapping("/increment")
    public Map<String, Object> increment(@RequestParam String key,
                                         @RequestParam(defaultValue = "1") long delta) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long value = redisService.increment(key, delta);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
            result.put("delta", delta);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "递增失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 递减计数器
     */
    @PostMapping("/decrement")
    public Map<String, Object> decrement(@RequestParam String key,
                                         @RequestParam(defaultValue = "1") long delta) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long value = redisService.decrement(key, delta);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
            result.put("delta", delta);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "递减失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取键的过期时间
     */
    @GetMapping("/ttl")
    public Map<String, Object> getTimeToLive(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long ttl = redisService.getExpire(key);
            result.put("success", true);
            result.put("key", key);
            result.put("ttl", ttl);
            result.put("message", ttl > 0 ? "剩余 " + ttl + " 秒" : (ttl == -1 ? "永不过期" : "键不存在"));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取过期时间失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * Redis连接测试
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 简单的连接测试
            String testKey = "ping:test:" + System.currentTimeMillis();
            redisService.set(testKey, "pong");
            Object value = redisService.get(testKey);
            redisService.delete(testKey);

            result.put("success", true);
            result.put("message", "Redis连接正常");
            result.put("response", value);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Redis连接失败: " + e.getMessage());
        }
        return result;
    }
}
