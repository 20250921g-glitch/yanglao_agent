import java.sql.*;
import java.math.BigDecimal;

public class FixCommission {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/elderly_care?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
        Connection conn = DriverManager.getConnection(url, "root", "GXJgxj.060811");
        
        conn.createStatement().execute("DELETE FROM commission_record");
        conn.createStatement().execute("ALTER TABLE commission_record AUTO_INCREMENT = 1");
        
        String sql = "INSERT INTO commission_record (worker_id, worker_name, order_id, order_no, order_amount, commission_rate, commission_amount, service_type, status, settle_time, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        
        Object[][] data = {
            {1, "王芳", 1, "WO202507010001", 500.00, 0.15, 75.00, "家政护工", 1, "2026-07-07 16:51:31", "2026-07-07 16:51:31", "2026-07-08 14:24:18"},
            {2, "李强", 2, "WO202507010002", 400.00, 0.20, 80.00, "康复理疗", 1, "2026-07-07 16:51:31", "2026-07-07 16:51:31", "2026-07-08 14:24:18"},
            {3, "张护士", 3, "SO20260708001", 200.00, 0.10, 20.00, "陪诊服务", 0, null, "2026-07-08 14:24:18", "2026-07-08 14:24:18"},
            {4, "刘护理", 4, "SO20260708002", 150.00, 0.10, 15.00, "上门护理", 0, null, "2026-07-08 14:24:18", "2026-07-08 14:24:18"}
        };
        
        for (Object[] row : data) {
            ps.setInt(1, (Integer) row[0]);
            ps.setString(2, (String) row[1]);
            ps.setInt(3, (Integer) row[2]);
            ps.setString(4, (String) row[3]);
            ps.setBigDecimal(5, new BigDecimal(row[4].toString()));
            ps.setBigDecimal(6, new BigDecimal(row[5].toString()));
            ps.setBigDecimal(7, new BigDecimal(row[6].toString()));
            ps.setString(8, (String) row[7]);
            ps.setInt(9, (Integer) row[8]);
            if (row[9] != null) ps.setString(10, (String) row[9]); else ps.setNull(10, Types.TIMESTAMP);
            ps.setString(11, (String) row[10]);
            ps.setString(12, (String) row[11]);
            ps.executeUpdate();
            System.out.println("Inserted: " + row[1] + " / " + row[7]);
        }
        
        ResultSet rs = conn.createStatement().executeQuery("SELECT id, worker_name, service_type, commission_amount, commission_rate FROM commission_record");
        System.out.println("\n=== Final Data ===");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("id") + " | " + rs.getString("worker_name") + " | " + rs.getString("service_type") + " | " + rs.getBigDecimal("commission_amount") + " | " + rs.getBigDecimal("commission_rate"));
        }
        
        conn.close();
    }
}