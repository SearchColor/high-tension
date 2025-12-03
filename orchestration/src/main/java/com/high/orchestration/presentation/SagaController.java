package com.high.orchestration.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orchestration")
public class SagaController {

    @GetMapping("/test")
    public String test() {
        return "connect test";
    }

}
