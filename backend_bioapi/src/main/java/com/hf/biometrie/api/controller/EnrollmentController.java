package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enroll")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<?> enrollUser(@RequestBody EnrollmentRequest request) {
        try {
            Utilisateur savedUser = enrollmentService.saveEnrollment(request);
            return ResponseEntity.ok().body("{\"status\":\"success\", \"id\":" + savedUser.getId() + "}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
