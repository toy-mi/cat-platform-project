package org.example.catplatform.module.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.task.entity.Task;
import org.example.catplatform.module.task.mapper.TaskMapper;
import org.example.catplatform.module.task.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
}