package com.cloudDemo.orderservice.service.impl;

import com.cloudDemo.orderservice.service.OrderIdempotentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 订单幂等性服务实现类
 * 基于Redis实现分布式幂等性控制
 */
@Slf4j
@Service
public class OrderIdempotentServiceImpl implements OrderIdempotentService {

    // Redis键前缀
    private static final String IDEMPOTENT_KEY_PREFIX = "order:idempotent:";
    private static final String TOKEN_KEY_PREFIX = "order:token:";
    // 过期时间配置
    private static final int IDEMPOTENT_EXPIRE_MINUTES = 30; // 幂等记录过期时间30分钟
    private static final int TOKEN_EXPIRE_MINUTES = 10; // 令牌过期时间10分钟
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IdempotentResult checkIdempotent(Long userId, String productId, String amount, String clientToken) {
        try {
            // 1. 验证客户端令牌有效性
            if (!validateClientToken(userId, clientToken)) {
                log.warn("用户 {} 提供的幂等令牌无效: {}", userId, clientToken);
                return new IdempotentResult(true, null, "幂等令牌无效或已过期");
            }

            // 2. 生成幂等键
            String idempotentKey = generateIdempotentKey(userId, productId, amount, clientToken);

            // 3. 检查是否存在重复请求
            String existingOrderNo = (String) redisTemplate.opsForValue().get(idempotentKey);

            if (existingOrderNo != null) {
                log.info("检测到重复下单请求，用户: {}, 商品: {}, 已存在订单: {}", userId, productId, existingOrderNo);
                return IdempotentResult.duplicate(existingOrderNo);
            }

            // 4. 幂等性检查通过
            log.debug("幂等性检查通过，用户: {}, 商品: {}, 令牌: {}", userId, productId, clientToken);
            return IdempotentResult.success();

        } catch (Exception e) {
            log.error("幂等性检查失败", e);
            return new IdempotentResult(true, null, "幂等性检查失败: " + e.getMessage());
        }
    }

    @Override
    public void markOrderCreated(Long userId, String productId, String amount, String clientToken, String orderNo) {
        try {
            // 生成幂等键
            String idempotentKey = generateIdempotentKey(userId, productId, amount, clientToken);

            // 记录订单创建结果，设置过期时间
            redisTemplate.opsForValue().set(idempotentKey, orderNo, IDEMPOTENT_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 使令牌失效（防止重复使用）
            String tokenKey = TOKEN_KEY_PREFIX + userId + ":" + clientToken;
            redisTemplate.delete(tokenKey);

            log.info("订单创建幂等性记录已保存，用户: {}, 订单: {}, 幂等键: {}", userId, orderNo, idempotentKey);

        } catch (Exception e) {
            log.error("保存订单幂等性记录失败", e);
        }
    }

    @Override
    public String generateIdempotentToken(Long userId) {
        try {
            // 生成UUID作为基础令牌
            String baseToken = UUID.randomUUID().toString().replaceAll("-", "");

            // 添加时间戳和用户ID，增强唯一性
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String rawToken = userId + "_" + timestamp + "_" + baseToken;

            // MD5哈希生成最终令牌
            String token = md5Hash(rawToken);

            // 将令牌存储到Redis，设置过期时间
            String tokenKey = TOKEN_KEY_PREFIX + userId + ":" + token;
            redisTemplate.opsForValue().set(tokenKey, "valid", TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.debug("为用户 {} 生成幂等令牌: {}", userId, token);
            return token;

        } catch (Exception e) {
            log.error("生成幂等令牌失败", e);
            throw new RuntimeException("生成幂等令牌失败", e);
        }
    }

    @Override
    public void cleanExpiredIdempotentRecords() {
        // Redis的过期机制会自动清理过期键，这里可以实现额外的清理逻辑
        log.info("幂等性记录清理任务执行完成（Redis自动过期机制）");
    }

    /**
     * 生成幂等键
     * 基于用户ID、商品ID、金额、令牌生成唯一的幂等键
     */
    private String generateIdempotentKey(Long userId, String productId, String amount, String clientToken) {
        String rawKey = userId + ":" + productId + ":" + amount + ":" + clientToken;
        String hashedKey = md5Hash(rawKey);
        return IDEMPOTENT_KEY_PREFIX + hashedKey;
    }

    /**
     * 验证客户端令牌有效性
     */
    private boolean validateClientToken(Long userId, String clientToken) {
        if (clientToken == null || clientToken.trim().isEmpty()) {
            return false;
        }

        String tokenKey = TOKEN_KEY_PREFIX + userId + ":" + clientToken;
        String tokenValue = (String) redisTemplate.opsForValue().get(tokenKey);
        return "valid".equals(tokenValue);
    }

    /**
     * MD5哈希工具方法
     */
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }
}
