package com.avnish.jobpulse.dto;

import com.avnish.jobpulse.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class JobApplicationDtos {

    public record CreateRequest(
            @NotBlank String company,
            @NotBlank String role,
            String channel,
            @NotNull LocalDate dateApplied,
            String contactName,
            String contactEmail,
            LocalDate followUpDue
    ) {}

    public record UpdateStatusRequest(@NotNull ApplicationStatus status) {}

    public record Response(
            Long id,
            String company,
            String role,
            String channel,
            LocalDate dateApplied,
            String contactName,
            String contactEmail,
            LocalDate followUpDue,
            boolean followUpFlagged,
            ApplicationStatus status
    ) {}
}
