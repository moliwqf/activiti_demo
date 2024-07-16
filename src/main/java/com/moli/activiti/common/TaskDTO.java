package com.moli.activiti.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author moli
 * @time 2024-07-16 11:46:34
 * @description 任务 dto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {

    private String taskId;

    private String assignee;
}
