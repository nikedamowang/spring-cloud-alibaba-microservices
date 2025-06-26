// ...existing code...
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class MySQLTestRunner implements CommandLineRunner {
    private final DataSource dataSource;

    public MySQLTestRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[MySQL测试] 数据库连接成功: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("[MySQL测试] 数据库连接失败: " + e.getMessage());
        }
    }
}
// ...existing code...
