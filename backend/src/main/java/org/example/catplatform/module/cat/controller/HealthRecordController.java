package org.example.catplatform.module.cat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.HealthRecord;
import org.example.catplatform.module.cat.service.HealthRecordService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    /**
     * 获取某猫咪的所有健康记录
     */
    @GetMapping("/cat/{catId}")
    public Result<List<HealthRecord>> listByCat(@PathVariable Long catId) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getCatId, catId).orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> list = healthRecordService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 新增健康记录（需要志愿者或管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<HealthRecord> add(@RequestBody HealthRecord record) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        record.setCreateBy(userDetails.getUserId());
        healthRecordService.save(record);
        return Result.success("新增成功", record);
    }

    /**
     * 更新健康记录
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<HealthRecord> update(@PathVariable Long id, @RequestBody HealthRecord record) {
        record.setId(id);
        healthRecordService.updateById(record);
        return Result.success("更新成功", record);
    }

    /**
     * 删除健康记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        healthRecordService.removeById(id);
        return Result.success("删除成功", null);
    }
}