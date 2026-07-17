package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.AppUser;
import com.care.module.user.entity.MemberLevel;
import com.care.module.user.entity.UserTag;
import com.care.module.user.entity.UserTagRelation;
import com.care.module.user.mapper.AppUserMapper;
import com.care.module.user.mapper.MemberLevelMapper;
import com.care.module.user.mapper.UserTagMapper;
import com.care.module.user.mapper.UserTagRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.care.module.user.dto.AppUserLoginDTO;
import com.care.module.user.dto.AppUserRegisterDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AppUserService extends ServiceImpl<AppUserMapper, AppUser> {

    @Autowired
    private UserTagRelationMapper userTagRelationMapper;
    @Autowired
    private UserTagMapper userTagMapper;
    @Autowired
    private MemberLevelMapper memberLevelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private SmsService smsService;

    public IPage<AppUser> getPage(Integer pageNum, Integer pageSize, String username, String realName, String phone,
                                   Integer status, String startDate, String endDate) {
        Page<AppUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(AppUser::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(AppUser::getRealName, realName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.eq(AppUser::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(AppUser::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(AppUser::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(AppUser::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(AppUser::getCreateTime);
        IPage<AppUser> result = page(page, wrapper);
        fillTags(result.getRecords());
        return result;
    }

    // P4-3：列表页批量聚合用户标签（避免 N+1）
    private void fillTags(List<AppUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(AppUser::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(UserTagRelation::getUserId, userIds);
        List<UserTagRelation> relations = userTagRelationMapper.selectList(relWrapper);
        if (relations.isEmpty()) {
            return;
        }
        Map<Long, List<Long>> userIdToTagIds = relations.stream()
                .collect(Collectors.groupingBy(UserTagRelation::getUserId,
                        Collectors.mapping(UserTagRelation::getTagId, Collectors.toList())));
        List<Long> allTagIds = relations.stream().map(UserTagRelation::getTagId).distinct().collect(Collectors.toList());
        Map<Long, String> tagIdToName = userTagMapper.selectList(new LambdaQueryWrapper<UserTag>().in(UserTag::getId, allTagIds))
                .stream().collect(Collectors.toMap(UserTag::getId, UserTag::getTagName));
        for (AppUser user : users) {
            List<Long> tagIds = userIdToTagIds.get(user.getId());
            if (tagIds != null && !tagIds.isEmpty()) {
                user.setTags(tagIds.stream().map(tagIdToName::get).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }
    }

    public AppUser getDetail(Long id) {
        AppUser user = getById(id);
        if (user == null) {
            return null;
        }
        // 填充等级名称
        if (user.getLevelId() != null) {
            MemberLevel level = memberLevelMapper.selectById(user.getLevelId());
            if (level != null) {
                user.setLevelName(level.getLevelName());
            }
        }
        // 填充标签
        LambdaQueryWrapper<UserTagRelation> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(UserTagRelation::getUserId, id);
        List<UserTagRelation> relations = userTagRelationMapper.selectList(tagWrapper);
        if (!relations.isEmpty()) {
            List<Long> tagIds = relations.stream().map(UserTagRelation::getTagId).collect(Collectors.toList());
            LambdaQueryWrapper<UserTag> tagQueryWrapper = new LambdaQueryWrapper<>();
            tagQueryWrapper.in(UserTag::getId, tagIds);
            List<UserTag> tags = userTagMapper.selectList(tagQueryWrapper);
            user.setTags(tags.stream().map(UserTag::getTagName).collect(Collectors.toList()));
        }
        return user;
    }

    public List<AppUser> listByRole(String role) {
        return list(new LambdaQueryWrapper<AppUser>().eq(AppUser::getRole, role).orderByAsc(AppUser::getId));
    }

    public void add(AppUser user) {
        save(user);
    }

    public void updateUser(AppUser user) {
        updateById(user);
    }

    public void updateStatus(Long id, Integer status) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setStatus(status);
        updateById(user);
    }

    public void delete(Long id) {
        removeById(id);
        // 删除用户标签关系
        LambdaQueryWrapper<UserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTagRelation::getUserId, id);
        userTagRelationMapper.delete(wrapper);
    }

    public List<UserTag> getUserTags(Long userId) {
        LambdaQueryWrapper<UserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTagRelation::getUserId, userId);
        List<UserTagRelation> relations = userTagRelationMapper.selectList(wrapper);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = relations.stream().map(UserTagRelation::getTagId).collect(Collectors.toList());
        return userTagMapper.selectList(new LambdaQueryWrapper<UserTag>().in(UserTag::getId, tagIds));
    }

    public void updateUserTags(Long userId, List<Long> tagIds) {
        // 删除旧关系
        LambdaQueryWrapper<UserTagRelation> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(UserTagRelation::getUserId, userId);
        userTagRelationMapper.delete(delWrapper);
        // 新增新关系
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                UserTagRelation relation = new UserTagRelation();
                relation.setUserId(userId);
                relation.setTagId(tagId);
                userTagRelationMapper.insert(relation);
            }
        }
    }

    /**
     * 普通用户注册（C端）。需图形验证码 + 短信验证码，密码 BCrypt 加密，来源标记为 APP注册。
     */
    public AppUser register(AppUserRegisterDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()
                || dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new RuntimeException("手机号和密码不能为空");
        }
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new RuntimeException("图形验证码错误");
        }
        if (!smsService.verify(dto.getPhone(), dto.getSmsCode())) {
            throw new RuntimeException("短信验证码错误或已过期");
        }
        String phone = dto.getPhone().trim();
        LambdaQueryWrapper<AppUser> w = new LambdaQueryWrapper<>();
        w.eq(AppUser::getPhone, phone);
        if (this.count(w) > 0) {
            throw new RuntimeException("该手机号已注册");
        }
        AppUser user = new AppUser();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        String nick = dto.getNickname();
        user.setUsername(nick != null && !nick.trim().isEmpty() ? nick.trim() : "用户" + phone);
        user.setSource("APP注册");
        user.setStatus(1);
        // 用户类型：校验为合法枚举，否则默认 FAMILY 家属
        String role = dto.getRole();
        if (role == null || !(role.equals("ELDER") || role.equals("FAMILY")
                || role.equals("VOLUNTEER") || role.equals("STAFF"))) {
            role = "FAMILY";
        }
        user.setRole(role);
        this.save(user);
        return user;
    }

    /**
     * 普通用户登录（C端）。需图形验证码；密码错误累计 5 次锁定 15 分钟。
     */
    public AppUser login(AppUserLoginDTO dto) {
        String phone = dto.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        Object lock = redisTemplate.opsForValue().get("login:lock:" + phone);
        if (lock != null) {
            throw new RuntimeException("账号已锁定，请15分钟后再试");
        }
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new RuntimeException("图形验证码错误");
        }
        LambdaQueryWrapper<AppUser> w = new LambdaQueryWrapper<>();
        w.eq(AppUser::getPhone, phone);
        AppUser user = this.getOne(w);
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (user.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            Long fails = redisTemplate.opsForValue().increment("login:fail:" + phone);
            if (fails == null) {
                fails = 1L;
            }
            redisTemplate.expire("login:fail:" + phone, 15, TimeUnit.MINUTES);
            if (fails >= 5) {
                redisTemplate.opsForValue().set("login:lock:" + phone, "1", 15, TimeUnit.MINUTES);
                redisTemplate.delete("login:fail:" + phone);
                throw new RuntimeException("密码错误次数过多，账号已锁定15分钟");
            }
            throw new RuntimeException("密码错误，还可尝试 " + (5 - fails) + " 次");
        }
        redisTemplate.delete("login:fail:" + phone);
        redisTemplate.delete("login:lock:" + phone);
        return user;
    }
}
