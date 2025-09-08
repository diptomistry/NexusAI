
package com.example.ai_assistant_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/sample")
public class SampleController {

    @GetMapping
    public String helloWorld() {
        return "Hello from Sample API!";
    }
}
