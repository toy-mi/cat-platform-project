package org.example.catplatform.module.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.task.entity.TaskApplication;
import org.example.catplatform.module.task.mapper.TaskApplicationMapper;
import org.example.catplatform.module.task.service.TaskApplicationService;
import org.springframework.stereotype.Service;

@Service
public class TaskApplicationServiceImpl extends ServiceImpl<TaskApplicationMapper, TaskApplication> implements TaskApplicationService {
}