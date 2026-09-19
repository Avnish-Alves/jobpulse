package com.avnish.jobpulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "job_application")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String role;

    private String channel;

    @Column(nullable = false)
    private LocalDate dateApplied;

    private String contactName;
    private String contactEmail;

    private LocalDate followUpDue;

    /** Set by FollowUpReminderJob, not by the user - this is what makes the sweep worth having. */
    private boolean followUpFlagged = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;
}
