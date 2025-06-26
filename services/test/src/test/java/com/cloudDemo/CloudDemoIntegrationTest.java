package com.cloudDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class CloudDemoIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @org.junit.jupiter.api.Test
    public void testMySQLConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[MySQL测试] 数据库连接成功: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("[MySQL测试] 数据库连接失败: " + e.getMessage());
            throw e;
        }
    }

    @org.junit.jupiter.api.Test
    public void testRedisConnection() {
        if (redisTemplate == null) {
            System.err.println("[Redis测试] 未找到 RedisTemplate Bean，请检查依赖和配置");
            return;
        }
        try {
            redisTemplate.opsForValue().set("test-key", "test-value");
            String value = redisTemplate.opsForValue().get("test-key");
            System.out.println("[Redis测试] 连接成功，写入并读取的值: " + value);
        } catch (Exception e) {
            System.err.println("[Redis测试] 连接失败: " + e.getMessage());
            throw e;
        }
    }
}
