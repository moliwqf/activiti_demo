package com.moli.activiti.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author moli
 * @time 2024-07-16 09:49:01
 * @description 人事监听器
 */
@Slf4j
public class HrListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        log.error("\r\n *****************MangerExecutionListener流程监听器*****************" +
                        "\r\n execution.getCurrentFlowElement().getId()：【{}】," +
                        "\r\n execution.getCurrentFlowElement().getName():【{}】，" +
                        "\r\n execution.getEventName()：【{}】，" +
                        "\r\n execution.getProcessDefinitionId()：【{}】，" +
                        "\r\n execution.getProcessInstanceId()：【{}】，" +
                        "\r\n execution：【{}】",
                execution.getCurrentFlowElement().getId(),
                execution.getCurrentFlowElement().getName(),
                execution.getEventName(),
                execution.getProcessDefinitionId(),
                execution.getProcessInstanceId(),
                execution
        );
    }
}
