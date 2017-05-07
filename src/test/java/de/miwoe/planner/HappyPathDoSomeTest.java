package de.miwoe.planner;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * Created by Grauschleier on 07.05.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HappyPathDoSomeTest {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    CaseService caseService;

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessEngine processEngine;


    private ProcessInstance processInstance;
    private CaseInstance caseInstance;
    private CaseExecution caseExecution;
    private CaseExecution caseExecution2;

    @Before
    public void reset() {
        processInstance = null;
        caseInstance = null;
        caseExecution = null;
        caseExecution2 = null;
    }

    @After
    public void cleanUp() {
        for (CaseInstance caseInstanceToDelete: processEngine.getCaseService().createCaseInstanceQuery().active().list()) {
            processEngine.getCaseService().terminateCaseExecution(caseInstanceToDelete.getId());
        }
        for (ProcessInstance processInstanceToDelete : processEngine.getRuntimeService().createProcessInstanceQuery().active().list()) {
            processEngine.getRuntimeService().deleteProcessInstance(processInstanceToDelete.getId(), "Test finished");
        }
    }

    private void startOne(String myBusinessKey) {
        processInstance = runtimeService.startProcessInstanceByKey("DoSomeStarter", myBusinessKey);

        assertThat(processInstance).isNotNull();

        caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseDefinitionKey("DoSomeCase").active().singleResult();
        assertThat(caseInstance).isNotNull();

        caseExecution = processEngine.getCaseService().createCaseExecutionQuery().caseInstanceBusinessKey(myBusinessKey).enabled().singleResult();
        assertThat(caseExecution).isNotNull();
    }

    private void startSecondExecution(String myBusinessKey) {
        caseExecution2 = processEngine.getCaseService().createCaseExecutionQuery().caseInstanceBusinessKey(myBusinessKey).enabled().singleResult();
        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution2.getId());
    }

    private void terminateCaseInstance() {
        for (CaseExecution caseExecution: caseService.createCaseInstanceQuery().active().list()) {
            processEngine.getCaseService().terminateCaseExecution(caseExecution.getId());
        }
        caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
        assertThat(caseInstance.isTerminated()).isTrue();
    }

    @Test
    public void I_Can_Directly_Close_The_Case() throws InterruptedException {

        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        caseService.completeCaseExecution(caseInstance.getId());

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
        caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
        assertThat(caseInstance.isActive()).isFalse();

    }



    @Test
    public void I_Can_NOT_Close_The_Case_If_some_running() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);

        terminateCaseInstance();
    }




    @Test
    public void I_Can_Close_The_Case_If_one_started_and_completed() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());

        Task task = taskService.createTaskQuery().active().singleResult();
        assertThat(task).isNotNull();
        taskService.complete(task.getId());
        caseService.completeCaseExecution(caseInstance.getId());
        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

        caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
        assertThat(caseInstance.isActive()).isFalse();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void I_Can_Close_The_Case_If_two_started_and_completed() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());
        startSecondExecution(myBusinessKey);

        List<Task> tasks = taskService.createTaskQuery().active().list();
        assertThat(tasks).isNotNull();
        for (Task task: tasks) {
            taskService.complete(task.getId());
        }

        caseService.completeCaseExecution(caseInstance.getId());
        caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
        assertThat(caseInstance.isActive()).isFalse();

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void I_Can_NOT_Close_The_Case_If_two_started_and_just_one_completed() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());
        startSecondExecution(myBusinessKey);

        List<Task> tasks = taskService.createTaskQuery().active().list();
        assertThat(tasks).isNotNull();
        for (Task task: tasks) {
            taskService.complete(task.getId());
            break;
        }

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_ACTIVE);
        terminateCaseInstance();
    }

    @Test
    public void I_Can_Close_The_Case_If_two_started_and_just_one_completed_and_other_one_aborted() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());
        startSecondExecution(myBusinessKey);

        List<Task> tasks = taskService.createTaskQuery().active().list();
        assertThat(tasks).isNotNull();
        for (Task task: tasks) {
            taskService.complete(task.getId());
            break;
        }

        Task task = taskService.createTaskQuery().active().singleResult();
        ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        runtimeService.correlateMessage("Abort");
        caseService.completeCaseExecution(caseInstance.getId());
        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void I_know_If_I_could_start_manual_execution() throws InterruptedException {
        final String myBusinessKey = UUID.randomUUID().toString();
        startOne(myBusinessKey);

        CaseExecution caseExecution = processEngine.getCaseService().createCaseExecutionQuery().caseInstanceBusinessKey(myBusinessKey).activityId("SomeActivity").enabled().singleResult();

        assertThat(caseExecution).isNotNull();

    }


}
