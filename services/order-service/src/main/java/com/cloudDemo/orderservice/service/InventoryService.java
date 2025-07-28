package com.cloudDemo.orderservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.orderservice.entity.ProductInventory;
import com.cloudDemo.orderservice.mapper.ProductInventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class InventoryService {

    private static final String INVENTORY_CACHE_KEY = "inventory:";
    private static final long CACHE_EXPIRE_TIME = 300; // 5分钟缓存
    @Autowired
    private ProductInventoryMapper inventoryMapper;
    @Autowired
    private DistributedLockService lockService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 预扣库存（分布式锁保护）
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 预扣是否成功
     */
    public boolean reserveStock(String productId, Integer quantity) {
        String lockKey = lockService.getInventoryLockKey(productId);

        return lockService.executeWithLock(lockKey, () -> {
            try {
                // 1. 查询当前库存信息
                ProductInventory inventory = getInventoryFromCacheOrDB(productId);
                if (inventory == null) {
                    log.warn("商品不存在: {}", productId);
                    throw new RuntimeException("商品不存在");
                }

                // 2. 检查库存是否充足
                if (inventory.getAvailableStock() < quantity) {
                    log.warn("库存不足，商品: {}, 可用库存: {}, 需要: {}",
                            productId, inventory.getAvailableStock(), quantity);
                    throw new RuntimeException("库存不足");
                }

                // 3. 使用乐观锁更新库存
                int updateCount = inventoryMapper.reserveStockWithVersion(
                        productId, quantity, inventory.getVersion());

                if (updateCount == 0) {
                    log.warn("库存更新失败，可能是并发冲突，商品: {}", productId);
                    throw new RuntimeException("库存更新失败，请重试");
                }

                // 4. 清除缓存，确保数据一致性
                clearInventoryCache(productId);

                log.info("库存预扣成功，商品: {}, 数量: {}", productId, quantity);
                return true;

            } catch (Exception e) {
                log.error("库存预扣失败，商品: {}, 数量: {}", productId, quantity, e);
                throw new RuntimeException("库存预扣失败: " + e.getMessage());
            }
        });
    }

    /**
     * 确认库存扣减（支付成功后调用）
     *
     * @param productId 商品ID
     * @param quantity  确认数量
     * @return 确认是否成功
     */
    @Transactional
    public boolean confirmStockReduction(String productId, Integer quantity) {
        String lockKey = lockService.getInventoryLockKey(productId);

        return lockService.executeWithLock(lockKey, () -> {
            try {
                ProductInventory inventory = getInventoryFromDB(productId);
                if (inventory == null) {
                    throw new RuntimeException("商品不存在");
                }

                // 检查预扣库存是否充足
                if (inventory.getReservedStock() < quantity) {
                    log.warn("预扣库存不足，商品: {}, 预扣库存: {}, 需要确认: {}",
                            productId, inventory.getReservedStock(), quantity);
                    throw new RuntimeException("预扣库存不足");
                }

                int updateCount = inventoryMapper.confirmStockReduction(
                        productId, quantity, inventory.getVersion());

                if (updateCount == 0) {
                    throw new RuntimeException("库存确认失败，请重试");
                }

                clearInventoryCache(productId);
                log.info("库存确认扣减成功，商品: {}, 数量: {}", productId, quantity);
                return true;

            } catch (Exception e) {
                log.error("库存确认扣减失败，商品: {}, 数量: {}", productId, quantity, e);
                throw new RuntimeException("库存确认失败: " + e.getMessage());
            }
        });
    }

    /**
     * 释放预扣库存（订单取消时调用）
     *
     * @param productId 商品ID
     * @param quantity  释放数量
     * @return 释放是否成功
     */
    @Transactional
    public boolean releaseStock(String productId, Integer quantity) {
        String lockKey = lockService.getInventoryLockKey(productId);

        return lockService.executeWithLock(lockKey, () -> {
            try {
                ProductInventory inventory = getInventoryFromDB(productId);
                if (inventory == null) {
                    throw new RuntimeException("商品不存在");
                }

                int updateCount = inventoryMapper.releaseReservedStock(
                        productId, quantity, inventory.getVersion());

                if (updateCount == 0) {
                    throw new RuntimeException("库存释放失败，请重试");
                }

                clearInventoryCache(productId);
                log.info("预扣库存释放成功，商品: {}, 数量: {}", productId, quantity);
                return true;

            } catch (Exception e) {
                log.error("预扣库存释放失败，商品: {}, 数量: {}", productId, quantity, e);
                throw new RuntimeException("库存释放失败: " + e.getMessage());
            }
        });
    }

    /**
     * 查询库存信息（优先从缓存获取）
     */
    public ProductInventory getInventory(String productId) {
        return getInventoryFromCacheOrDB(productId);
    }

    /**
     * 初始化商品库存
     */
    @Transactional
    public ProductInventory initializeInventory(String productId, String productName, Integer totalStock) {

        // 【商品存在性验证】- 生产环境中应启用此验证
        // validateProductExists(productId);

        ProductInventory inventory = new ProductInventory();
        inventory.setProductId(productId);
        inventory.setProductName(productName);
        inventory.setTotalStock(totalStock);
        inventory.setAvailableStock(totalStock);
        inventory.setReservedStock(0);
        inventory.setSoldStock(0);
        inventory.setStatus("NORMAL");
        inventory.setCreateTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        inventory.setVersion(1);

        inventoryMapper.insert(inventory);

        // 缓存新创建的库存信息
        cacheInventory(inventory);

        log.info("初始化商品库存成功，商品: {}, 总库存: {}", productId, totalStock);
        return inventory;
    }

    /**
     * 验证商品是否真实存在（注释版本 - 生产环境启用）
     * 在真实系统中应该调用商品服务验证商品存在性
     */
    // private void validateProductExists(String productId) {
    //     try {
    //         // 方案1：通过Dubbo调用商品服务验证
    //         // ProductService productService = ...; // 注入商品服务
    //         // Product product = productService.getProductById(productId);
    //         // if (product == null) {
    //         //     throw new RuntimeException("商品不存在，无法初始化库存: " + productId);
    //         // }
    //
    //         // 方案2：通过HTTP调用商品服务API验证
    //         // RestTemplate restTemplate = ...; // 注入RestTemplate
    //         // String productServiceUrl = "http://product-service/api/products/" + productId;
    //         // try {
    //         //     ResponseEntity<Product> response = restTemplate.getForEntity(productServiceUrl, Product.class);
    //         //     if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
    //         //         throw new RuntimeException("商品不存在，无法初始化库存: " + productId);
    //         //     }
    //         // } catch (RestClientException e) {
    //         //     throw new RuntimeException("商品服务调用失败，无法验证商品: " + productId, e);
    //         // }
    //
    //         // 方案3：查询本地商品缓存验证(推荐)
    //         // String cacheKey = "product:" + productId;
    //         // Object cachedProduct = redisTemplate.opsForValue().get(cacheKey);
    //         // if (cachedProduct == null) {
    //         //     // 缓存未命中，调用商品服务并缓存结果
    //         //     Product product = productService.getProductById(productId);
    //         //     if (product == null) {
    //         //         throw new RuntimeException("商品不存在，无法初始化库存: " + productId);
    //         //     }
    //         //     redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(30));
    //         // }
    //
    //         log.info("商品存在性验证通过: {}", productId);
    //
    //     } catch (Exception e) {
    //         log.error("商品存在性验证失败: {}", productId, e);
    //         throw new RuntimeException("商品验证失败: " + e.getMessage());
    //     }
    // }

    /**
     * 从缓存或数据库获取库存信息
     */
    private ProductInventory getInventoryFromCacheOrDB(String productId) {
        // 先从缓存获取
        String cacheKey = INVENTORY_CACHE_KEY + productId;
        ProductInventory cached = (ProductInventory) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，从数据库获取
        ProductInventory inventory = getInventoryFromDB(productId);
        if (inventory != null) {
            cacheInventory(inventory);
        }

        return inventory;
    }

    /**
     * 从数据库获取库存信息
     */
    private ProductInventory getInventoryFromDB(String productId) {
        return inventoryMapper.selectOne(
                new QueryWrapper<ProductInventory>().eq("product_id", productId));
    }

    /**
     * 缓存库存信息
     */
    private void cacheInventory(ProductInventory inventory) {
        String cacheKey = INVENTORY_CACHE_KEY + inventory.getProductId();
        redisTemplate.opsForValue().set(cacheKey, inventory, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 清除库存缓存
     */
    private void clearInventoryCache(String productId) {
        String cacheKey = INVENTORY_CACHE_KEY + productId;
        redisTemplate.delete(cacheKey);
    }
}
