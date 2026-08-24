package org.example.catplatform.module.cat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.cat.entity.CatPhoto;
import org.example.catplatform.module.cat.mapper.CatPhotoMapper;
import org.example.catplatform.module.cat.service.CatPhotoService;
import org.springframework.stereotype.Service;

@Service
public class CatPhotoServiceImpl extends ServiceImpl<CatPhotoMapper, CatPhoto> implements CatPhotoService {
}