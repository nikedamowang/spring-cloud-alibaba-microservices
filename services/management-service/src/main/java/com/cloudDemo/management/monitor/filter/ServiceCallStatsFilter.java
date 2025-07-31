package com.cloudDemo.management.monitor.filter;

import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dubbo服务调用统计Filter
 * 拦截所有RPC调用，记录调用统计信息
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER})
public class ServiceCallStatsFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String callId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();

        // 获取调用信息
        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();
        String consumerIp = RpcContext.getServiceContext().getLocalHost();
        String providerIp = RpcContext.getServiceContext().getRemoteHost();

        try {
            log.debug("🔍 Dubbo调用开始 - 服务: {}, 方法: {}, 调用ID: {}",
                    serviceName, methodName, callId);

            // 执行实际调用
            Result result = invoker.invoke(invocation);

            // 计算响应时间
            long endMillis = System.currentTimeMillis();
            long responseTime = endMillis - startMillis;
            LocalDateTime endTime = LocalDateTime.now();

            // 构建调用记录
            ServiceCallRecord record = ServiceCallRecord.builder()
                    .callId(callId)
                    .serviceName(serviceName)
                    .methodName(methodName)
                    .consumerIp(consumerIp)
                    .providerIp(providerIp)
                    .startTime(startTime)
                    .endTime(endTime)
                    .responseTime(responseTime)
                    .status("SUCCESS")
                    .responseStatus("正常")
                    .requestParams("参数数量: " + invocation.getArguments().length)
                    .build();

            // 异步记录统计信息（简化版，直接使用Redis）
            recordServiceCallAsync(record);

            log.debug("✅ Dubbo调用成功 - 服务: {}, 方法: {}, 耗时: {}ms",
                    serviceName, methodName, responseTime);

            return result;

        } catch (RpcException e) {
            // 处理RPC异常
            handleException(callId, serviceName, methodName, consumerIp, providerIp,
                    startTime, startMillis, e, "RPC异常");
            throw e;

        } catch (Exception e) {
            // 处理其他异常
            handleException(callId, serviceName, methodName, consumerIp, providerIp,
                    startTime, startMillis, e, "系统异常");
            throw new RpcException("服务调用异常: " + e.getMessage(), e);
        }
    }

    private void handleException(String callId, String serviceName, String methodName,
                                 String consumerIp, String providerIp, LocalDateTime startTime,
                                 long startMillis, Exception e, String errorType) {
        long endMillis = System.currentTimeMillis();
        long responseTime = endMillis - startMillis;
        LocalDateTime endTime = LocalDateTime.now();

        ServiceCallRecord record = ServiceCallRecord.builder()
                .callId(callId)
                .serviceName(serviceName)
                .methodName(methodName)
                .consumerIp(consumerIp)
                .providerIp(providerIp)
                .startTime(startTime)
                .endTime(endTime)
                .responseTime(responseTime)
                .status("FAILURE")
                .errorMessage(e.getMessage())
                .responseStatus(errorType)
                .build();

        recordServiceCallAsync(record);

        log.error("❌ Dubbo调用失败 - 服务: {}, 方法: {}, 耗时: {}ms, 错误: {}",
                serviceName, methodName, responseTime, e.getMessage());
    }

    /**
     * 异步记录服务调用（简化版）
     */
    private void recordServiceCallAsync(ServiceCallRecord record) {
        try {
            // 这里简化实现，在实际项目中可以使用消息队列或异步线程池
            log.info("📊 服务调用统计 - 服务: {}, 方法: {}, 状态: {}, 耗时: {}ms",
                    record.getServiceName(), record.getMethodName(),
                    record.getStatus(), record.getResponseTime());
        } catch (Exception e) {
            log.warn("记录服务调用统计失败: {}", e.getMessage());
        }
    }
}
