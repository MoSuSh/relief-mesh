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
                                    int requestedQuantity, int expectedVersion)
                                    throws SQLException{
        try(Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            try(PreparedStatement psmt = conn.prepareStatement("UPDATE resources SET stock = stock - ?, version = version + 1 WHERE id = ? AND version = ? AND stock >= ?;")) {

                psmt.setInt(1, requestedQuantity);
                psmt.setLong(2, resourceId);
                psmt.setInt(3, expectedVersion);
                psmt.setInt(4, requestedQuantity);

                int rowsUpdated = psmt.executeUpdate();

                if (rowsUpdated == 1) {
                    try(PreparedStatement psmts = conn.prepareStatement("INSERT INTO reservations(requestedQuantity, resourceId, expectedVersion) VALUES(?,?,?);")){
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

    }
}
