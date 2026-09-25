package com.avnish.jobpulse.controller;

import com.avnish.jobpulse.dto.JobApplicationDtos.CreateRequest;
import com.avnish.jobpulse.dto.JobApplicationDtos.Response;
import com.avnish.jobpulse.dto.JobApplicationDtos.StatsResponse;
import com.avnish.jobpulse.dto.JobApplicationDtos.UpdateStatusRequest;
import com.avnish.jobpulse.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Response> create(@AuthenticationPrincipal UserDetails user,
                                            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.ok(applicationService.create(user.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<Response>> list(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(applicationService.listForUser(user.getUsername()));
    }

    @GetMapping("/follow-ups")
    public ResponseEntity<List<Response>> flaggedFollowUps(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(applicationService.listFlaggedFollowUps(user.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(applicationService.stats(user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        applicationService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response> updateStatus(@AuthenticationPrincipal UserDetails user,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(user.getUsername(), id, request.status()));
    }
}
