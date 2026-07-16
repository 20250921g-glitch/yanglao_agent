import java.sql.*;

public class ProductUpgrade {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/elderly_care?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
        String user = "root";
        String password = "GXJgxj.060811";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            
            // 添加商品分类表字段
            try {
                stmt.executeUpdate("ALTER TABLE product_category ADD COLUMN service_type VARCHAR(50) COMMENT '服务类型' AFTER name");
            } catch (SQLException e) { System.out.println("service_type字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product_category ADD COLUMN icon VARCHAR(255) COMMENT '图标' AFTER sort");
            } catch (SQLException e) { System.out.println("icon字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product_category ADD COLUMN description VARCHAR(500) COMMENT '描述' AFTER icon");
            } catch (SQLException e) { System.out.println("description字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product_category ADD COLUMN update_time DATETIME COMMENT '更新时间' AFTER create_time");
            } catch (SQLException e) { System.out.println("update_time字段已存在"); }
            
            // 添加商品表字段
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN code VARCHAR(100) COMMENT '商品编码' AFTER name");
            } catch (SQLException e) { System.out.println("code字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN service_type VARCHAR(50) COMMENT '服务类型' AFTER category_name");
            } catch (SQLException e) { System.out.println("product.service_type字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN original_price DECIMAL(10,2) COMMENT '原价' AFTER price");
            } catch (SQLException e) { System.out.println("original_price字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN sales INT DEFAULT 0 COMMENT '销量' AFTER stock");
            } catch (SQLException e) { System.out.println("sales字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN images TEXT COMMENT '图片列表JSON' AFTER image");
            } catch (SQLException e) { System.out.println("images字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN detail TEXT COMMENT '详情' AFTER description");
            } catch (SQLException e) { System.out.println("detail字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN unit VARCHAR(20) COMMENT '单位' AFTER detail");
            } catch (SQLException e) { System.out.println("unit字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN recommend TINYINT DEFAULT 0 COMMENT '是否推荐' AFTER status");
            } catch (SQLException e) { System.out.println("recommend字段已存在"); }
            
            try {
                stmt.executeUpdate("ALTER TABLE product ADD COLUMN sort INT DEFAULT 0 COMMENT '排序' AFTER recommend");
            } catch (SQLException e) { System.out.println("sort字段已存在"); }
            
            // 清空并插入分类数据
            stmt.executeUpdate("TRUNCATE TABLE product_category");
            
            // 家政护理分类
            String[][] categories = {
                {"生活照料", "家政护理", "1", "日常生活起居照料服务"},
                {"临床护理", "家政护理", "2", "专业临床护理服务"},
                {"康复护理", "家政护理", "3", "康复训练护理服务"},
                {"心理关怀", "家政护理", "4", "心理疏导关怀服务"},
                {"上门做饭", "家政护理", "5", "上门做饭送餐服务"},
                {"健康管理", "家政护理", "6", "健康管理咨询服务"},
                {"陪同就医", "家政护理", "7", "陪同就医陪诊服务"},
                {"日常清洁", "家政护理", "8", "家庭清洁卫生服务"},
                {"中医理疗", "康复理疗", "1", "中医推拿针灸理疗"},
                {"康复训练", "康复理疗", "2", "康复运动训练指导"},
                {"物理治疗", "康复理疗", "3", "物理因子治疗服务"},
                {"作业治疗", "康复理疗", "4", "日常生活能力训练"},
                {"常规体检", "上门体检", "1", "基础健康体检项目"},
                {"慢性病筛查", "上门体检", "2", "慢性疾病专项筛查"},
                {"专项检查", "上门体检", "3", "专项医学检查服务"},
                {"健康评估", "上门体检", "4", "健康状态综合评估"}
            };
            
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO product_category (name, service_type, sort, status, description, create_time) VALUES (?, ?, ?, 1, ?, NOW())"
            );
            
            for (String[] cat : categories) {
                ps.setString(1, cat[0]);
                ps.setString(2, cat[1]);
                ps.setInt(3, Integer.parseInt(cat[2]));
                ps.setString(4, cat[3]);
                ps.executeUpdate();
            }
            
            // 更新商品服务类型
            stmt.executeUpdate("UPDATE product SET service_type = '家政护理'");
            
            System.out.println("商品分类数据升级完成！");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
