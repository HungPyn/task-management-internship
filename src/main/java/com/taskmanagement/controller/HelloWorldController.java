package com.taskmanagement.controller;

import com.taskmanagement.retresponse.SuccessResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello-world")
public class HelloWorldController {

    @GetMapping
    public SuccessResponse helloWorld() {
        return new SuccessResponse("Hello World");
    }
}
