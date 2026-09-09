import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        // 1. Configure the database connection manager
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/reliefmesh");
        config.setUsername("admin");
        config.setPassword("password123");
        config.setMaximumPoolSize(10);

        HikariDataSource dataSource = new HikariDataSource(config);

        // 2. Instantiate your database worker
        ResourceDao resourceDao = new ResourceDao(dataSource);

        // 3. Create Javalin 7 instance and register endpoints inside the config block
        Javalin app = Javalin.create(javalinConfig -> {

            // Access the .post() method directly through javalinConfig.routes
            javalinConfig.routes.post("/api/reservations", ctx -> {

                // Convert incoming web JSON into your ReservationRequest object
                ReservationRequest req = ctx.bodyAsClass(ReservationRequest.class);

                // Run the safe database transaction via ResourceDao
                boolean isReserved = resourceDao.reserveInventory(
                        req.resource_id,
                        req.user_id,
                        req.quantity,
                        req.version
                );

                // Send back an HTTP status code and response message
                if (isReserved) {
                    ctx.status(200);
                    ctx.result("Reservation successful");
                } else {
                    ctx.status(400);
                    ctx.result("Reservation failed: low stock or version mismatch");
                }
            });

        }).start(8080); // 4. Power on the server on port 8080
    }
}