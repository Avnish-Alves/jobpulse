package com.avnish.jobpulse.scheduler;

import com.avnish.jobpulse.model.ApplicationStatus;
import com.avnish.jobpulse.model.JobApplication;
import com.avnish.jobpulse.model.User;
import com.avnish.jobpulse.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the scheduled sweep in isolation from the clock and the database - this is the
 * "integration path" for the scheduling design decision: given some overdue, unflagged
 * applications, the job must flag exactly those and save them, and touch nothing else.
 */
@ExtendWith(MockitoExtension.class)
class FollowUpReminderJobTest {

    @Mock private JobApplicationRepository applicationRepository;

    @InjectMocks
    private FollowUpReminderJob followUpReminderJob;

    private JobApplication appWithFollowUp(LocalDate followUpDue) {
        JobApplication app = new JobApplication();
        app.setOwner(new User("owner@user.com", "hash"));
        app.setCompany("Acme");
        app.setRole("SDE");
        app.setDateApplied(LocalDate.now().minusDays(10));
        app.setFollowUpDue(followUpDue);
        app.setStatus(ApplicationStatus.APPLIED);
        return app;
    }

    @Test
    void flagsAllOverdueUnflaggedApplications() {
        JobApplication overdueOne = appWithFollowUp(LocalDate.now().minusDays(2));
        JobApplication overdueTwo = appWithFollowUp(LocalDate.now());

        when(applicationRepository.findByFollowUpDueLessThanEqualAndFollowUpFlaggedFalse(any()))
                .thenReturn(List.of(overdueOne, overdueTwo));

        followUpReminderJob.flagOverdueFollowUps();

        assertThat(overdueOne.isFollowUpFlagged()).isTrue();
        assertThat(overdueTwo.isFollowUpFlagged()).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<JobApplication>> captor = ArgumentCaptor.forClass(List.class);
        verify(applicationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(overdueOne, overdueTwo);
    }

    @Test
    void doesNothing_whenNoApplicationsAreOverdue() {
        when(applicationRepository.findByFollowUpDueLessThanEqualAndFollowUpFlaggedFalse(any()))
                .thenReturn(List.of());

        followUpReminderJob.flagOverdueFollowUps();

        verify(applicationRepository, never()).saveAll(any());
    }
}
