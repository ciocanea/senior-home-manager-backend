package com.seniorhomemanager.backend.controllers;

import com.seniorhomemanager.backend.models.HelloWorldModel;
import com.seniorhomemanager.backend.services.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello-world")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    @Autowired
    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("/hello-world")
    public String helloWorld() {
        return helloWorldService.helloWorld();
    }

    @GetMapping("/hello-world-repository")
    public String helloWorldRepository() {
        return helloWorldService.helloWorldRepository();
    }

    @PostMapping("/create-hello-world")
    public String createHelloWorld(@RequestBody HelloWorldModel helloWorldModel) {
        return helloWorldService.createHelloWorld(helloWorldModel);
    }
}
