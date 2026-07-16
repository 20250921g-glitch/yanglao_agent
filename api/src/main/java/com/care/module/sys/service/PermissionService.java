package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.care.module.sys.entity.RoleMenu;
import com.care.module.sys.entity.SysMenu;
import com.care.module.sys.entity.UserRole;
import com.care.module.sys.mapper.RoleMenuMapper;
import com.care.module.sys.mapper.SysMenuMapper;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.module.sys.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private CacheHelper cacheHelper;

    /** 角色缓存：每用户每 30 分钟查一次库，避免每次请求都查 user_role 表 */
    private static final String ROLES_KEY = "user:roles:";

    public List<String> getUserRoles(Long userId) {
        String key = ROLES_KEY + userId;
        Object cached = cacheHelper.get(key);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) cached;
            return roles;
        }
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        List<String> roles = roleIds.stream().map(id -> "ROLE_" + id).collect(Collectors.toList());
        cacheHelper.put(key, roles, CacheTtl.ROLE_AUTH);
        return roles;
    }

    public List<Long> getUserMenuIds(Long userId) {
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String roleIdsStr = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return roleMenuMapper.getMenuIdsByRoleIds(roleIdsStr);
    }

    /**
     * 获取用户按钮级权限码集合（union of sys_menu.perms，按角色可访问菜单聚合）。
     */
    public List<String> getUserPerms(Long userId) {
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        String roleIdsStr = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Long> menuIds = roleMenuMapper.getMenuIdsByRoleIds(roleIdsStr);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> menus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .isNotNull(SysMenu::getPerms)
                        .ne(SysMenu::getPerms, ""));
        List<String> perms = new ArrayList<>();
        for (SysMenu m : menus) {
            for (String p : m.getPerms().split(",")) {
                String t = p.trim();
                if (!t.isEmpty()) perms.add(t);
            }
        }
        return perms.stream().distinct().collect(Collectors.toList());
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        // 角色变更后失效缓存，下次请求重新加载
        cacheHelper.evict(ROLES_KEY + userId);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }
}