package com.care.config;

import com.care.module.sys.entity.RoleMenu;
import com.care.module.sys.entity.UserRole;
import com.care.module.sys.mapper.RoleMenuMapper;
import com.care.module.sys.mapper.UserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initUserRoles();
        initRoleMenus();
    }

    private void initUserRoles() {
        long count = userRoleMapper.selectCount(null);
        if (count == 0) {
            log.info("Initializing sys_user_role data...");
            UserRole ur1 = new UserRole();
            ur1.setUserId(1L);
            ur1.setRoleId(1L);
            userRoleMapper.insert(ur1);

            UserRole ur2 = new UserRole();
            ur2.setUserId(2L);
            ur2.setRoleId(2L);
            userRoleMapper.insert(ur2);
            log.info("sys_user_role initialized successfully");
        }
    }

    private void initRoleMenus() {
        long count = roleMenuMapper.selectCount(null);
        if (count == 0) {
            log.info("Initializing sys_role_menu data...");
            long[] superAdminMenus = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
            long[] operatorMenus = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

            for (long menuId : superAdminMenus) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(1L);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }

            for (long menuId : operatorMenus) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(2L);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
            log.info("sys_role_menu initialized successfully");
        }
    }
}