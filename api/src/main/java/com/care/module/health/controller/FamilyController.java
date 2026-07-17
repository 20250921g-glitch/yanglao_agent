package com.care.module.health.controller;

import com.care.common.result.Result;
import com.care.module.health.entity.ElderFamily;
import com.care.module.health.service.ElderFamilyService;
import com.care.module.user.entity.AppUser;
import com.care.module.user.service.AppUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Health-Family")
@RestController
@RequestMapping("/api/health/family")
public class FamilyController {

    @Autowired
    private ElderFamilyService familyService;
    @Autowired
    private AppUserService appUserService;

    @ApiOperation("Query by elder id")
    @GetMapping("/list/{elderId}")
    public Result<List<ElderFamily>> getByElderId(@PathVariable Long elderId) {
        return Result.success(familyService.getByElderId(elderId));
    }

    @ApiOperation("All family list")
    @GetMapping("/all")
    public Result<List<ElderFamily>> getAll() {
        return Result.success(familyService.getAllList());
    }

    @ApiOperation("家属候选账号(FAMILY角色)")
    @GetMapping("/candidates")
    public Result<List<AppUser>> candidates() {
        return Result.success(appUserService.listByRole("FAMILY"));
    }

    @ApiOperation("Add family")
    @PostMapping
    public Result<Void> add(@RequestBody ElderFamily family) {
        familyService.add(family);
        return Result.ok("Add success");
    }

    @ApiOperation("Update family")
    @PutMapping
    public Result<Void> update(@RequestBody ElderFamily family) {
        familyService.updateFamily(family);
        return Result.ok("Update success");
    }

    @ApiOperation("Delete family")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        familyService.delete(id);
        return Result.ok("Delete success");
    }
}
