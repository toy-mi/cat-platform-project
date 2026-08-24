package org.example.catplatform.module.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.adoption.entity.AdoptionApplication;
import org.example.catplatform.module.adoption.mapper.AdoptionApplicationMapper;
import org.example.catplatform.module.adoption.service.AdoptionApplicationService;
import org.springframework.stereotype.Service;

@Service
public class AdoptionApplicationServiceImpl extends ServiceImpl<AdoptionApplicationMapper, AdoptionApplication> implements AdoptionApplicationService {
}