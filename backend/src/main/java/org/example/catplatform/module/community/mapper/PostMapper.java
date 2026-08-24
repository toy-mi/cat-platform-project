package org.example.catplatform.module.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.community.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}