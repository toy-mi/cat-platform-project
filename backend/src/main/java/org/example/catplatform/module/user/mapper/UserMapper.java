package org.example.catplatform.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 可以添加自定义查询方法，BaseMapper 已提供基础的 CRUD
}