package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.sys.entity.SysRole;
import com.care.module.sys.entity.SysUser;
import com.care.module.sys.entity.UserRole;
import com.care.module.sys.mapper.SysRoleMapper;
import com.care.module.sys.mapper.SysUserMapper;
import com.care.module.sys.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    public IPage<SysUser> getPage(Integer pageNum, Integer pageSize, String username, String realName) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(SysUser::getRealName, realName);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> result = page(page, wrapper);
        
        for (SysUser user : result.getRecords()) {
            List<Long> roleIds = userRoleMapper.getRoleIdsByUserId(user.getId());
            if (!roleIds.isEmpty()) {
                user.setRoleId(roleIds.get(0));
                SysRole role = sysRoleMapper.selectById(roleIds.get(0));
                if (role != null) {
                    user.setRoleName(role.getRoleName());
                }
            }
        }
        
        return result;
    }

    public SysUser login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 支持用户名或手机号登录
        wrapper.eq(SysUser::getUsername, username).or().eq(SysUser::getPhone, username);
        wrapper.last("LIMIT 1");
        SysUser user = getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    @Transactional
    public void add(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (getOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        save(user);
        
        if (user.getRoleId() != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(user.getRoleId());
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void updateUser(SysUser user) {
        SysUser existing = getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        if (StringUtils.hasText(user.getPassword())) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword()); // 编辑时密码留空则不修改，避免被清空
        }
        updateById(user);
        
        if (user.getRoleId() != null) {
            LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(UserRole::getUserId, user.getId());
            userRoleMapper.delete(urWrapper);
            
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(user.getRoleId());
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void resetPassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        updateById(user);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
