package org.example.catplatform.module.cat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.cat.entity.CatLocation;
import org.example.catplatform.module.cat.mapper.CatLocationMapper;
import org.example.catplatform.module.cat.service.CatLocationService;
import org.springframework.stereotype.Service;

@Service
public class CatLocationServiceImpl extends ServiceImpl<CatLocationMapper, CatLocation> implements CatLocationService {
}