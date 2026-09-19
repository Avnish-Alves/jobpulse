package com.avnish.jobpulse.repository;

import com.avnish.jobpulse.model.JobApplication;
import com.avnish.jobpulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByOwner(User owner);

    List<JobApplication> findByOwnerAndId(User owner, Long id);

    // Used by the scheduled sweep: anything with a follow-up due today or earlier that hasn't
    // been flagged yet. Kept as a derived query rather than pulling every row and filtering in
    // Java - the whole point of the job is that this runs cheaply on a schedule.
    List<JobApplication> findByFollowUpDueLessThanEqualAndFollowUpFlaggedFalse(LocalDate cutoff);

    List<JobApplication> findByOwnerAndFollowUpFlaggedTrue(User owner);
}
