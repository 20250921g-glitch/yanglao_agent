import java.sql.*;

public class ProductDataFix {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/elderly_care?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "GXJgxj.060811";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // 更新商品数据
            String[][] products = {
                {"1", "钙片", "SP001", "16", "上门体检", "68.00", "88.00", "瓶", "中老年人补钙专用", "每瓶100片，每日2次，每次1片", "1", "1"},
                {"2", "轮椅", "SP002", "2", "家政护理", "899.00", "999.00", "台", "高品质轮椅，适合老人使用", "可折叠，带刹车，承重100kg", "1", "1"},
                {"3", "纸尿裤", "SP003", "3", "家政护理", "128.00", "168.00", "包", "成人纸尿裤，透气舒适", "每包30片，S/M/L/XL码可选", "1", "0"},
                {"4", "蛋白粉", "SP004", "4", "家政护理", "198.00", "258.00", "罐", "中老年营养蛋白粉", "每罐400g，每日1-2勺", "1", "1"}
            };

            String sql = "UPDATE product SET name=?, code=?, category_id=?, service_type=?, price=?, original_price=?, unit=?, description=?, detail=?, recommend=?, update_time=NOW() WHERE id=?";

            for (String[] p : products) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, p[1]);  // name
                    ps.setString(2, p[2]);  // code
                    ps.setInt(3, Integer.parseInt(p[3]));  // category_id
                    ps.setString(4, p[4]);  // service_type
                    ps.setBigDecimal(5, new java.math.BigDecimal(p[5]));  // price
                    ps.setBigDecimal(6, new java.math.BigDecimal(p[6]));  // original_price
                    ps.setString(7, p[7]);  // unit
                    ps.setString(8, p[8]);  // description
                    ps.setString(9, p[9]);  // detail
                    ps.setInt(10, Integer.parseInt(p[10]));  // recommend
                    ps.setInt(11, Integer.parseInt(p[0]));  // id
                    int rows = ps.executeUpdate();
                    System.out.println("Updated product " + p[0] + ": " + rows + " rows");
                }
            }

            // 验证
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, service_type FROM product")) {
                System.out.println("\n验证结果:");
                while (rs.next()) {
                    System.out.println("ID=" + rs.getInt("id") + ", Name=" + rs.getString("name") + ", Type=" + rs.getString("service_type"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
