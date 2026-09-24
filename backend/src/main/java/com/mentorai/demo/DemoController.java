package com.mentorai.demo;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.mentorai.demo.DemoModels.*;
@RestController @RequestMapping("/api/demo")
public class DemoController {
    private final DemoService service;
    public DemoController(DemoService service){this.service=service;}
    @GetMapping public Status get(Authentication auth){return service.status(auth);}
    @PostMapping("/start") public Status start(Authentication auth,@Valid @RequestBody StartRequest request){return service.start(auth);}
    @PostMapping("/exam") public Status exam(Authentication auth,@Valid @RequestBody ExamRequest request){return service.exam(auth,request);}
}
