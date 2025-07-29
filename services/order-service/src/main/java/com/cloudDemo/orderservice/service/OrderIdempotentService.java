package com.cloudDemo.orderservice.service;

/**
 * 订单幂等性服务接口
 * 防止重复下单，确保订单创建的幂等性
 */
public interface OrderIdempotentService {

    /**
     * 检查订单创建的幂等性
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param amount      订单金额
     * @param clientToken 客户端提供的幂等令牌
     * @return 幂等性检查结果
     */
    IdempotentResult checkIdempotent(Long userId, String productId, String amount, String clientToken);

    /**
     * 标记订单创建完成
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param amount      订单金额
     * @param clientToken 客户端提供的幂等令牌
     * @param orderNo     创建的订单号
     */
    void markOrderCreated(Long userId, String productId, String amount, String clientToken, String orderNo);

    /**
     * 生成幂等令牌
     *
     * @param userId 用户ID
     * @return 幂等令牌
     */
    String generateIdempotentToken(Long userId);

    /**
     * 清理过期的幂等记录
     */
    void cleanExpiredIdempotentRecords();

    /**
     * 幂等性检查结果
     */
    class IdempotentResult {
        private boolean duplicate;
        private String existingOrderNo;
        private String message;

        public IdempotentResult(boolean duplicate, String existingOrderNo, String message) {
            this.duplicate = duplicate;
            this.existingOrderNo = existingOrderNo;
            this.message = message;
        }

        public static IdempotentResult success() {
            return new IdempotentResult(false, null, "幂等性检查通过");
        }

        public static IdempotentResult duplicate(String orderNo) {
            return new IdempotentResult(true, orderNo, "重复下单，返回已存在订单");
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getExistingOrderNo() {
            return existingOrderNo;
        }

        public String getMessage() {
            return message;
        }
    }
}
