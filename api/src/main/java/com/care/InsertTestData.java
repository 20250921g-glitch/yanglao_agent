package com.care;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsertTestData {
    private static final String URL = "jdbc:mysql://localhost:3306/elderly_care?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASS = "GXJgxj.060811";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Connected successfully!");
            
            // Insert elders (ids 4-10 to avoid duplicates with existing ids 1-3)
            insertElders(conn);
            
            // Insert service orders (ids 4-10)
            insertServiceOrders(conn);
            
            // Insert product orders (ids 3-12)
            insertProductOrders(conn);
            
            // Create and insert health_service_order table
            createHealthServiceOrderTable(conn);
            insertHealthServiceOrders(conn);
            
            // Insert staff
            insertStaff(conn);
            
            // Insert activity
            insertActivity(conn);
            
            // Insert banner
            insertBanner(conn);
            
            System.out.println("All test data inserted successfully!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    static void insertElders(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO elder (id, name, gender, birth_date, phone, address, health_level, id_card, emergency_contact, emergency_phone, blood_type, allergy, chronic_disease, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {4, "刘德福", "男", LocalDate.of(1940,5,30), "13800138004", "北京市西城区温馨家园1号楼401", 1, "110101194005300013", "刘芳", "13900139004", "AB型", "无", "高血脂", LocalDateTime.now()},
                {5, "赵淑芬", "女", LocalDate.of(1952,9,18), "13800138005", "北京市丰台区长寿阁6号楼103", 2, "110101195209180027", "赵强", "13900139005", "A型", "芒果过敏", "骨质疏松", LocalDateTime.now()},
                {6, "孙长顺", "男", LocalDate.of(1946,2,14), "13800138006", "北京市石景山区康乐居4号楼202", 3, "110101194602140015", "孙丽", "13900139006", "B型", "无", "慢性支气管炎", LocalDateTime.now()},
                {7, "周玉珍", "女", LocalDate.of(1949,12,25), "13800138007", "北京市通州区福瑞园7号楼301", 1, "110101194912250022", "周涛", "13900139007", "O型", "无", "无", LocalDateTime.now()},
                {8, "吴天明", "男", LocalDate.of(1943,8,7), "13800138008", "北京市大兴区久安里8号楼501", 2, "110101194308070016", "吴娟", "13900139008", "A型", "花粉过敏", "痛风", LocalDateTime.now()},
                {9, "郑丽华", "女", LocalDate.of(1950,4,20), "13800138009", "北京市昌平区康宁院9号楼102", 3, "110101195004200028", "郑浩", "13900139009", "AB型", "无", "颈椎病", LocalDateTime.now()},
                {10, "黄永年", "男", LocalDate.of(1947,6,12), "13800138010", "北京市顺义区和煦居10号楼401", 1, "110101194706120014", "黄静", "13900139010", "B型", "无", "高血糖", LocalDateTime.now()},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Elders inserted: " + count);
        }
    }
    
    static void insertServiceOrders(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO service_order (id, elder_id, elder_name, worker_id, worker_name, service_type, service_date, duration, address, price, status, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {4, 4L, "刘德福", 1L, "王芳", "上门体检", LocalDateTime.of(2026,7,4,8,0), 60, "北京市西城区温馨家园1号楼401", 100.00, "已完成", LocalDateTime.now()},
                {5, 5L, "赵淑芬", 2L, "李强", "康复理疗", LocalDateTime.of(2026,7,5,15,0), 90, "北京市丰台区长寿阁6号楼103", 200.00, "服务中", LocalDateTime.now()},
                {6, 6L, "孙长顺", 3L, "张红", "家政护工", LocalDateTime.of(2026,7,6,9,30), 120, "北京市石景山区康乐居4号楼202", 150.00, "待派单", LocalDateTime.now()},
                {7, 7L, "周玉珍", 1L, "王芳", "康复理疗", LocalDateTime.of(2026,7,7,10,0), 90, "北京市通州区福瑞园7号楼301", 200.00, "待派单", LocalDateTime.now()},
                {8, 8L, "吴天明", 2L, "李强", "上门体检", LocalDateTime.of(2026,7,8,8,30), 60, "北京市大兴区久安里8号楼501", 100.00, "待派单", LocalDateTime.now()},
                {9, 1L, "张秀英", 3L, "张红", "家政护工", LocalDateTime.of(2026,7,9,14,0), 180, "北京市朝阳区幸福里小区3号楼201", 200.00, "已取消", LocalDateTime.now()},
                {10, 4L, "刘德福", 1L, "王芳", "上门体检", LocalDateTime.of(2026,7,10,9,0), 60, "北京市西城区温馨家园1号楼401", 100.00, "已完成", LocalDateTime.now()},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Service orders inserted: " + count);
        }
    }
    
    static void insertProductOrders(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO product_order (id, user_id, user_name, product_id, product_name, quantity, total_price, address, status, payment_method, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {3, 3L, "王五", 3L, "轮椅", 2, 2560.00, "北京市东城区东直门", "待发货", "支付宝", LocalDateTime.now()},
                {4, 2L, "李四", 4L, "制氧机", 1, 2200.00, "北京市海淀区中关村", "配送中", "支付宝", LocalDateTime.now()},
                {5, 1L, "张三", 1L, "血压计", 1, 299.00, "北京市朝阳区团结湖小区", "已完成", "微信支付", LocalDateTime.now()},
                {6, 3L, "王五", 3L, "轮椅", 1, 1280.00, "北京市东城区东直门", "已完成", "支付宝", LocalDateTime.now()},
                {7, 4L, "赵六", 6L, "拐杖", 2, 198.00, "北京市西城区金融街", "待发货", "微信支付", LocalDateTime.now()},
                {8, 2L, "李四", 5L, "护理床", 1, 3500.00, "北京市海淀区中关村", "已完成", "余额支付", LocalDateTime.now()},
                {9, 5L, "孙七", 2L, "血糖仪", 1, 299.00, "北京市丰台区马家堡", "已完成", "微信支付", LocalDateTime.now()},
                {10, 1L, "张三", 4L, "制氧机", 1, 2200.00, "北京市朝阳区团结湖小区", "已完成", "支付宝", LocalDateTime.now()},
                {11, 4L, "赵六", 7L, "护理包", 3, 297.00, "北京市西城区金融街", "已完成", "微信支付", LocalDateTime.now()},
                {12, 5L, "孙七", 1L, "血压计", 2, 598.00, "北京市丰台区马家堡", "待发货", "余额支付", LocalDateTime.now()},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Product orders inserted: " + count);
        }
    }
    
    static void createHealthServiceOrderTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS health_service_order (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "elder_id BIGINT NOT NULL," +
            "elder_name VARCHAR(50)," +
            "service_name VARCHAR(100)," +
            "worker_id BIGINT," +
            "worker_name VARCHAR(50)," +
            "service_time DATETIME," +
            "duration INT," +
            "status VARCHAR(20) DEFAULT '待派单'," +
            "remark VARCHAR(255)," +
            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("health_service_order table created/verified");
        }
    }
    
    static void insertHealthServiceOrders(Connection conn) throws SQLException {
        String sql = "INSERT INTO health_service_order (elder_id, elder_name, service_name, worker_id, worker_name, service_time, duration, status, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1L, "张秀英", "每日血压监测", 1L, "王芳", LocalDateTime.of(2026,7,10,8,0), 30, "已完成", "血压稳定"},
                {2L, "王建国", "康复训练指导", 2L, "李强", LocalDateTime.of(2026,7,10,10,0), 60, "已完成", "康复进度良好"},
                {3L, "李桂兰", "上门护理", 3L, "张红", LocalDateTime.of(2026,7,10,14,0), 90, "服务中", "需要定期换药"},
                {4L, "刘德福", "定期体检", 1L, "王芳", LocalDateTime.of(2026,7,11,9,0), 45, "待派单", ""},
                {5L, "赵淑芬", "康复按摩", 2L, "李强", LocalDateTime.of(2026,7,11,15,0), 60, "待派单", "腰腿疼痛"},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                ps.executeUpdate();
                count++;
            }
            System.out.println("Health service orders inserted: " + count);
        }
    }
    
    static void insertStaff(Connection conn) throws SQLException {
        String sql = "INSERT INTO staff (name, staff_no, dept, position, phone, email, gender, entry_date, status, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"张伟", "EMP001", "护理部", "护理主管", "13700137001", "zhangwei@care.com", "男", LocalDate.of(2020,1,15), 1, "资深护理经验10年"},
                {"刘芳", "EMP002", "运营部", "运营专员", "13700137002", "liufang@care.com", "女", LocalDate.of(2021,3,20), 1, "负责活动策划执行"},
                {"陈明", "EMP003", "技术部", "技术经理", "13700137003", "chenming@care.com", "男", LocalDate.of(2019,8,1), 1, "系统开发与维护"},
                {"王丽", "EMP004", "客服部", "客服主管", "13700137004", "wangli@care.com", "女", LocalDate.of(2020,6,10), 1, "客户满意度评分4.8"},
                {"李强", "EMP005", "市场部", "市场专员", "13700137005", "liqiang@care.com", "男", LocalDate.of(2022,1,5), 1, "负责线上推广"},
                {"赵敏", "EMP006", "财务部", "财务会计", "13700137006", "zhaomin@care.com", "女", LocalDate.of(2020,9,15), 1, "财务核算专业"},
                {"孙磊", "EMP007", "人事部", "人事专员", "13700137007", "sunlei@care.com", "男", LocalDate.of(2021,11,1), 1, "招聘与培训"},
                {"周杰", "EMP008", "护理部", "高级护理员", "13700137008", "zhoujie@care.com", "男", LocalDate.of(2018,4,20), 1, "特级护理资质"},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Staff inserted: " + count);
        }
    }
    
    static void insertActivity(Connection conn) throws SQLException {
        String sql = "INSERT INTO activity (name, type, image_url, start_time, end_time, description, participant_count, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"重阳节敬老活动", "线下活动", "https://picsum.photos/800/400?random=1", LocalDateTime.of(2026,10,1,9,0), LocalDateTime.of(2026,10,7,18,0), "重阳节免费健康检查和理发服务", 128, 1},
                {"健康知识讲座", "健康讲座", "https://picsum.photos/800/400?random=2", LocalDateTime.of(2026,8,15,14,0), LocalDateTime.of(2026,8,15,17,0), "邀请三甲医院主任医师讲解老年慢性病管理", 86, 1},
                {"夏季纳凉晚会", "文艺活动", "https://picsum.photos/800/400?random=3", LocalDateTime.of(2026,7,20,19,0), LocalDateTime.of(2026,7,20,21,30), "社区居民文艺表演和互动游戏", 210, 1},
                {"免费体检月", "健康活动", "https://picsum.photos/800/400?random=4", LocalDateTime.of(2026,6,1,8,0), LocalDateTime.of(2026,6,30,18,0), "为60岁以上老人提供全面免费体检", 456, 0},
                {"端午包粽子大赛", "民俗活动", "https://picsum.photos/800/400?random=5", LocalDateTime.of(2026,5,30,10,0), LocalDateTime.of(2026,5,30,15,0), "端午节包粽子比赛，奖品丰富", 75, 0},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Activities inserted: " + count);
        }
    }
    
    static void insertBanner(Connection conn) throws SQLException {
        String sql = "INSERT INTO banner (title, image_url, link_url, position, sort, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"欢迎使用智慧养老系统", "https://picsum.photos/1200/400?random=10", "", "首页", 1, 1},
                {"重阳节敬老活动火热进行中", "https://picsum.photos/1200/400?random=11", "/operation/activity", "首页", 2, 1},
                {"免费体检月活动公告", "https://picsum.photos/1200/400?random=12", "/operation/evaluation", "首页", 3, 1},
                {"新用户注册送优惠券", "https://picsum.photos/1200/400?random=13", "/user/coupon", "首页", 4, 1},
                {"智慧养老 服务到家", "https://picsum.photos/1200/400?random=14", "", "登录页", 1, 1},
            };
            int count = 0;
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setObject(i+1, row[i]);
                try { ps.executeUpdate(); count++; } catch (SQLException ignored) {}
            }
            System.out.println("Banners inserted: " + count);
        }
    }
}
