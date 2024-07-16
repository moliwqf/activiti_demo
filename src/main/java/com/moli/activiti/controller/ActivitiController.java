package com.moli.activiti.controller;

import com.moli.activiti.common.ReturnData;
import com.moli.activiti.common.TaskDTO;
import com.moli.activiti.service.ActService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author moli
 * @time 2024-07-15 17:13:51
 * @description 实际使用的 activiti 控制层
 */
@RestController
@RequestMapping("/act")
public class ActivitiController {

    @Resource
    private ActService actService;

    @PostMapping("deploy")
    public ReturnData<?> deploy(@RequestPart("file") MultipartFile file,
                                @RequestParam("actName") String actName) {
        boolean success = actService.deploy(file, actName);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }


    // 查询流程部署信息
    @GetMapping("queryDeploymentInfo")
    public ReturnData<List<Deployment>> queryDeploymentInfo() {
        return ReturnData.ok(actService.getAllDeployInfo());
    }

    // 查询流程定义信息
    @GetMapping("queryProcessInfo")
    public ReturnData<List<ProcessDefinition>> queryProcessInfo() {
        return ReturnData.ok(actService.getAllProcessDefinitions());
    }

    // 根据部署id删除流程部署
    @DeleteMapping("deleteDeploymentById")
    public ReturnData<?> deleteDeploymentById(String deploymentId) {
        boolean success = actService.deleteDeployById(deploymentId);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 发起流程
    @PostMapping("startProcess")
    public ReturnData<String> startProcess(String processDefinitionId,
                                           @RequestParam Map<String, Object> vars) {
        boolean success = actService.startProcess(processDefinitionId, vars);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 完成任务
    @PostMapping("completeTask")
    public ReturnData<String> completeTask(String taskId,
                                           @RequestParam Map<String, Object> vars) {
        boolean success = actService.completeTask(taskId, vars);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 完成任务
    @PostMapping("completeDelegateTask")
    public ReturnData<String> completeDelegateTask(String taskId,
                                                   @RequestParam Map<String, Object> vars) {
        boolean success = actService.completeDelegateTask(taskId, vars);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }


    // 查询历史流程实例
    @GetMapping("queryHistoryProcessInstance")
    public ReturnData<List<HistoricProcessInstance>> queryHistoryProcessInstance() {
        return ReturnData.ok(actService.getAllHistoryProcessIns());
    }

    // 查询历史任务
    @GetMapping("queryHistoryTask")
    public ReturnData<List<HistoricTaskInstance>> queryHistoryTask() {
        return ReturnData.ok(actService.getAllHistoryTask());
    }

    // 查看历史活动流程实例
    @GetMapping("queryActivityInstance")
    public ReturnData<List<HistoricActivityInstance>> queryActivityInstance() {
        return ReturnData.ok(actService.getAllActivityIns());
    }

    // 根据代办人查询任务
    @GetMapping("queryByAssigneeTask")
    public ReturnData<List<Task>> queryByAssigneeTask(String assignee) {
        return ReturnData.ok(actService.getAllTaskByAssigneeName(assignee));
    }

    // 按任务id更新代办人
    @PutMapping("updateAssigneeByTaskId")
    public ReturnData<String> updateAssigneeByTaskId(String taskId, String assignee) {
        boolean success = actService.setAssignee(taskId, assignee);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 添加审批人意见
    @PostMapping("addComment")
    public ReturnData<String> addComment(String taskId,
                                         String type,
                                         String message) {
        boolean success = actService.addComment(taskId, type, message);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 查询个人审批意见
    @GetMapping("queryComment")
    public ReturnData<List<Comment>> queryComment(String taskId) {
        return ReturnData.ok(actService.getAllComment(taskId, null));
    }


    // 根据候选人查询任务
    @GetMapping("queryTaskByCandidateUser")
    public ReturnData<?> queryTaskByCandidateUser(String candidate) {
        return ReturnData.ok(actService.getTaskByCandidate(candidate));
    }

    // 候选人拾取任务，拾取后的任务，候选人才可以完成
    @PostMapping("claimTask")
    public ReturnData<String> claimTask(@RequestParam("taskId") String taskId,
                                        @RequestParam("candidate") String candidate) {
        //拾取任务
        boolean success = actService.claimTask(taskId, candidate);
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 任务委派
    @PostMapping("delegateTask")
    public ReturnData<?> delegateTask(@RequestBody TaskDTO task) {
        boolean success = actService.delegateTask(task.getTaskId(), task.getAssignee());
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }

    // 任务转办
    @PostMapping("setAssignee")
    public ReturnData<?> setAssignee(@RequestBody TaskDTO task) {
        boolean success = actService.setAssignee(task.getTaskId(), task.getAssignee());
        if (success) return ReturnData.ok();
        return ReturnData.fail();
    }
}
