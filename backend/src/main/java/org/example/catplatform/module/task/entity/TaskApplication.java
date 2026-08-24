package org.example.catplatform.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_application")
public class TaskApplication {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long userId;
    private LocalDateTime applyTime;
    private Integer status;       // 0-待审核，1-已选中，2-未选中，3-已取消

    @TableField(exist = false)
    private String userName;      // 非数据库字段，用于显示报名人
}