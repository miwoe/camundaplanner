package de.miwoe.planner;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Grauschleier on 15.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HappyPathPlannerTest {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    CaseService caseService;

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessEngine processEngine;

    @Test
    public void test() throws InterruptedException {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("starter");
//        List<Execution> executionList = runtimeService.createExecutionQuery().list();
//        for (Execution execution : executionList)  {
//
//        }
        System.out.println(processInstance.getId());
        Thread.sleep(500);

        CaseInstance caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseDefinitionKey("case_planner").singleResult();
//        List<CaseExecution> caseExecutionList = processEngine.getCaseService().createCaseExecutionQuery().caseDefinitionKey("case_planner").list();
        Thread.sleep(500);
        Task task = taskService.createTaskQuery().taskDefinitionKey("PlanItem_0lcgevm").singleResult();
        taskService.complete(task.getId());
//        caseService.completeCaseExecution(caseInstance.getId());

        caseService.setVariable(caseInstance.getId(), "finished", true);
        Thread.sleep(500);
        caseInstance = caseService.createCaseInstanceQuery().singleResult();
        assertThat(caseInstance.isTerminated()).isEqualTo(true);
        System.out.println(processEngine.getHistoryService().createHistoricCaseActivityInstanceQuery().list());
        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
    }
}
