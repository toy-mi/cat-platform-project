package org.example.catplatform.module.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.task.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}