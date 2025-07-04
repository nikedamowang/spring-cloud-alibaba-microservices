package com.cloudDemo.test;

import com.cloudDemo.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void testRedisBasicOperations() {
        // 测试基本的set和get操作
        String key = "test:string";
        String value = "Hello Redis!";

        redisService.set(key, value);
        Object result = redisService.get(key);

        assertEquals(value, result);
        System.out.println("基本操作测试通过: " + result);

        // 清理
        redisService.delete(key);
    }

    @Test
    public void testRedisWithExpiration() {
        // 测试带过期时间的操作
        String key = "test:expire";
        String value = "Will expire soon";

        redisService.set(key, value, 5, TimeUnit.SECONDS);

        // 验证键存在
        assertTrue(redisService.hasKey(key));
        System.out.println("过期时间测试: 键存在，过期时间为 " + redisService.getExpire(key) + " 秒");

        // 等待一段时间后验证（这里只验证逻辑，不真正等待）
        Long expireTime = redisService.getExpire(key);
        assertTrue(expireTime > 0 && expireTime <= 5);

        // 清理
        redisService.delete(key);
    }

    @Test
    public void testRedisIncrement() {
        // 测试计数器功能
        String key = "test:counter";

        // 初始递增
        Long count1 = redisService.increment(key);
        assertEquals(1L, count1);

        // 递增指定值
        Long count2 = redisService.increment(key, 5);
        assertEquals(6L, count2);

        // 递减
        Long count3 = redisService.decrement(key);
        assertEquals(5L, count3);

        // 递减指定值
        Long count4 = redisService.decrement(key, 2);
        assertEquals(3L, count4);

        System.out.println("计数器测试通过，最终值: " + count4);

        // 清理
        redisService.delete(key);
    }

    @Test
    public void testRedisObjectStorage() {
        // 测试对象存储
        String key = "test:object";

        // 创建一个测试对象
        TestObject testObj = new TestObject("张三", 25, "developer");

        redisService.set(key, testObj);
        Object result = redisService.get(key);

        assertNotNull(result);
        System.out.println("对象存储测试通过: " + result);

        // 清理
        redisService.delete(key);
    }

    @Test
    public void testRedisKeyOperations() {
        // 测试键操作
        String key = "test:key:operations";
        String value = "test value";

        // 设置值
        redisService.set(key, value);

        // 检查键是否存在
        assertTrue(redisService.hasKey(key));

        // 设置过期时间
        Boolean expireResult = redisService.expire(key, 10, TimeUnit.SECONDS);
        assertTrue(expireResult);

        // 检查过期时间
        Long expireTime = redisService.getExpire(key);
        assertTrue(expireTime > 0);
        System.out.println("键操作测试通过，过期时间: " + expireTime + " 秒");

        // 删除键
        Boolean deleteResult = redisService.delete(key);
        assertTrue(deleteResult);

        // 验证键已删除
        assertFalse(redisService.hasKey(key));
        System.out.println("键删除测试通过");
    }

    @Test
    public void testRedisConnectionAndPerformance() {
        // 测试连接和性能
        long startTime = System.currentTimeMillis();

        // 批量操作测试
        for (int i = 0; i < 100; i++) {
            String key = "test:batch:" + i;
            redisService.set(key, "value" + i);
        }

        // 验证数据
        int count = 0;
        for (int i = 0; i < 100; i++) {
            String key = "test:batch:" + i;
            if (redisService.hasKey(key)) {
                count++;
            }
        }

        assertEquals(100, count);

        // 清理批量数据
        for (int i = 0; i < 100; i++) {
            String key = "test:batch:" + i;
            redisService.delete(key);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("批量操作测试通过，耗时: " + (endTime - startTime) + " ms");
    }

    // 测试用的内部类
    public static class TestObject {
        private String name;
        private Integer age;
        private String profession;

        public TestObject() {
        }

        public TestObject(String name, Integer age, String profession) {
            this.name = name;
            this.age = age;
            this.profession = profession;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getProfession() {
            return profession;
        }

        public void setProfession(String profession) {
            this.profession = profession;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', age=" + age + ", profession='" + profession + "'}";
        }
    }
}
