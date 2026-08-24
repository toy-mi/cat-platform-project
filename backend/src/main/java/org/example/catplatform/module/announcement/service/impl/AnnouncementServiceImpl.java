package org.example.catplatform.module.announcement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.announcement.entity.Announcement;
import org.example.catplatform.module.announcement.mapper.AnnouncementMapper;
import org.example.catplatform.module.announcement.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}