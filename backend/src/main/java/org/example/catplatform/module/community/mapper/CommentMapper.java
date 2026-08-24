package org.example.catplatform.module.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.user.dto.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}