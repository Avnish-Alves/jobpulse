package com.avnish.jobpulse.service;

import com.avnish.jobpulse.dto.JobApplicationDtos.CreateRequest;
import com.avnish.jobpulse.dto.JobApplicationDtos.Response;
import com.avnish.jobpulse.model.ApplicationStatus;
import com.avnish.jobpulse.model.JobApplication;
import com.avnish.jobpulse.model.User;
import com.avnish.jobpulse.repository.JobApplicationRepository;
import com.avnish.jobpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public Response create(String ownerEmail, CreateRequest request) {
        User owner = currentUser(ownerEmail);

        JobApplication app = new JobApplication();
        app.setOwner(owner);
        app.setCompany(request.company());
        app.setRole(request.role());
        app.setChannel(request.channel());
        app.setDateApplied(request.dateApplied());
        app.setContactName(request.contactName());
        app.setContactEmail(request.contactEmail());
        app.setFollowUpDue(request.followUpDue());

        return toResponse(applicationRepository.save(app));
    }

    public List<Response> listForUser(String ownerEmail) {
        User owner = currentUser(ownerEmail);
        return applicationRepository.findByOwner(owner).stream().map(this::toResponse).toList();
    }

    public Response updateStatus(String ownerEmail, Long id, ApplicationStatus status) {
        User owner = currentUser(ownerEmail);
        JobApplication app = applicationRepository.findByOwnerAndId(owner, id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such application for this user"));
        app.setStatus(status);
        return toResponse(applicationRepository.save(app));
    }

    public List<Response> listFlaggedFollowUps(String ownerEmail) {
        User owner = currentUser(ownerEmail);
        return applicationRepository.findByOwnerAndFollowUpFlaggedTrue(owner).stream()
                .map(this::toResponse).toList();
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    private Response toResponse(JobApplication app) {
        return new Response(
                app.getId(), app.getCompany(), app.getRole(), app.getChannel(),
                app.getDateApplied(), app.getContactName(), app.getContactEmail(),
                app.getFollowUpDue(), app.isFollowUpFlagged(), app.getStatus()
        );
    }
}
