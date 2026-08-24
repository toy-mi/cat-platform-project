package org.example.catplatform.module.task.dto;

import lombok.Data;

@Data
public class TaskCompleteDTO {
    private String description;   // 完成描述
    private String images;        // 图片URL列表，逗号分隔
}