package org.example.catplatform.module.cat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.cat.entity.Cat;

@Mapper
public interface CatMapper extends BaseMapper<Cat> {
}