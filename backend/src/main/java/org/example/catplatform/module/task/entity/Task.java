package org.example.catplatform.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String taskType;
    private String description;
    private Long catId;          // 关联猫咪ID，null 表示全局任务
    private Integer status;       // 0-待分配，1-已分配，2-进行中，3-已完成，4-已取消
    private Long assignedTo;      // 指派的志愿者ID
    private Integer assignedType; // 0-手动，1-自动
    private Integer priority;     // 0-普通，1-紧急
    private LocalDateTime deadline;
    private String completionDescription;   // 完成描述
    private String completionImages;        // 完成图片（逗号分隔URL）
    private Integer completionStatus;       // 0未提交 1待审核 2通过 3拒绝
    private LocalDateTime completionTime;   // 提交时间
    private String completionRemark;        // 审核意见

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createBy;
    private LocalDateTime completeTime;

    @TableField(exist = false)   // 非数据库字段，用于前端显示猫咪名称
    private String catName;
}