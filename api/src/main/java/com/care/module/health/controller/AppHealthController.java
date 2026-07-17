package com.care.module.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.Disease;
import com.care.module.health.entity.Elder;
import com.care.module.health.entity.ElderFamily;
import com.care.module.health.entity.HealthRecord;
import com.care.module.health.service.AiAdviceCacheService;
import com.care.module.health.service.DiseaseService;
import com.care.module.health.service.ElderFamilyService;
import com.care.module.health.service.ElderService;
import com.care.module.health.service.HealthRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

/**
 * C 端普通用户 —— 健康档案（读 + 自助维护）。
 * 所有涉及具体老人的接口都做归属校验：只有档案关联的普通用户本人才能查看/修改/删除，
 * 与管理员后台 /api/health/*（可看全部老人）完全隔离。新增老人时 appUserId 强制为当前登录用户，
 * 客户端无法冒充他人归属，杜绝越权。
 */
@RestController
@RequestMapping("/api/app/health")
@Api(tags = "C端用户-健康档案")
public class AppHealthController {

    @Autowired
    private ElderService elderService;
    @Autowired
    private HealthRecordService healthRecordService;
    @Autowired
    private DiseaseService diseaseService;
    @Autowired
    private ElderFamilyService elderFamilyService;
    @Autowired
    private AiAdviceCacheService aiAdviceCacheService;

    /** 取当前登录的普通用户ID（由 JWT 过滤器写入 request 的 userId 属性） */
    private Long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        return v == null ? null : (Long) v;
    }

    /**
     * 归属校验：返回老人表示通过；返回 null 表示“不存在或无权限”（统一按 403 处理）。
     */
    private Elder checkOwnership(Long elderId, Long userId) {
        Elder elder = elderService.getById(elderId);
        if (elder == null) {
            return null;
        }
        if (elder.getAppUserId() == null || !elder.getAppUserId().equals(userId)) {
            return null;
        }
        return elder;
    }

    @ApiOperation("获取我关联的老人档案列表")
    @GetMapping("/elders")
    public Result<List<Elder>> myElders(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        List<Elder> list = elderService.lambdaQuery()
                .eq(Elder::getAppUserId, userId)
                .orderByDesc(Elder::getId)
                .list();
        return Result.success(list);
    }

    @ApiOperation("获取单个老人档案详情（需归属本人）")
    @GetMapping("/elder/{id}")
    public Result<Elder> elderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Elder elder = checkOwnership(id, userId);
        if (elder == null) {
            return Result.forbidden("无权查看该档案");
        }
        return Result.success(elder);
    }

    @ApiOperation("分页查询健康记录（需归属本人）")
    @GetMapping("/records")
    public Result<PageResult<HealthRecord>> records(
            @RequestParam Long elderId,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (checkOwnership(elderId, userId) == null) {
            return Result.forbidden("无权查看该档案");
        }
        IPage<HealthRecord> page = healthRecordService.getPage(pageNum, pageSize, elderId, recordType, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("健康档案-家属列表（需归属本人）")
    @GetMapping("/family")
    public Result<List<ElderFamily>> family(@RequestParam Long elderId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (checkOwnership(elderId, userId) == null) {
            return Result.forbidden("无权查看该档案");
        }
        return Result.success(elderFamilyService.getByElderId(elderId));
    }

    @ApiOperation("新增家属（需归属本人）")
    @PostMapping("/family")
    public Result<ElderFamily> addFamily(@RequestBody ElderFamily family, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (family == null || family.getElderId() == null
                || !StringUtils.hasText(family.getFamilyName())) {
            return Result.error("请填写老人档案和家属姓名");
        }
        if (checkOwnership(family.getElderId(), userId) == null) {
            return Result.forbidden("无权为该档案添加家属");
        }
        family.setId(null);
        elderFamilyService.add(family);
        return Result.success(family);
    }

    @ApiOperation("修改家属（需归属本人）")
    @PutMapping("/family/{id}")
    public Result<Void> updateFamily(@PathVariable Long id, @RequestBody ElderFamily body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        ElderFamily family = elderFamilyService.getById(id);
        if (family == null) {
            return Result.error("家属不存在");
        }
        if (checkOwnership(family.getElderId(), userId) == null) {
            return Result.forbidden("无权修改该家属");
        }
        body.setId(id);
        body.setElderId(family.getElderId());
        elderFamilyService.updateFamily(body);
        return Result.ok("Update success");
    }

    @ApiOperation("删除家属（需归属本人）")
    @DeleteMapping("/family/{id}")
    public Result<Void> deleteFamily(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        ElderFamily family = elderFamilyService.getById(id);
        if (family == null) {
            return Result.ok("Delete success");
        }
        if (checkOwnership(family.getElderId(), userId) == null) {
            return Result.forbidden("无权删除该家属");
        }
        elderFamilyService.delete(id);
        return Result.ok("Delete success");
    }

    @ApiOperation("健康科普-疾病宝典（只读，按状态筛选，不区分归属）")
    @GetMapping("/diseases")
    public Result<List<Disease>> diseases(@RequestParam(required = false, defaultValue = "1") Integer status) {
        return Result.success(diseaseService.getAll(status));
    }

    // ===================== 自助维护：老人档案 =====================

    @ApiOperation("新增我的老人档案（自动归属当前登录用户，禁止冒充他人）")
    @PostMapping("/elder")
    public Result<Elder> addElder(@RequestBody Elder elder, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (elder == null || !StringUtils.hasText(elder.getName()) || elder.getGender() == null) {
            return Result.error("请填写老人姓名和性别");
        }
        elder.setId(null);
        elder.setAppUserId(userId);          // 强制归属当前用户
        if (elder.getStatus() == null) {
            elder.setStatus(1);
        }
        elderService.add(elder);
        return Result.success(elder);
    }

    @ApiOperation("修改我的老人档案（需归属本人）")
    @PutMapping("/elder/{id}")
    public Result<Void> updateElder(@PathVariable Long id, @RequestBody Elder body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Elder owned = checkOwnership(id, userId);
        if (owned == null) {
            return Result.forbidden("无权修改该档案");
        }
        // 仅允许更新业务字段，归属(appUserId)、主键、审计时间保持不变
        owned.setName(body.getName());
        owned.setGender(body.getGender());
        owned.setAge(body.getAge());
        owned.setIdCard(body.getIdCard());
        owned.setPhone(body.getPhone());
        owned.setHealthLevel(body.getHealthLevel());
        owned.setEmergencyContact(body.getEmergencyContact());
        owned.setEmergencyPhone(body.getEmergencyPhone());
        owned.setAddress(body.getAddress());
        elderService.updateElder(owned);
        return Result.ok("Update success");
    }

    @ApiOperation("删除我的老人档案（需归属本人）")
    @DeleteMapping("/elder/{id}")
    public Result<Void> deleteElder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Elder owned = checkOwnership(id, userId);
        if (owned == null) {
            return Result.forbidden("无权删除该档案");
        }
        elderService.delete(id);
        aiAdviceCacheService.evictForElder(id);  // 同步清理其 AI 建议缓存
        return Result.ok("Delete success");
    }

    // ===================== 自助维护：健康记录 =====================

    @ApiOperation("新增健康记录（需归属本人；自动填充老人姓名；记录时间缺省取当前时间）")
    @PostMapping("/record")
    public Result<HealthRecord> addRecord(@RequestBody HealthRecord record, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (record == null || record.getElderId() == null
                || !StringUtils.hasText(record.getRecordType())
                || !StringUtils.hasText(record.getRecordValue())) {
            return Result.error("请填写记录类型与记录值");
        }
        Elder owned = checkOwnership(record.getElderId(), userId);
        if (owned == null) {
            return Result.forbidden("无权为该老人添加记录");
        }
        record.setId(null);
        record.setElderName(owned.getName());   // 自动补全老人姓名，避免前后端不一致
        healthRecordService.add(record);        // 内部：recordTime 为空则取当前时间
        aiAdviceCacheService.evictForElder(record.getElderId());  // 档案变更后失效 AI 缓存
        return Result.success(record);
    }

    @ApiOperation("删除健康记录（需归属本人）")
    @DeleteMapping("/record/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        HealthRecord record = healthRecordService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        if (checkOwnership(record.getElderId(), userId) == null) {
            return Result.forbidden("无权删除该记录");
        }
        healthRecordService.delete(id);
        aiAdviceCacheService.evictForElder(record.getElderId());
        return Result.ok("Delete success");
    }
}
