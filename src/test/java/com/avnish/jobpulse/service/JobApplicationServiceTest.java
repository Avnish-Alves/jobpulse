package com.avnish.jobpulse.service;

import com.avnish.jobpulse.dto.JobApplicationDtos.StatsResponse;
import com.avnish.jobpulse.model.ApplicationStatus;
import com.avnish.jobpulse.model.JobApplication;
import com.avnish.jobpulse.model.User;
import com.avnish.jobpulse.repository.JobApplicationRepository;
import com.avnish.jobpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Covers the two operations that touch data ownership directly: deleting an application
 * (must scope to the caller, not just the id) and summarizing stats (must count correctly
 * per status without missing empty buckets).
 */
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService service;

    private User owner;

    private JobApplication appWithStatus(ApplicationStatus status, boolean flagged) {
        JobApplication app = new JobApplication();
        app.setOwner(owner);
        app.setCompany("Acme");
        app.setRole("SDE");
        app.setDateApplied(LocalDate.now().minusDays(5));
        app.setStatus(status);
        app.setFollowUpFlagged(flagged);
        return app;
    }

    private void stubOwner() {
        owner = new User("owner@user.com", "hash");
        when(userRepository.findByEmail("owner@user.com")).thenReturn(Optional.of(owner));
    }

    @Test
    void delete_removesApplication_whenOwnedByCaller() {
        stubOwner();
        JobApplication app = appWithStatus(ApplicationStatus.APPLIED, false);
        when(applicationRepository.findByOwnerAndId(owner, 1L)).thenReturn(List.of(app));

        service.delete("owner@user.com", 1L);

        verify(applicationRepository).delete(app);
    }

    @Test
    void delete_throws_whenApplicationNotOwnedByCaller() {
        stubOwner();
        when(applicationRepository.findByOwnerAndId(owner, 99L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.delete("owner@user.com", 99L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void stats_countsTotalsFlaggedAndByStatus() {
        stubOwner();
        List<JobApplication> apps = List.of(
                appWithStatus(ApplicationStatus.APPLIED, false),
                appWithStatus(ApplicationStatus.APPLIED, true),
                appWithStatus(ApplicationStatus.INTERVIEW, false)
        );
        when(applicationRepository.findByOwner(owner)).thenReturn(apps);

        StatsResponse stats = service.stats("owner@user.com");

        assertThat(stats.total()).isEqualTo(3);
        assertThat(stats.flaggedForFollowUp()).isEqualTo(1);
        assertThat(stats.byStatus())
                .containsEntry(ApplicationStatus.APPLIED, 2L)
                .containsEntry(ApplicationStatus.INTERVIEW, 1L)
                .doesNotContainKey(ApplicationStatus.OFFER);
    }

    @Test
    void stats_returnsZeroTotals_whenUserHasNoApplications() {
        stubOwner();
        when(applicationRepository.findByOwner(owner)).thenReturn(List.of());

        StatsResponse stats = service.stats("owner@user.com");

        assertThat(stats.total()).isZero();
        assertThat(stats.flaggedForFollowUp()).isZero();
        assertThat(stats.byStatus()).isEmpty();
    }
}
