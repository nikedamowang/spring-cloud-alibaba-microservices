# Sentinel 熔断降级验证测试
Write-Host "开始 Sentinel 熔断降级功能验证..." -ForegroundColor Green

# 测试1: 正常请求验证
Write-Host "=== 测试1: 正常请求验证 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 3; $i++) {
    Write-Host "正常请求 $i" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 5
        Write-Host " - SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - ERROR" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

# 测试2: 高频请求测试
Write-Host "=== 测试2: 高频请求测试 ===" -ForegroundColor Yellow
$successCount = 0
$errorCount = 0

for ($i = 1; $i -le 30; $i++) {
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 1
        $successCount++
    }
    catch
    {
        $errorCount++
    }
    if ($i % 10 -eq 0)
    {
        Write-Host "进度: $i/30, 成功: $successCount, 失败: $errorCount" -ForegroundColor Cyan
    }
    Start-Sleep -Milliseconds 100
}

Write-Host "最终结果: 成功 $successCount 次, 失败 $errorCount 次" -ForegroundColor White

Write-Host "=== Sentinel 验证完成 ===" -ForegroundColor Green
