package com.moli.activiti.service;

import com.moli.activiti.common.ReturnData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author moli
 * @time 2024-07-15 17:14:45
 * @description activiti 服务
 */
public interface ActService {

    boolean deploy(MultipartFile file, String actName);

    List<Deployment> getAllDeployInfo();

    List<Deployment> pageDeployInfo(Long pageNum, Long pageSize);

    List<ProcessDefinition> getAllProcessDefinitions();

    boolean deleteDeployById(String deploymentId);

    boolean startProcess(String processDefId, Map<String, Object> vars);

    boolean completeTask(String taskId, Map<String, Object> vars);

    boolean completeDelegateTask(String processInsId, Map<String, Object> vars);

    List<HistoricProcessInstance> getAllHistoryProcessIns();

    List<HistoricTaskInstance> getAllHistoryTask();

    List<HistoricActivityInstance> getAllActivityIns();

    List<Task> getAllTaskByAssigneeName(String assignee);

    boolean addComment(String taskId, String type, String message);

    List<Comment> getAllComment(String taskId, String type);

    List<Task> getTaskByCandidate(String candidate);

    boolean claimTask(String taskId, String candidate);

    boolean delegateTask(String taskId, String assignee);

    boolean setAssignee(String taskId, String assignee);
}
