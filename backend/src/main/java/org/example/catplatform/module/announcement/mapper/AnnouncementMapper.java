package org.example.catplatform.module.announcement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.announcement.entity.Announcement;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}