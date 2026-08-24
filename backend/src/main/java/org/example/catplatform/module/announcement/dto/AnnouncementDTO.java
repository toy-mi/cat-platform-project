package org.example.catplatform.module.announcement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {
    private String title;
    private String content;
    private Integer priority;     // 0-普通，1-重要
    private Integer status;       // 0-草稿，1-已发布
    private LocalDateTime publishTime;
}