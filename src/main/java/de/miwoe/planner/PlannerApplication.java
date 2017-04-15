package de.miwoe.planner;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by Grauschleier on 15.04.2017.
 */
@SpringBootApplication
@EnableProcessApplication("myProcessApplicationName")
public class PlannerApplication  {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerApplication.class);

    private static int PORT;

    public static void main(String[] args) {
        SpringApplication.run(PlannerApplication.class, args);
        LOGGER.info("You can reach the web app under: http://localhost:{}/", PORT);
    }

    @Component
    public static class ServletContainerListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Override
        public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
            PORT = event.getEmbeddedServletContainer().getPort();
        }

    }

}