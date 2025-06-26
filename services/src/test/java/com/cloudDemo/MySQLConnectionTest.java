package com.cloudDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class MySQLConnectionTest {
    @Autowired
    private DataSource dataSource;

    @Test
    public void testMySQLConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[MySQL测试] 数据库连接成功: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("[MySQL测试] 数据库连接失败: " + e.getMessage());
            throw e;
        }
    }
}
// ...existing code...
