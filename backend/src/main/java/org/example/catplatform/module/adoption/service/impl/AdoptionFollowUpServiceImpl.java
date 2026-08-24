package org.example.catplatform.module.adoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.adoption.entity.AdoptionFollowUp;
import org.example.catplatform.module.adoption.mapper.AdoptionFollowUpMapper;
import org.example.catplatform.module.adoption.service.AdoptionFollowUpService;
import org.springframework.stereotype.Service;

@Service
public class AdoptionFollowUpServiceImpl extends ServiceImpl<AdoptionFollowUpMapper, AdoptionFollowUp> implements AdoptionFollowUpService {
}