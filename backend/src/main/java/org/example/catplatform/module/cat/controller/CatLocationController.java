package org.example.catplatform.module.cat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.CatLocation;
import org.example.catplatform.module.cat.service.CatLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cat-locations")
public class CatLocationController {

    @Autowired
    private CatLocationService catLocationService;

    /**
     * 获取猫咪的所有位置记录
     */
    @GetMapping("/cat/{catId}")
    public Result<List<CatLocation>> listByCat(@PathVariable Long catId) {
        LambdaQueryWrapper<CatLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatLocation::getCatId, catId)
                .orderByDesc(CatLocation::getIsCurrent)
                .orderByDesc(CatLocation::getCreateTime);
        List<CatLocation> list = catLocationService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 新增位置记录
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<CatLocation> add(@RequestBody CatLocation location) {
        // 如果设为当前地点，先将该猫咪的其他地点 is_current 置为0
        if (location.getIsCurrent() == 1) {
            LambdaQueryWrapper<CatLocation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CatLocation::getCatId, location.getCatId());
            CatLocation update = new CatLocation();
            update.setIsCurrent(0);
            catLocationService.update(update, wrapper);
        }
        catLocationService.save(location);
        return Result.success("新增成功", location);
    }

    /**
     * 更新位置记录
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<CatLocation> update(@PathVariable Long id, @RequestBody CatLocation location) {
        // 如果设为当前地点，先将其他地点置为0
        if (location.getIsCurrent() == 1) {
            LambdaQueryWrapper<CatLocation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CatLocation::getCatId, location.getCatId())
                    .ne(CatLocation::getId, id);
            CatLocation update = new CatLocation();
            update.setIsCurrent(0);
            catLocationService.update(update, wrapper);
        }
        location.setId(id);
        catLocationService.updateById(location);
        return Result.success("更新成功", location);
    }

    /**
     * 删除位置记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        catLocationService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 设置某条记录为当前常出没点
     */
    @PutMapping("/{id}/set-current")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> setCurrent(@PathVariable Long id) {
        CatLocation location = catLocationService.getById(id);
        if (location == null) {
            return Result.error("记录不存在");
        }
        // 先将该猫咪的其他记录 is_current 置0
        LambdaQueryWrapper<CatLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatLocation::getCatId, location.getCatId());
        CatLocation update = new CatLocation();
        update.setIsCurrent(0);
        catLocationService.update(update, wrapper);

        // 设置当前记录为1
        location.setIsCurrent(1);
        catLocationService.updateById(location);
        return Result.success("设置成功", null);
    }
}