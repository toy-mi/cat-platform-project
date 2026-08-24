package org.example.catplatform.module.cat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.Cat;
import org.example.catplatform.module.cat.service.CatService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cats")
public class CatController {

    @Autowired
    private CatService catService;


    //
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 分页查询猫咪列表（公开接口）
     */
    @GetMapping
    public Result<Page<Cat>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,      // 关键词（姓名或品种）
                                  @RequestParam(required = false) String breed,        // 品种
                                  @RequestParam(required = false) Integer gender,      // 性别
                                  @RequestParam(required = false) Integer neuterStatus,// 绝育状态
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer adoptionStatus) {

        //
        String cacheKey = "cats:" + pageNum + ":" + pageSize + ":" +
                (keyword != null ? keyword : "") + ":" +
                (adoptionStatus != null ? adoptionStatus : "all");

        if (redisTemplate != null) {
            try {
                Page<Cat> cached = (Page<Cat>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    System.out.println("Redis缓存命中: " + cacheKey);
                    return Result.success(cached);
                }
            } catch (Exception e) {
                System.out.println("⚠ Redis不可用，直接查数据库");
            }
        }

        //

        Page<Cat> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Cat> wrapper = new LambdaQueryWrapper<>();
        // 关键词搜索（姓名或品种）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Cat::getName, keyword)
                    .or()
                    .like(Cat::getBreed, keyword));
        }
        // 品种精确匹配（如果关键词已处理品种，可二选一，此处为单独品种筛选）
        if (breed != null && !breed.trim().isEmpty()) {
            wrapper.eq(Cat::getBreed, breed);
        }
        if (gender != null) {
            wrapper.eq(Cat::getGender, gender);
        }
        if (neuterStatus != null) {
            wrapper.eq(Cat::getNeuterStatus, neuterStatus);
        }

        if (name != null && !name.isEmpty()) {
            wrapper.like(Cat::getName, name);
        }
        if (adoptionStatus != null) {
            wrapper.eq(Cat::getAdoptionStatus, adoptionStatus);
        }
        wrapper.orderByDesc(Cat::getCreateTime);
        catService.page(page, wrapper);

        //
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, page, 30, TimeUnit.MINUTES);
                System.out.println("Redis缓存已设置: " + cacheKey);
            } catch (Exception e) {
                System.out.println("⚠ Redis缓存设置失败");
            }
        }
        //

        return Result.success(page);
    }

    /**
     * 根据ID查询猫咪详情
     */
    @GetMapping("/{id}")
    public Result<Cat> getById(@PathVariable Long id) {
        Cat cat = catService.getById(id);
        return cat != null ? Result.success(cat) : Result.error("猫咪不存在");
    }

    /**
     * 新增猫咪（需要登录，志愿者及以上角色）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Cat> add(@RequestBody Cat cat) {
        // 获取当前登录用户ID
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        cat.setCreatorId(userDetails.getUserId());
        catService.save(cat);

        // 清除缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(redisTemplate.keys("cats:*"));
                System.out.println("🗑️ 已清除猫咪列表缓存");
            } catch (Exception e) {}
        }
        //
        return Result.success("添加成功", cat);
    }

    /**
     * 更新猫咪信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Cat> update(@PathVariable Long id, @RequestBody Cat cat) {
        Cat existingCat = catService.getById(id);
        if (existingCat == null) {
            return Result.error("猫咪不存在");
        }

        // 只更新有值的字段
        if (cat.getName() != null) {
            existingCat.setName(cat.getName());
        }
        if (cat.getAvatar() != null) {
            existingCat.setAvatar(cat.getAvatar());
        }
        if (cat.getBreed() != null) {
            existingCat.setBreed(cat.getBreed());
        }
        if (cat.getGender() != null) {
            existingCat.setGender(cat.getGender());
        }
        if (cat.getPersonality() != null) {
            existingCat.setPersonality(cat.getPersonality());
        }
        if (cat.getDescription() != null) {
            existingCat.setDescription(cat.getDescription());
        }
        if (cat.getHealthStatus() != null) {
            existingCat.setHealthStatus(cat.getHealthStatus());
        }
        if (cat.getNeuterStatus() != null) {
            existingCat.setNeuterStatus(cat.getNeuterStatus());
        }
        if (cat.getAdoptionStatus() != null) {
            existingCat.setAdoptionStatus(cat.getAdoptionStatus());
        }

        // 手动设置 updateTime，确保更新时间被更新
        existingCat.setUpdateTime(LocalDateTime.now());
        catService.updateById(existingCat);

        // 清除缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(redisTemplate.keys("cats:*"));
                System.out.println("🗑️ 已清除猫咪列表缓存");
            } catch (Exception e) {}
        }
        //

        return Result.success("更新成功", existingCat);
    }

    /**
     * 删除猫咪（物理删除，仅管理员可用）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        catService.removeById(id);

        // 清除缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(redisTemplate.keys("cats:*"));
                System.out.println("🗑️ 已清除猫咪列表缓存");
            } catch (Exception e) {}
        }
        //

        return Result.success("删除成功", null);
    }

    /**
     * 获取猫咪统计信息
     */
    @GetMapping("/statistics")
//    @PreAuthorize("hasRole('ADMIN')")   // 仅管理员可访问，如希望普通用户也能查看可去掉
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 猫咪总数
        long total = catService.count();
        result.put("total", total);

        // 2. 各领养状态数量
        List<Map<String, Object>> statusStats = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            LambdaQueryWrapper<Cat> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Cat::getAdoptionStatus, i);
            long count = catService.count(wrapper);
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("status", i);
                item.put("count", count);
                statusStats.add(item);
            }
        }
        result.put("statusStats", statusStats);

        // 3. 男女比例
        LambdaQueryWrapper<Cat> maleWrapper = new LambdaQueryWrapper<>();
        maleWrapper.eq(Cat::getGender, 1);
        long maleCount = catService.count(maleWrapper);
        LambdaQueryWrapper<Cat> femaleWrapper = new LambdaQueryWrapper<>();
        femaleWrapper.eq(Cat::getGender, 2);
        long femaleCount = catService.count(femaleWrapper);
        LambdaQueryWrapper<Cat> unknownWrapper = new LambdaQueryWrapper<>();
        unknownWrapper.eq(Cat::getGender, 0);
        long unknownCount = catService.count(unknownWrapper);
        result.put("genderStats", List.of(
                Map.of("gender", "公", "count", maleCount),
                Map.of("gender", "母", "count", femaleCount),
                Map.of("gender", "未知", "count", unknownCount)
        ));

        return Result.success(result);
    }
}