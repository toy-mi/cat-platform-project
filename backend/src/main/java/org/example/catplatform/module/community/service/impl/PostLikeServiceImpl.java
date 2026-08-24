package org.example.catplatform.module.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.community.entity.PostLike;
import org.example.catplatform.module.community.mapper.PostLikeMapper;
import org.example.catplatform.module.community.service.PostLikeService;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {
}