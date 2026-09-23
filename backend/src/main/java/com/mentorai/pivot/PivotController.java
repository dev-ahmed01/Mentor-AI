package com.mentorai.pivot;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.mentorai.pivot.PivotModels.*;

@RestController @RequestMapping("/api/pivots")
public class PivotController {
    private final PivotService service;
    public PivotController(PivotService service){this.service=service;}
    @GetMapping public List<Summary> list(Authentication auth){return service.list(auth);}
    @GetMapping("/{id}") public Detail get(Authentication auth,@PathVariable UUID id){return service.get(auth,id);}
    @PostMapping public ResponseEntity<Detail> create(Authentication auth,@Valid @RequestBody CreateRequest request){var detail=service.create(auth,request);return ResponseEntity.created(URI.create("/api/pivots/"+detail.id())).body(detail);}
    @PostMapping("/{id}/accept") public Detail accept(Authentication auth,@PathVariable UUID id,@Valid @RequestBody AcceptRequest request){return service.accept(auth,id,request);}
}
