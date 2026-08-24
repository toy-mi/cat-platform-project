package org.example.catplatform.module.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.user.dto.Comment;
import org.example.catplatform.module.community.mapper.CommentMapper;
import org.example.catplatform.module.community.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}