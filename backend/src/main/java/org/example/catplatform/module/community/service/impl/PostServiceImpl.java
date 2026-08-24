package org.example.catplatform.module.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.community.entity.Post;
import org.example.catplatform.module.community.mapper.PostMapper;
import org.example.catplatform.module.community.service.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}