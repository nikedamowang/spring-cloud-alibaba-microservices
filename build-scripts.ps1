# Maven Profile环境配置构建脚本
# 用于不同环境的自动化构建和部署

# ==============================================
# 开发环境构建脚本
# ==============================================

# 开发环境编译（默认Profile）
Write-Host "=== 开发环境构建 ===" -ForegroundColor Green
mvn clean compile -Pdev

# 开发环境打包
Write-Host "=== 开发环境打包 ===" -ForegroundColor Green
mvn clean package -Pdev -DskipTests

# 开发环境运行（示例）
Write-Host "=== 开发环境服务启动示例 ===" -ForegroundColor Green
Write-Host "用户服务: java -jar -Dspring.profiles.active=dev services/user-service/target/user-service-0.0.1-SNAPSHOT.jar"
Write-Host "订单服务: java -jar -Dspring.profiles.active=dev services/order-service/target/order-service-0.0.1-SNAPSHOT.jar"

# ==============================================
# 测试环境构建脚本
# ==============================================

# 测试环境编译
Write-Host "`n=== 测试环境构建 ===" -ForegroundColor Yellow
mvn clean compile -Ptest

# 测试环境打包
Write-Host "=== 测试环境打包 ===" -ForegroundColor Yellow
mvn clean package -Ptest -DskipTests

# 测试环境运行（示例）
Write-Host "=== 测试环境服务启动示例 ===" -ForegroundColor Yellow
Write-Host "用户服务: java -jar -Dspring.profiles.active=test services/user-service/target/user-service-0.0.1-SNAPSHOT.jar"
Write-Host "订单服务: java -jar -Dspring.profiles.active=test services/order-service/target/order-service-0.0.1-SNAPSHOT.jar"

# ==============================================
# 生产环境构建脚本
# ==============================================

# 生产环境编译
Write-Host "`n=== 生产环境构建 ===" -ForegroundColor Red
mvn clean compile -Pprod

# 生产环境打包
Write-Host "=== 生产环境打包 ===" -ForegroundColor Red
mvn clean package -Pprod -DskipTests

# 生产环境运行（示例）
Write-Host "=== 生产环境服务启动示例 ===" -ForegroundColor Red
Write-Host "用户服务: java -jar -Dspring.profiles.active=prod services/user-service/target/user-service-0.0.1-SNAPSHOT.jar"
Write-Host "订单服务: java -jar -Dspring.profiles.active=prod services/order-service/target/order-service-0.0.1-SNAPSHOT.jar"

# ==============================================
# 环境验证脚本
# ==============================================

Write-Host "`n=== 环境配置验证 ===" -ForegroundColor Cyan

# 检查Profile配置是否正确
Write-Host "检查Maven Profile配置..."
mvn help:all-profiles

# 显示当前激活的Profile
Write-Host "`n显示当前激活的Profile..."
mvn help:active-profiles

Write-Host "`n=== 构建脚本执行完成 ===" -ForegroundColor Green
