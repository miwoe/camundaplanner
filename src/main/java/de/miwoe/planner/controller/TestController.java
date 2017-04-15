package de.miwoe.planner.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Grauschleier on 15.04.2017.
 */
@RestController
public class TestController {

    @RequestMapping(path = "/static/out", method = RequestMethod.GET)
    public String out() {
        return "Hello";
    }
}
