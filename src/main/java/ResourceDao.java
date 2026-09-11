import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResourceDao {
    private final DataSource dataSource;

    public ResourceDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean reserveInventory(long resourceId, long userId, int requestedQuantity, int expectedVersion) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String updateSql = "UPDATE resources SET available_quantity = available_quantity - ?, version = version + 1 " +
                    "WHERE primary_id = ? AND version = ? AND available_quantity >= ?;";

            try (PreparedStatement psmt = conn.prepareStatement(updateSql)) {
                psmt.setInt(1, requestedQuantity);
                psmt.setLong(2, resourceId);
                psmt.setInt(3, expectedVersion);
                psmt.setInt(4, requestedQuantity);

                int rowsUpdated = psmt.executeUpdate();

                if (rowsUpdated == 1) {
                    String insertSql = "INSERT INTO reservations(quantity, resource_id, user_id) VALUES(?,?,?);";
                    try (PreparedStatement psmts = conn.prepareStatement(insertSql)) {
                        psmts.setInt(1, requestedQuantity);
                        psmts.setLong(2, resourceId);
                        psmts.setLong(3, userId);
                        psmts.executeUpdate();
                    }
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

}