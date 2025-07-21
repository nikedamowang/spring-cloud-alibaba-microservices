# Sentinel 熔断降级压力测试脚本
# 用于测试 Sentinel 的限流和熔断功能

Write-Host "开始 Sentinel 熔断降级测试..." -ForegroundColor Green

# 测试1: 正常请求测试
Write-Host "`n=== 测试1: 正常请求测试 ===" -ForegroundColor Yellow
for ($i = 1; $i -le 5; $i++) {
    Write-Host "正常请求 $i :"
    try
    {
        $response = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 5
        Write-Host "SUCCESS - 获取到用户数据" -ForegroundColor Green
    }
    catch
    {
        Write-Host "ERROR - $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

# 测试2: 快速连续请求测试 - 触发限流
Write-Host "`n=== 测试2: 快速连续请求测试（可能触发限流） ===" -ForegroundColor Yellow
for ($i = 1; $i -le 20; $i++) {
    Write-Host "快速请求 $i : " -NoNewline
    try
    {
        $response = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 2
        Write-Host "SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host "ERROR - $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 100  # 100ms间隔，高频请求
}

# 测试3: 用户信息查询（带Sentinel注解的接口）
Write-Host "`n=== 测试3: 用户信息查询测试 ===" -ForegroundColor Yellow
$userIds = @(1, 2, 3, 999, 1000)  # 包含一些不存在的用户ID来测试异常情况
foreach ($userId in $userIds)
{
    Write-Host "查询用户ID $userId : " -NoNewline
    try
    {
        $response = Invoke-RestMethod -Uri "http://localhost:9000/user/id/$userId" -Method Get -TimeoutSec 5
        Write-Host "SUCCESS" -ForegroundColor Green
    }
    catch
    {
        Write-Host "ERROR - $( $_.Exception.Message )" -ForegroundColor Red
    }
    Start-Sleep -Seconds 1
}

# 测试4: 并发压力测试
Write-Host "`n=== 测试4: 并发压力测试（可能触发熔断） ===" -ForegroundColor Yellow
$jobs = @()
for ($i = 1; $i -le 10; $i++) {
    $job = Start-Job -ScriptBlock {
        param($threadId)
        for ($j = 1; $j -le 5; $j++) {
            try
            {
                $result = Invoke-RestMethod -Uri "http://localhost:9000/user/list" -Method Get -TimeoutSec 3
                "Thread${threadId}-Request${j}: SUCCESS"
            }
            catch
            {
                "Thread${threadId}-Request${j}: ERROR - $( $_.Exception.Message )"
            }
            Start-Sleep -Milliseconds 50
        }
    } -ArgumentList $i
    $jobs += $job
}

# 等待所有并发任务完成
$jobs | Wait-Job | Receive-Job
$jobs | Remove-Job

Write-Host "`n=== Sentinel 测试完成 ===" -ForegroundColor Green
Write-Host "请检查以下内容来验证 Sentinel 功能:" -ForegroundColor Cyan
Write-Host "1. Sentinel Dashboard (http://localhost:8080)" -ForegroundColor White
Write-Host "2. 用户服务日志中的限流/熔断记录" -ForegroundColor White
Write-Host "3. 服务响应时间和错误率变化" -ForegroundColor White
