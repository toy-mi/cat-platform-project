package org.example.catplatform.module.task.dto;

import lombok.Data;
import java.time.LocalDateTime;


//用于接收前端创建/更新任务的请求
@Data
public class TaskDTO {
    private String title;
    private String taskType;
    private String description;
    private Long catId;
    private Integer priority;     // 0-普通，1-紧急
    private LocalDateTime deadline;
}