package org.example.catplatform.module.cat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.cat.entity.Cat;
import org.example.catplatform.module.cat.mapper.CatMapper;
import org.example.catplatform.module.cat.service.CatService;
import org.springframework.stereotype.Service;

@Service
public class CatServiceImpl extends ServiceImpl<CatMapper, Cat> implements CatService {
    // 自定义方法可以在这里实现
}