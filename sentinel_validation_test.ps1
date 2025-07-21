# Sentinel 熔断降级验证测试
Write-Host "开始 Sentinel 熔断降级功能验证..." -ForegroundColor Green

# 测试1: 正常请求验证
Write-Host "`n=== 测试1: 正常请求验证 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 3; $i++) {
    Write-Host "正常请求 $i" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 5
        Write-Host " - SUCCESS (返回 $( $result.Count ) 个用户)" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - ERROR: $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

# 测试2: 高频请求测试 - 测试Sentinel限流
Write-Host "`n=== 测试2: 高频请求测试 ===" -ForegroundColor Yellow
$successCount = 0
$errorCount = 0

for ($i = 1; $i -le 50; $i++) {
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 1
        $successCount++
        if ($i % 10 -eq 0)
        {
            Write-Host "成功请求: $successCount, 失败请求: $errorCount" -ForegroundColor Cyan
        }
    }
    catch
    {
        $errorCount++
        if ($i % 10 -eq 0)
        {
            Write-Host "成功请求: $successCount, 失败请求: $errorCount" -ForegroundColor Cyan
        }
    }
    Start-Sleep -Milliseconds 50  # 很短的间隔来测试限流
}

Write-Host "`n最终结果: 成功 $successCount 次, 失败 $errorCount 次" -ForegroundColor White

# 测试3: 测试用户查询接口（带Sentinel注解）
Write-Host "`n=== 测试3: 用户查询接口测试 ===" -ForegroundColor Yellow
$userIds = @(1, 2, 3, 999)  # 包含不存在的用户ID
foreach ($userId in $userIds)
{
    Write-Host "查询用户 ID $userId" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/id/$userId" -Method Get -TimeoutSec 3
        Write-Host " - SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - ERROR: $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 500
}

# 测试4: 验证Sentinel配置
Write-Host "`n=== 测试4: Sentinel配置验证 ===" -ForegroundColor Yellow
Write-Host "检查用户服务Sentinel配置..." -NoNewline
try
{
    # 检查Actuator健康端点
    $health = Invoke-RestMethod -Uri "http://localhost:9000/actuator/health" -Method Get -TimeoutSec 3
    Write-Host " - 服务健康状态正常" -ForegroundColor Green
}
catch
{
    Write-Host " - 无法访问健康检查端点" -ForegroundColor Yellow
}

Write-Host "`n=== Sentinel 验证完成 ===" -ForegroundColor Green
Write-Host "说明:" -ForegroundColor Cyan
Write-Host "1. 如果所有请求都成功，说明当前负载下Sentinel未触发限流" -ForegroundColor White
Write-Host "2. 如果出现失败请求，可能是网络超时或Sentinel限流" -ForegroundColor White
Write-Host "3. Sentinel的限流规则可能需要在Dashboard中手动配置" -ForegroundColor White
Write-Host "4. 查看用户服务日志可以了解Sentinel的工作状态" -ForegroundColor White
