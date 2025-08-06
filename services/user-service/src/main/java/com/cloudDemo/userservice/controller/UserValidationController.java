package com.cloudDemo.userservice.controller;

import com.cloudDemo.userservice.dto.validation.CreateUserRequest;
import com.cloudDemo.userservice.dto.validation.SimpleCreateUserRequest;
import com.cloudDemo.userservice.dto.validation.UpdateUserRequest;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import com.cloudDemo.userservice.validation.group.CreateGroup;
import com.cloudDemo.userservice.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户校验增强控制器
 * 提供带有完整参数校验功能的用户管理接口
 */
@Slf4j
@RestController
@RequestMapping("/user/validation")
@Tag(name = "用户校验增强API", description = "提供完整参数校验功能的用户管理接口")
@Validated
public class UserValidationController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建用户 - 带完整校验
     */
    @PostMapping("/create")
    @Operation(summary = "创建用户", description = "创建新用户，支持完整的参数校验")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @Validated(CreateGroup.class) @RequestBody CreateUserRequest request) {

        log.info("🔧 开始创建用户，用户名: {}", request.getUsername());

        try {
            // 转换为User实体
            User user = new User();
            BeanUtils.copyProperties(request, user);

            // 密码加密
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setStatus("active");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            // 保存用户
            int result = userMapper.insert(user);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                // 隐藏密码信息
                user.setPassword(null);

                response.put("success", true);
                response.put("message", "用户创建成功");
                response.put("data", user);

                log.info("✅ 用户创建成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户创建失败");

                log.error("❌ 用户创建失败，数据库插入返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 创建用户 - 简化版校验测试
     */
    @PostMapping("/create-simple")
    @Operation(summary = "创建用户(简化校验)", description = "创建新用户，使用简化的参数校验进行测试")
    public ResponseEntity<Map<String, Object>> createUserSimple(
            @Valid @RequestBody SimpleCreateUserRequest request) {

        log.info("🔧 开始创建用户(简化版)，用户名: {}", request.getUsername());

        try {
            // 转换为User实体
            User user = new User();
            BeanUtils.copyProperties(request, user);

            // 密码加密
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setStatus("active");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            // 保存用户
            int result = userMapper.insert(user);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                user.setPassword(null);
                response.put("success", true);
                response.put("message", "用户创建成功");
                response.put("data", user);

                log.info("✅ 用户创建成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户创建失败");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新用户 - 带完整校验
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户", description = "更新用户信息，支持完整的参数校验")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Valid @Validated(UpdateGroup.class) @RequestBody UpdateUserRequest request) {

        log.info("🔧 开始更新用户，用户ID: {}", request.getId());

        try {
            // 检查用户是否存在
            User existingUser = userMapper.selectById(request.getId());
            if (existingUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");

                log.warn("⚠️ 更新失败，用户不存在，ID: {}", request.getId());
                return ResponseEntity.badRequest().body(response);
            }

            // 更新用户信息
            User user = new User();
            BeanUtils.copyProperties(request, user);
            user.setUpdateTime(LocalDateTime.now());

            int result = userMapper.updateById(user);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                // 获取更新后的用户信息
                User updatedUser = userMapper.selectById(request.getId());
                updatedUser.setPassword(null); // 隐藏密码

                response.put("success", true);
                response.put("message", "用户更新成功");
                response.put("data", updatedUser);

                log.info("✅ 用户更新成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户更新失败");

                log.error("❌ 用户更新失败，数据库更新返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 更新用户异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取用户详情 - 带路径参数校验
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<Map<String, Object>> getUserDetail(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable @NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须大于0") Integer id) {

        log.info("🔍 查询用户详情，ID: {}", id);

        try {
            User user = userMapper.selectById(id);

            Map<String, Object> response = new HashMap<>();
            if (user != null) {
                user.setPassword(null); // 隐藏密码

                response.put("success", true);
                response.put("message", "查询成功");
                response.put("data", user);

                log.info("✅ 用户查询成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");

                log.warn("⚠️ 用户不存在，ID: {}", id);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 查询用户异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除用户 - 带路径参数校验
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable @NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须大于0") Integer id) {

        log.info("🗑️ 删除用户，ID: {}", id);

        try {
            // 检查用户是否存在
            User existingUser = userMapper.selectById(id);
            if (existingUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");

                log.warn("⚠️ 删除失败，用户不存在，ID: {}", id);
                return ResponseEntity.badRequest().body(response);
            }

            int result = userMapper.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "用户删除成功");
                response.put("deletedUserId", id);

                log.info("✅ 用户删除成功，ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户删除失败");

                log.error("❌ 用户删除失败，数据库删除返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 删除用户异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
