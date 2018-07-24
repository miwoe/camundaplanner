package de.miwoe.example;

import de.miwoe.TestConfig;
import de.miwoe.planner.PlannerApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Grauschleier on 15.04.2017.
 */
@SpringBootTest(classes = PlannerApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class HappyPathExampleTest extends TestConfig {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    ProcessEngine processEngine;

    @Test
    public void test() throws InterruptedException {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("example");
        System.out.println(processInstance.getId());
        Thread.sleep(2000);

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
    }
}
