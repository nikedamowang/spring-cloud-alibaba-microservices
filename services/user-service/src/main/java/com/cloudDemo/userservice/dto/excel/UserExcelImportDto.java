package com.cloudDemo.userservice.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户Excel导入数据模型
 * 定义Excel表格的列结构和数据验证规则
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
@ColumnWidth(15)
public class UserExcelImportDto {

    /**
     * 用户名 - 必填
     */
    @ExcelProperty(value = "用户名", index = 0)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名必须是3-20位字母、数字或下划线")
    private String username;

    /**
     * 密码 - 必填
     */
    @ExcelProperty(value = "密码", index = 1)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    private String password;

    /**
     * 邮箱 - 必填
     */
    @ExcelProperty(value = "邮箱", index = 2)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @ColumnWidth(25)
    private String email;

    /**
     * 手机号 - 必填
     */
    @ExcelProperty(value = "手机号", index = 3)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 真实姓名 - 可选
     */
    @ExcelProperty(value = "真实姓名", index = 4)
    @ColumnWidth(12)
    private String realName;

    /**
     * 年龄 - 可选
     */
    @ExcelProperty(value = "年龄", index = 5)
    @ColumnWidth(8)
    private Integer age;

    /**
     * 性别 - 可选 (男/女)
     */
    @ExcelProperty(value = "性别", index = 6)
    @Pattern(regexp = "^(男|女)$", message = "性别只能填写'男'或'女'")
    @ColumnWidth(8)
    private String gender;

    /**
     * 地址 - 可选
     */
    @ExcelProperty(value = "地址", index = 7)
    @ColumnWidth(30)
    private String address;

    /**
     * 备注 - 可选
     */
    @ExcelProperty(value = "备注", index = 8)
    @ColumnWidth(25)
    private String remark;

    // Excel导入时的行号，用于错误定位
    private Integer rowIndex;

    // 导入验证错误信息
    private String errorMessage;

    // 导入状态: SUCCESS, FAILED, DUPLICATE
    private String importStatus;

    // 创建时间 - 系统自动填充
    private LocalDateTime createTime;
}
