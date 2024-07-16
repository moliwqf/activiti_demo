package com.moli.activiti.service.impl;

import com.moli.activiti.common.ReturnData;
import com.moli.activiti.service.ActService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipInputStream;

/**
 * @author moli
 * @time 2024-07-15 17:14:59
 */
@Slf4j
@Service
public class ActServiceImpl implements ActService {

    @Resource
    private RepositoryService repositoryService; // 提供对流程定义和部署存储库的访问服务

    @Resource
    private RuntimeService runtimeService; // 运行时的接口

    @Resource
    private TaskService taskService; // 任务处理接口

    @Resource
    private HistoryService historyService; // 历史处理接口

    /**
     * 部署流程
     *
     * @param file    流程图压缩包 .zip
     * @param actName 流程名 act_re_deployment - NAME
     */
    @Override
    public boolean deploy(MultipartFile file, String actName) {
        try {
            if (file.isEmpty()) {
                throw new FileNotFoundException("部署压缩包不能为空");
            }
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
            // 压缩流
            ZipInputStream zip = new ZipInputStream(file.getInputStream());
            deploymentBuilder.addZipInputStream(zip);
            // 设置部署流程名称
            deploymentBuilder.name(actName);
            // 部署
            Deployment deploy = deploymentBuilder.deploy();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 查询所有流程部署信息
     */
    @Override
    public List<Deployment> getAllDeployInfo() {
        return repositoryService.createDeploymentQuery().list();
    }

    /**
     * 分页查询流程部署信息
     *
     * @param pageNum  当前页
     * @param pageSize 页大小
     */
    @Override
    public List<Deployment> pageDeployInfo(Long pageNum, Long pageSize) {
        int first = (pageNum.intValue() - 1) * pageSize.intValue();
        return repositoryService.createDeploymentQuery().listPage(first, pageSize.intValue());
    }

    /**
     * 获取所有流程定义信息
     */
    @Override
    public List<ProcessDefinition> getAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery().list();
    }

    /**
     * 根据部署 id 删除流程部署
     *
     * @param deploymentId 部署 Id
     */
    @Override
    public boolean deleteDeployById(String deploymentId) {
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId).list();
        if (list.size() != 1) {
            return false;
        }
        repositoryService.deleteDeployment(deploymentId);
        return true;
    }

    /**
     * 开启流程
     *
     * @param processDefId 流程定义 id
     * @param vars         变量
     */
    @Override
    public boolean startProcess(String processDefId,
            /*流程节点中变量，替换占位符*/ Map<String, Object> vars) {
        // 查询是否存在流程定义信息
        ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefId).singleResult();
        if (Objects.isNull(processDef)) return false;
        // 设置发起人
        Authentication.setAuthenticatedUserId(vars.get("username").toString());
        log.info("发起流程, 发起人: {} - processName: {}, processId: {}", processDef.getName(), processDefId);

        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefId, vars);
        log.info("流程启动成功, 流程名: {}, 启动时间: {}",
                processInstance.getName(),
                processInstance.getStartTime());
        Authentication.setAuthenticatedUserId(null);
        return true;
    }

    /**
     * 完成普通任务
     *
     * @param taskId 任务 id
     * @param vars   变量
     */
    @Override
    public boolean completeTask(String taskId,
            /*流程节点中变量，替换占位符*/ Map<String, Object> vars) {
        //根据流程实例id，查询任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) return false;
        // TODO 判断任务的代办人是否与当前用户一致

        log.info("Task: {} is completing", task.getName());

        // 完成任务
        taskService.complete(task.getId(), vars);
        log.info("Task: {} is completed", task.getName());
        return true;
    }

    /**
     * 完成委派任务
     *
     * @param taskId 任务 id
     * @param vars   变量
     */
    @Override
    public boolean completeDelegateTask(String taskId, Map<String, Object> vars) {
        //根据流程实例id，查询任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) return false;
        // TODO 判断任务的代办人是否与当前用户一致

        log.info("Delegate Task: {} is completing", task.getName());

        // 解析任务
        taskService.resolveTask(task.getId(), vars);
        //根据任务id，完成任务
        taskService.complete(task.getId(), vars);
        log.info("Delegate Task: {} is completed", task.getName());
        return true;
    }

    /**
     * 获取所有的历史流程实例信息
     */
    @Override
    public List<HistoricProcessInstance> getAllHistoryProcessIns() {
        // 也可以设置查询条件，自行查询 API
        return historyService.createHistoricProcessInstanceQuery().list();
    }

    /**
     * 获取所有的历史任务
     */
    @Override
    public List<HistoricTaskInstance> getAllHistoryTask() {
        return historyService.createHistoricTaskInstanceQuery().list();
    }

    /**
     * 获取所有的活动实例信息
     */
    @Override
    public List<HistoricActivityInstance> getAllActivityIns() {
        return historyService.createHistoricActivityInstanceQuery().list();
    }

    /**
     * 根据代办人获取其所有的任务信息
     *
     * @param assignee 代办人
     */
    @Override
    public List<Task> getAllTaskByAssigneeName(String assignee) {
        return taskService.createTaskQuery()
                // 代办人姓名
                .taskAssignee(assignee)
                // 活动状态
                .active()
                .list();
    }

    /**
     * 为任务添加评论
     *
     * @param taskId  任务 id
     * @param type    自定义任务类型
     * @param message 评论信息
     */
    @Override
    public boolean addComment(String taskId, String type, String message) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            return false;
        }
        // TODO 判断评论的人是否正确
        // 添加评论
        taskService.addComment(taskId, task.getProcessInstanceId(), type, message);
        return true;
    }

    /**
     * 获取所有的任务评论信息
     *
     * @param taskId 任务 id
     * @param type   任务类型
     */
    @Override
    public List<Comment> getAllComment(String taskId, String type) {
        return taskService.getTaskComments(taskId, type);
    }

    /**
     * 根据候选人获取任务信息
     *
     * @param candidate 候选人
     */
    @Override
    public List<Task> getTaskByCandidate(String candidate) {
        return taskService.createTaskQuery()
                //候选人名称
                .taskCandidateUser(candidate)
                .list();
    }

    /**
     * 候选人拾取任务
     *
     * @param taskId    任务 id
     * @param candidate 候选人
     */
    @Override
    public boolean claimTask(String taskId, String candidate) {
        Task task = taskService.createTaskQuery()
                // 任务id
                .taskId(taskId)
                // 候选人名称
                .taskCandidateUser(candidate)
                .taskUnassigned()
                .singleResult();
        if (task == null) {
            return false;
        }
        // 拾取任务
        taskService.claim(taskId, candidate);
        return true;
    }

    /**
     * 任务委派
     *
     * @param taskId   任务 id
     * @param assignee 代办人
     */
    @Override
    public boolean delegateTask(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return false;
        }
        taskService.delegateTask(taskId, assignee);
        return true;
    }

    /**
     * 任务转办
     *
     * @param taskId   任务 id
     * @param assignee 代办人
     */
    @Override
    public boolean setAssignee(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return false;
        }
        taskService.setAssignee(taskId, assignee);
        return true;
    }
}
