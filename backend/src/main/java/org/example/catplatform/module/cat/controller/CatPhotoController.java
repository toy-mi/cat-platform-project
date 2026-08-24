package org.example.catplatform.module.cat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.CatPhoto;
import org.example.catplatform.module.cat.service.CatPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cat-photos")
public class CatPhotoController {

    @Autowired
    private CatPhotoService catPhotoService;

    // 上传文件存储路径（从配置文件读取）
    @Value("${file.upload.path:uploads/cats/}")
    private String uploadPath;

    // 访问文件的 URL 前缀
    @Value("${file.access.prefix:/uploads/cats/}")
    private String accessPrefix;

    /**
     * 上传猫咪照片
     */
    @PostMapping("/upload/{catId}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<CatPhoto> upload(@PathVariable Long catId,
                                   @RequestParam("file") MultipartFile file,
                                   HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + suffix;

            // 创建存储目录
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            System.out.println("File saved to: " + dest.getAbsolutePath());

            // 构建文件访问 URL（需要配置静态资源映射）
            String fileUrl = accessPrefix + fileName;

            // 保存到数据库
            CatPhoto catPhoto = new CatPhoto();
            catPhoto.setCatId(catId);
            catPhoto.setPhotoUrl(fileUrl);
            // 获取当前最大排序号
            LambdaQueryWrapper<CatPhoto> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CatPhoto::getCatId, catId).orderByDesc(CatPhoto::getSortOrder).last("LIMIT 1");
            CatPhoto maxSort = catPhotoService.getOne(wrapper);
            int sortOrder = (maxSort == null || maxSort.getSortOrder() == null) ? 0 : maxSort.getSortOrder() + 1;
            catPhoto.setSortOrder(sortOrder);

            catPhotoService.save(catPhoto);

            return Result.success("上传成功", catPhoto);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }

    /**
     * 获取猫咪照片列表
     */
    @GetMapping("/list/{catId}")
    public Result<List<CatPhoto>> list(@PathVariable Long catId) {
        LambdaQueryWrapper<CatPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatPhoto::getCatId, catId)
                .orderByAsc(CatPhoto::getSortOrder);
        List<CatPhoto> list = catPhotoService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 删除照片
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 先查询照片信息，获取文件路径
        CatPhoto catPhoto = catPhotoService.getById(id);
        if (catPhoto == null) {
            return Result.error("照片不存在");
        }

        // 删除文件
        String fileUrl = catPhoto.getPhotoUrl();
        // 从 URL 中提取文件名（假设访问前缀与存储路径对应）
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        File file = new File(uploadPath + fileName);
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        catPhotoService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新照片排序（可选，可批量）
     */
    @PutMapping("/sort")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> updateSort(@RequestBody List<CatPhoto> photos) {
        for (CatPhoto photo : photos) {
            catPhotoService.updateById(photo);
        }
        return Result.success("排序更新成功", null);
    }
}