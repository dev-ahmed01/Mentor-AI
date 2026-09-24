package com.mentorai.mentor;

import com.mentorai.mentor.MentorModels.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/mentor")
public class MentorController {
    private final MentorService service;
    public MentorController(MentorService service){this.service=service;}
    @GetMapping("/status") public Map<String,Object> status(){return service.status();}
    @GetMapping("/conversations") public List<Conversation> list(Authentication auth){return service.list(auth);}
    @PostMapping("/conversations") @ResponseStatus(HttpStatus.CREATED) public Conversation create(@Valid @RequestBody CreateRequest request,Authentication auth){return service.create(request,auth);}
    @GetMapping("/conversations/{id}") public History history(@PathVariable UUID id,@RequestParam(defaultValue="0") int page,Authentication auth){return service.history(id,page,auth);}
    @PostMapping("/conversations/{id}/messages") @ResponseStatus(HttpStatus.CREATED) public Turn message(@PathVariable UUID id,@Valid @RequestBody MessageRequest request,Authentication auth){return service.message(id,request,auth);}
}
