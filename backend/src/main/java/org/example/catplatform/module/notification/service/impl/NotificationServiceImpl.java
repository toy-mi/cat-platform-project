package org.example.catplatform.module.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.mapper.NotificationMapper;
import org.example.catplatform.module.notification.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
}