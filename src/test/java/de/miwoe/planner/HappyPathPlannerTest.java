package de.miwoe.planner;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Grauschleier on 15.04.2017.
 */
@SpringBootTest(classes = PlannerApplication.class)
@RunWith(SpringRunner.class)
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

        final String myBusinessKey = "myBusinessKey";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("starter", myBusinessKey);
//        List<Execution> executionList = runtimeService.createExecutionQuery().list();
//        for (Execution execution : executionList)  {
//
//        }
        System.out.println(processInstance.getId());

        // Hole die laufende CaseInstanz (gibt ja definitv nur eine)
        CaseInstance caseInstance = processEngine.getCaseService().createCaseInstanceQuery().caseDefinitionKey("case_planner").singleResult();

//        List<CaseExecution> caseExecutionList = processEngine.getCaseService().createCaseExecutionQuery().caseDefinitionKey("case_planner").list();
//        Thread.sleep(500);

        // Andere Variante die CaseIntance via Active-Flag zu holen (momentan natürlich auch nur eine)
        CaseInstance caseInstanceByActive = processEngine.getCaseService().createCaseInstanceQuery().active().caseInstanceBusinessKey(myBusinessKey)
                .singleResult();

        // DoItOptional ist nicht aktiv, aber enabled
        CaseExecution caseExecution = processEngine.getCaseService().createCaseExecutionQuery().caseInstanceBusinessKey(myBusinessKey).enabled().singleResult();

        // Starte DoItOptional
        processEngine.getCaseService().manuallyStartCaseExecution(caseExecution.getId());

        // Variante um alle Tasks zu holen
        List<Task> tasks = taskService.createTaskQuery().caseInstanceBusinessKey(myBusinessKey).list();

        // Ist der Task PlanItem_DoIt aktiv?
        Task task = taskService.createTaskQuery().taskDefinitionKey("PlanItem_DoIt").singleResult();
        // Complete den Task PlanItem_DoIt
        taskService.complete(task.getId());
//
        // Setze die Variable finished
        caseService.setVariable(caseInstance.getId(), "finished", true);

        // Ich kann den Case nicht abschließen, weil DoItOptional leider schon aktiv ist.
        assertThatThrownBy(() -> caseService.closeCaseInstance(caseInstance.getId())).isInstanceOf(NotAllowedException.class);

        Task taskOptional = taskService.createTaskQuery().taskDefinitionKey("PlanItem_DoItOptional").singleResult();
        // Complete den Task PlanItem_DoIt
        taskService.complete(taskOptional.getId());

        // Jetzt kann auch der Case abgeschlossen werden.
        caseService.closeCaseInstance(caseInstance.getId());

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    }
}
