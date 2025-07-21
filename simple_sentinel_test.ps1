# 简单的 Sentinel 测试脚本
Write-Host "开始 Sentinel 熔断降级测试..." -ForegroundColor Green

# 测试1: 正常请求测试
Write-Host "=== 测试1: 正常请求测试 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 5; $i++) {
    Write-Host "正常请求 $i"
    $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get
    Write-Host "SUCCESS" -ForegroundColor Green
    Start-Sleep -Seconds 1
}

# 测试2: 快速请求测试
Write-Host "=== 测试2: 快速请求测试 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 15; $i++) {
    Write-Host "快速请求 $i" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 2
        Write-Host " - SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - ERROR" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "Sentinel 测试完成!" -ForegroundColor Green
