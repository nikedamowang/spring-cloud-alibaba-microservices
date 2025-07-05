# PowerShell API Test Script
$baseUrl = "http://localhost:9090/api/nacos"

Write-Host "Testing Management Service APIs..." -ForegroundColor Green

# Test 1: Get all configs
Write-Host "`n1. Testing /configs endpoint:" -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/configs" -Method Get -TimeoutSec 10
    Write-Host "Success: Found $( $response.count ) configs" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
}
catch
{
    Write-Host "Failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 2: Get all services
Write-Host "`n2. Testing /services endpoint:" -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/services" -Method Get -TimeoutSec 10
    Write-Host "Success: Found $( $response.count ) services" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
}
catch
{
    Write-Host "Failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 3: Get specific config
Write-Host "`n3. Testing /config endpoint:" -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/config?dataId=user-service.properties&group=DEFAULT_GROUP" -Method Get -TimeoutSec 10
    Write-Host "Success: Retrieved config" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
}
catch
{
    Write-Host "Failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 4: Compare config with template
Write-Host "`n4. Testing /config/compare endpoint:" -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/config/compare?dataId=user-service.properties&group=DEFAULT_GROUP" -Method Get -TimeoutSec 10
    Write-Host "Success: Config comparison completed" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
}
catch
{
    Write-Host "Failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 5: Search configs
Write-Host "`n5. Testing /configs/search endpoint:" -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/configs/search?keyword=user" -Method Get -TimeoutSec 10
    Write-Host "Success: Found $( $response.count ) matching configs" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
}
catch
{
    Write-Host "Failed: $( $_.Exception.Message )" -ForegroundColor Red
}

Write-Host "`nAPI Testing completed!" -ForegroundColor Green
