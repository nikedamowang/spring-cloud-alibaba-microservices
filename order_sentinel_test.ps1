# 订单服务 Sentinel 熔断降级测试脚本
Write-Host "开始订单服务 Sentinel 熔断降级测试..." -ForegroundColor Green

# 测试1: 正常订单查询测试
Write-Host "`n=== 测试1: 正常订单查询测试 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 5; $i++) {
    Write-Host "查询订单 $i" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:8000/order/123" -Method Get -TimeoutSec 5
        Write-Host " - SUCCESS: $result" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - ERROR: $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

# 测试2: 订单创建测试（会有随机异常来触发熔断）
Write-Host "`n=== 测试2: 订单创建测试（模拟随机异常） ===" -ForegroundColor Yellow
for ($i = 1; $i -le 10; $i++) {
    Write-Host "创建订单 $i" -NoNewline
    try
    {
        $orderData = @{
            productId = 1001
            quantity = 2
            price = 99.99
        } | ConvertTo-Json

        $result = Invoke-RestMethod -Uri "http://localhost:8000/order/create" -Method Post -Body $orderData -ContentType "application/json" -TimeoutSec 3
        Write-Host " - SUCCESS: $result" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - FALLBACK: $( $_.Exception.Message )" -ForegroundColor Yellow
    }
    Start-Sleep -Milliseconds 500
}

# 测试3: 高频订单状态更新测试
Write-Host "`n=== 测试3: 订单状态更新测试 ===" -ForegroundColor Yellow
$statuses = @("processing", "shipped", "delivered", "cancelled")
for ($i = 1; $i -le 15; $i++) {
    $orderId = Get-Random -Minimum 1 -Maximum 1000
    $status = $statuses[(Get-Random -Minimum 0 -Maximum $statuses.Length)]

    Write-Host "更新订单 $orderId 状态为 $status" -NoNewline
    try
    {
        $result = Invoke-RestMethod -Uri "http://localhost:8000/order/$orderId/status?status=$status" -Method Put -TimeoutSec 2
        Write-Host " - SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host " - FALLBACK" -ForegroundColor Yellow
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "`n=== 订单服务 Sentinel 测试完成 ===" -ForegroundColor Green
Write-Host "测试说明:" -ForegroundColor Cyan
Write-Host "1. 订单查询接口相对稳定" -ForegroundColor White
Write-Host "2. 订单创建接口有50%概率随机异常，用于测试熔断" -ForegroundColor White
Write-Host "3. 订单状态更新有30%概率异常，测试降级" -ForegroundColor White
Write-Host "4. 当异常率达到阈值时，Sentinel会触发熔断保护" -ForegroundColor White
