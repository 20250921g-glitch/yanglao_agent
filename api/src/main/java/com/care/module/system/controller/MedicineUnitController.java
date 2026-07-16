package com.care.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.system.entity.MedicineUnit;
import com.care.module.system.service.MedicineUnitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统管理-药品单位")
@RestController
@RequestMapping("/api/system/medicine-unit")
public class MedicineUnitController {

    @Autowired
    private MedicineUnitService medicineUnitService;

    @ApiOperation("分页查询药品单位")
    @GetMapping("/page")
    public Result<PageResult<MedicineUnit>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<MedicineUnit> page = medicineUnitService.getPage(pageNum, pageSize, keyword);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增单位")
    @PostMapping
    public Result<Void> add(@RequestBody MedicineUnit u) {
        medicineUnitService.saveUnit(u);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改单位")
    @PutMapping
    public Result<Void> update(@RequestBody MedicineUnit u) {
        medicineUnitService.saveUnit(u);
        return Result.ok("修改成功");
    }

    @ApiOperation("启用/停用单位")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        medicineUnitService.updateStatus(id, status);
        return Result.ok("状态已更新");
    }

    @ApiOperation("删除单位")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        medicineUnitService.removeById(id);
        return Result.ok("删除成功");
    }
}
