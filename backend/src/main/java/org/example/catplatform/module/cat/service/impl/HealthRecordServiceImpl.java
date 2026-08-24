package org.example.catplatform.module.cat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.cat.entity.HealthRecord;
import org.example.catplatform.module.cat.mapper.HealthRecordMapper;
import org.example.catplatform.module.cat.service.HealthRecordService;
import org.springframework.stereotype.Service;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {
}