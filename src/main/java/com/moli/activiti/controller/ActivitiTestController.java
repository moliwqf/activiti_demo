package com.moli.activiti.controller;

import com.moli.activiti.common.ReturnData;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author moli
 * @time 2024-07-15 11:11:21
 */
@Slf4j
@RestController
public class ActivitiTestController {

    // 提供对流程定义和部署存储库的访问服务
    @Autowired
    private RepositoryService repositoryService;

    // 运行时的接口
    @Autowired
    private RuntimeService runtimeService;

    // 任务处理接口
    @Autowired
    private TaskService taskService;

    // 历史处理接口
    @Autowired
    private HistoryService historyService;

    /**
     * 部署流程
     * <p>
     * 1、设计器设计流程xml/png
     * 2、部署流程
     * 3、发起流程
     * 4、执行流程
     *
     * @param file 上传流程压缩包
     */
    @PostMapping("deploy")
    public ReturnData<Object> deploy(@RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new NullPointerException("部署压缩包不能为空");
            }
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
            //压缩流
            ZipInputStream zip = new ZipInputStream(file.getInputStream());
            deploymentBuilder.addZipInputStream(zip);
            //设置部署流程名称
            deploymentBuilder.name("请假审批");
            //部署流程
            Deployment deploy = deploymentBuilder.deploy();
            return ReturnData.ok(deploy);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnData.fail(e.toString());
        }
    }


    // 查询流程部署信息
    @GetMapping("queryDeploymentInfo")
    public ReturnData<String> queryDeploymentInfo() {
        // 也可以设置查询部署筛选条件，自行查询API，基本上都是见名知意的
        List<Deployment> list = repositoryService.createDeploymentQuery().list();
        log.info("流程部署信息：{}", list);
        return ReturnData.ok(list.toString());
    }

    // 查询流程定义信息
    @GetMapping("queryProcessInfo")
    public ReturnData<String> queryProcessInfo() {
        //也可以设置查询流程定义筛选条件，自行查询API，基本上都是见名知意的
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        log.info("流程定义信息：{}", list);
        return ReturnData.ok(list.toString());
    }

    // 根据部署id删除流程部署
    @DeleteMapping("deleteDeploymentById")
    public ReturnData<?> deleteDeploymentById(String deploymentId) {
        List<Deployment> list = repositoryService.createDeploymentQuery().deploymentId(deploymentId).list();
        if (list.size() != 1) {
            return ReturnData.fail("流程定义未找到");
        }
        //根据部署id删除流程部署
        repositoryService.deleteDeployment(deploymentId);
        return ReturnData.ok("删除成功");
    }

    // 发起流程
    @PostMapping("startProcess")
    public ReturnData<String> startProcess(String processDefinitionId, String userName) {
        log.info("发起流程，processDefinitionId：{}", processDefinitionId);
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).list();
        if (list.size() != 1) {
            return ReturnData.fail("流程定义不存在");
        }
        // 流程节点中变量，替换占位符
        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("userName", userName);
        // 通过流程定义ID启动一个流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variablesMap);
        log.info("流程实例：{}", processInstance);
        return ReturnData.ok("发起成功");
    }

    // 完成任务
    @PostMapping("completeTask")
    public ReturnData<String> completeTask(String processInstanceId) {
        //根据流程实例id，查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (taskList.size() != 1) {
            return ReturnData.fail("当前没有任务");
        }
        log.info("任务列表：{}", taskList);
        //根据任务id，完成任务
        Map<String, Object> vars = new HashMap<>();
        vars.put("permitType", 1); // 实现流程驳回功能
        taskService.complete(taskList.get(0).getId(), vars);
        return ReturnData.ok("完成任务");
    }

    // 完成任务
    @PostMapping("completeDelegateTask")
    public ReturnData<String> completeDelegateTask(String processInstanceId) {
        //根据流程实例id，查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (taskList.size() != 1) {
            return ReturnData.fail("当前没有任务");
        }
        log.info("任务列表：{}", taskList);
        taskService.resolveTask(taskList.get(0).getId());
        //根据任务id，完成任务
        taskService.complete(taskList.get(0).getId());
        return ReturnData.ok("完成任务");
    }


    // 查询历史流程实例
    @GetMapping("queryHistoryProcessInstance")
    public ReturnData<String> queryHistoryProcessInstance() {
        // 也可以设置查询条件，自行查询API
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().list();
        log.info("查询历史流程实例 {}", list);
        return ReturnData.ok(list.toString());
    }

    // 查询历史任务
    @GetMapping("queryHistoryTask")
    public ReturnData<String> queryHistoryTask() {
        //也可以设置查询条件，自行查询API
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().list();
        log.info("查询历史任务 {}", list);
        return ReturnData.ok(list.toString());
    }

    // 查看历史活动流程实例
    @GetMapping("queryActivityInstance")
    public ReturnData<String> queryActivityInstance() {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().list();
        log.info("查看历史活动流程实例 {}", list);
        return ReturnData.ok(list.toString());
    }

    // 根据代办人查询任务
    @GetMapping("queryByAssigneeTask")
    public ReturnData<String> queryByAssigneeTask(String assignee) {
        List<Task> taskList = taskService.createTaskQuery()
                //代办人姓名
                .taskAssignee(assignee)
                //活动状态
                .active()
                .list();
        log.info("根据代办人查询任务 {}", taskList);
        return ReturnData.ok(taskList.toString());
    }

    // 按任务id更新代办人
    @PutMapping("updateAssigneeByTaskId")
    public ReturnData<String> updateAssigneeByTaskId(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ReturnData.fail("任务不存在");
        }
        //更新当前任务的代办人
        taskService.setAssignee(taskId, assignee);
        return ReturnData.ok("更新成功");
    }

    // 添加审批人意见
    @PostMapping("addComment")
    public ReturnData<String> addComment(String taskId,
                                         String processInstanceId,
                                         String message) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            return ReturnData.fail("任务不存在");
        }
        taskService.addComment(taskId, processInstanceId, message);
        // taskService.addComment("任务id", "流程实例id", "自定义变量type，可以用作用户id", "意见");
        return ReturnData.ok("添加成功");
    }

    // 查询个人审批意见
    @GetMapping("queryComment")
    public ReturnData<String> queryComment(String taskId) {
        // 注意，这里也可以使用type做搜索，通过添加意见的第三个参数，指定用户id
        // taskService.addComment("任务id", "流程实例id", "自定义变量type，可以用作用户id", "意见");
        List<Comment> taskComments = taskService.getTaskComments(taskId);
        //taskService.getTaskComments(taskId,"自定义变量type，可以用作用户id");
        log.info("查询个人审批意见 {}", taskComments);
        return ReturnData.ok(taskComments.toString());
    }


    // 根据候选人查询任务
    @GetMapping("queryTaskByCandidateUser")
    public ReturnData<?> queryTaskByCandidateUser(String userName) {
        List<Task> taskList = taskService.createTaskQuery()
                //候选人名称
                .taskCandidateUser(userName)
                .list();
        return ReturnData.ok(taskList.toString());
    }

    /**
     * 拾取任务，拾取后的任务，该候选人才可以完成任务
     *
     * @param taskId   任务id
     * @param userName 候选人名称
     */
    // 候选人拾取任务，拾取后的任务，候选人才可以完成
    @PostMapping("claimTask")
    public ReturnData<String> claimTask(String taskId, String userName) {
        Task task = taskService.createTaskQuery()
                //任务id
                .taskId(taskId)
                //候选人名称
                .taskCandidateUser(userName)
                .singleResult();
        if (task == null) {
            return ReturnData.fail("任务不存在");
        }
        //拾取任务
        taskService.claim(taskId, userName);
        return ReturnData.ok("拾取任务成功");
    }

    // 发起流程
    @PostMapping("startProcessBranch")
    public ReturnData<String> startProcessBranch(String processDefinitionId) {
        log.info("发起流程： processDefinitionId：{}", processDefinitionId);
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).list();
        if (list.size() != 1) {
            return ReturnData.fail("流程定义不存在");
        }
        // 流程节点中变量，替换占位符
        Map<String, Object> variablesMap = new HashMap<>();
        // 流程变量day
        variablesMap.put("day", "8");
        // 通过流程定义ID启动一个流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variablesMap);
        log.info("流程实例：{}", processInstance);
        return ReturnData.ok("发起成功 " + processInstance);
    }


    // 任务委派
    @PostMapping("delegateTask")
    public ReturnData<?> delegateTask(
            @RequestParam("taskId") String taskId,
            @RequestParam("userName") String userName
    ) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ReturnData.fail("任务不存在");
        }
        taskService.delegateTask(taskId, userName);
        return ReturnData.ok();
    }

    // 任务转办
    @PostMapping("setAssignee")
    public ReturnData<?> setAssignee(
            String taskId,
            String userName
    ) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ReturnData.fail("任务不存在");
        }
        taskService.setAssignee(taskId, userName);
        return ReturnData.ok();
    }
}
