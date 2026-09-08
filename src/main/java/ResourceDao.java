import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResourceDao {
    private final DataSource dataSource;
    public ResourceDao(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public boolean reserveInventory(long resourceId, long userId,
                                    int requestedQuantity, int expectedVersion){
        try(Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            try(PreparedStatement psmt = conn.prepareStatement("UPDATE resources SET available_quantity = available_quantity - ?, version = version + 1 WHERE primary_id = ? AND version = ? AND available_quantity >= ?;")) {

                psmt.setInt(1, requestedQuantity);
                psmt.setLong(2, resourceId);
                psmt.setInt(3, expectedVersion);
                psmt.setInt(4, requestedQuantity);

                int rowsUpdated = psmt.executeUpdate();

                if (rowsUpdated == 1) {
                    try(PreparedStatement psmts = conn.prepareStatement("INSERT INTO reservations(quantity, resource_id, user_id) VALUES(?,?,?);")){
                        psmts.setInt(1,requestedQuantity);
                        psmts.setLong(2,resourceId);
                        psmts.setInt(3,expectedVersion);
                        psmts.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }else if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
                else {
                    return false;
                }
            }
            catch(SQLException e){
                conn.rollback();
                return false;
            }
        }
        catch(SQLException e){
            return false;
        }
    }
    public static void main(String[] args) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/reliefmesh");
        config.setUsername("admin");
        config.setPassword("password123");
        config.setMaximumPoolSize(10);

        HikariDataSource dataSource = new HikariDataSource(config);

        ResourceDao resConnect = new ResourceDao(dataSource);

        long resourceId = 3L;
        long userId = 100L;
        int requestedQuantity = 2;
        int expectedVersion = 1;

        try{
            boolean isReserved = resConnect.reserveInventory(resourceId, userId, requestedQuantity, expectedVersion);
            if(isReserved){
                System.out.println("SUCCESS: Inventory reserved and log entry added!");
            } else {
                System.out.println("FAILED: Reservation failed due to low stock or version mismatch.");
            }
        }
        finally {
            dataSource.close();
        }
    }

}
