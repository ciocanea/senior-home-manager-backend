package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.HelloWorldModel;
import com.seniorhomemanager.backend.repositories.HelloWorldRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HelloWorldService {

    private final HelloWorldRepository helloWorldRepository;

    public HelloWorldService(HelloWorldRepository helloWorldRepository) {
        this.helloWorldRepository = helloWorldRepository;
    }

    public String helloWorld() {
        return "Hello, World!";
    }

    public String helloWorldRepository() {
        HelloWorldModel helloWorldModel = helloWorldRepository.findAll().getFirst();
        return helloWorldModel.getMessage();
    }

    public String createHelloWorld(HelloWorldModel helloWorldModel) {
        return helloWorldRepository.save(helloWorldModel).getMessage();
    }
}