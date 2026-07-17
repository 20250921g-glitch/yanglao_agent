package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.ElderFamily;
import com.care.module.health.entity.Elder;
import com.care.module.health.mapper.ElderFamilyMapper;
import com.care.module.user.entity.AppUser;
import com.care.module.user.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ElderFamilyService extends ServiceImpl<ElderFamilyMapper, ElderFamily> {

    @Autowired
    private ElderService elderService;
    @Autowired
    private AppUserService appUserService;

    public List<ElderFamily> getByElderId(Long elderId) {
        return list(new LambdaQueryWrapper<ElderFamily>().eq(ElderFamily::getElderId, elderId));
    }

    public List<ElderFamily> getAllList() {
        List<ElderFamily> families = list();
        if (families.isEmpty()) {
            return families;
        }
        // 关联老人姓名
        List<Long> elderIds = families.stream().map(ElderFamily::getElderId).distinct().collect(Collectors.toList());
        Map<Long, String> elderNameMap = elderService.listByIds(elderIds).stream()
                .collect(Collectors.toMap(Elder::getId, Elder::getName, (a, b) -> a));
        // 关联家属用户姓名/电话（保证与真实用户一致，避免"和用户对不上"）
        List<Long> userIds = families.stream().map(ElderFamily::getAppUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, AppUser> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : appUserService.listByIds(userIds).stream().collect(Collectors.toMap(AppUser::getId, u -> u, (a, b) -> a));
        for (ElderFamily f : families) {
            f.setElderName(elderNameMap.getOrDefault(f.getElderId(), "—"));
            AppUser u = userMap.get(f.getAppUserId());
            if (u != null) {
                String name = (u.getRealName() != null && !u.getRealName().isEmpty()) ? u.getRealName() : u.getUsername();
                f.setFamilyName(name);
                f.setPhone(u.getPhone());
                f.setUserName(name);
                f.setUserPhone(u.getPhone());
            }
        }
        return families;
    }

    // 新增/编辑时：若关联了真实家属账号，则姓名与电话以该账号为准
    private void fillFromUser(ElderFamily family) {
        if (family.getAppUserId() != null) {
            AppUser u = appUserService.getById(family.getAppUserId());
            if (u != null) {
                String name = (u.getRealName() != null && !u.getRealName().isEmpty()) ? u.getRealName() : u.getUsername();
                family.setFamilyName(name);
                family.setPhone(u.getPhone());
            }
        }
    }

    public void add(ElderFamily family) {
        fillFromUser(family);
        save(family);
    }

    public void updateFamily(ElderFamily family) {
        fillFromUser(family);
        updateById(family);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
