package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.module.sys.entity.RoleMenu;
import com.care.module.sys.entity.SysRole;
import com.care.module.sys.mapper.RoleMenuMapper;
import com.care.module.sys.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private CacheHelper cacheHelper;

    public IPage<SysRole> getPage(Integer pageNum, Integer pageSize, String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return page(page, wrapper);
    }

    public List<SysRole> getAll() {
        Object cached = cacheHelper.get("sys:role:all");
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<SysRole> all = (List<SysRole>) cached;
            return all;
        }
        List<SysRole> all = list();
        cacheHelper.put("sys:role:all", all, CacheTtl.ROLE_ALL);
        return all;
    }

    public void add(SysRole role) {
        save(role);
        cacheHelper.evict("sys:role:all");
    }

    public void updateRole(SysRole role) {
        updateById(role);
        cacheHelper.evict("sys:role:all");
    }

    public void delete(Long id) {
        removeById(id);
        cacheHelper.evict("sys:role:all");
    }

    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.getMenuIdsByRoleId(roleId);
    }

    @Transactional
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(wrapper);

        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
