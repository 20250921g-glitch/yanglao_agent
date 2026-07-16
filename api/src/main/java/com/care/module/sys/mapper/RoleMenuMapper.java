package com.care.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.sys.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id IN (${roleIds})")
    List<Long> getMenuIdsByRoleIds(@Param("roleIds") String roleIds);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> getMenuIdsByRoleId(@Param("roleId") Long roleId);

    void batchInsert(@Param("list") List<RoleMenu> list);
}