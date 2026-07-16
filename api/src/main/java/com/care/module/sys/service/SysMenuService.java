package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.module.sys.entity.RoleMenu;
import com.care.module.sys.entity.SysMenu;
import com.care.module.sys.entity.UserRole;
import com.care.module.sys.mapper.RoleMenuMapper;
import com.care.module.sys.mapper.SysMenuMapper;
import com.care.module.sys.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Autowired
    private CacheHelper cacheHelper;

    public List<SysMenu> getTree() {
        Object cached = cacheHelper.get("sys:menu:tree");
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<SysMenu> tree = (List<SysMenu>) cached;
            return tree;
        }
        List<SysMenu> all = list();
        List<SysMenu> tree = buildTree(all, 0L);
        cacheHelper.put("sys:menu:tree", tree, CacheTtl.MENU_TREE);
        return tree;
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(buildTree(all, menu.getId())))
                .collect(Collectors.toList());
    }

    public List<SysMenu> getByRoleId(Long roleId) {
        return list();
    }

    public List<SysMenu> getUserMenus(Long userId) {
        List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String roleIdsStr = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Long> menuIds = roleMenuMapper.getMenuIdsByRoleIds(roleIdsStr);
        
        if (menuIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<SysMenu> menus = listByIds(menuIds);
        return buildTree(menus, 0L);
    }

    public void add(SysMenu menu) {
        save(menu);
        cacheHelper.evict("sys:menu:tree");
    }

    public void updateMenu(SysMenu menu) {
        updateById(menu);
        cacheHelper.evict("sys:menu:tree");
    }

    public void delete(Long id) {
        List<SysMenu> children = list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (!children.isEmpty()) {
            throw new RuntimeException("请先删除子菜单");
        }
        removeById(id);
        cacheHelper.evict("sys:menu:tree");
    }
}
