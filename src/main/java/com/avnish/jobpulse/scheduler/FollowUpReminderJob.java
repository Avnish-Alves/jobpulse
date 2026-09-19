package com.avnish.jobpulse.scheduler;

import com.avnish.jobpulse.model.JobApplication;
import com.avnish.jobpulse.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * The one real design decision in this project, along with JWT auth.
 *
 * Why a scheduled sweep instead of computing "is this overdue" on every read: the tracker this
 * mirrors has a "Follow-up due" column that's easy to update but easy to forget to check. A
 * nightly sweep that flags anything overdue means the flag is already sitting on the record by
 * the time anyone looks - no per-request date math, and the "flagged" list is a cheap indexed
 * read instead of a full-table scan with a date comparison on every call.
 *
 * Runs once a day at 02:00 server time. In interviews: this is the difference between "reactive"
 * (check on read) and "proactive" (precomputed on a schedule) - the right call whenever reads are
 * far more frequent than the underlying data changes, which is true here.
 */
@Component
@RequiredArgsConstructor
public class FollowUpReminderJob {

    private static final Logger log = LoggerFactory.getLogger(FollowUpReminderJob.class);

    private final JobApplicationRepository applicationRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void flagOverdueFollowUps() {
        LocalDate today = LocalDate.now();
        List<JobApplication> overdue =
                applicationRepository.findByFollowUpDueLessThanEqualAndFollowUpFlaggedFalse(today);

        if (overdue.isEmpty()) {
            log.info("Follow-up sweep: nothing overdue as of {}", today);
            return;
        }

        overdue.forEach(app -> app.setFollowUpFlagged(true));
        applicationRepository.saveAll(overdue);
        log.info("Follow-up sweep: flagged {} application(s) as of {}", overdue.size(), today);
    }
}
