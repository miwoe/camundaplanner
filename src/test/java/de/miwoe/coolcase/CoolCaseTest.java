package de.miwoe.coolcase;


import de.miwoe.planner.PlannerApplication;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PlannerApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class CoolCaseTest {


    @Autowired
    TaskService taskService;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    CaseService caseService;

    @Test
    public void testRequired() throws InterruptedException {
        final CaseInstance coolCase = caseService.createCaseInstanceByKey("CoolCase");


        // Noch darf der Case nicht abgeschlossen werden, da der Required Task noch nicht abgeschlossen wurde.
        assertThatThrownBy(() -> caseService.closeCaseInstance(coolCase.getId())).isInstanceOf(NotAllowedException.class);


        CaseExecution caseExecution = processEngine.getCaseService().createCaseExecutionQuery().enabled().singleResult();
        caseService.manuallyStartCaseExecution(caseExecution.getId());
        // Ist der Task UserTask_Cool aktiv?
        Task task = taskService.createTaskQuery().taskDefinitionKey("UserTask_Cool").singleResult();
        // Complete den Task UserTask_Cool
        taskService.complete(task.getId());

        caseService.closeCaseInstance(coolCase.getId());

    }
}
