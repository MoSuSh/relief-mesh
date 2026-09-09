import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/reliefmesh");
        config.setUsername("admin");
        config.setPassword("password123");
        config.setMaximumPoolSize(10);

        HikariDataSource dataSource = new HikariDataSource(config);

        ResourceDao rd  = new ResourceDao(dataSource);

        Javalin app = Javalin.create().start(8080);
    }
}
