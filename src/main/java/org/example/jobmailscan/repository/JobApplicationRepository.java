package org.example.jobmailscan.repository;

import org.example.jobmailscan.entity.JobApplication;
import org.example.jobmailscan.entity.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

    Optional<JobApplication> findByGmailMessageId(String gmailMessageId);

    boolean existsByGmailMessageId(String gmailMessageId);

    List<JobApplication> findByOfferStatusOrderByAppliedDateDesc(OfferStatus offerStatus);

    List<JobApplication> findAllByOrderByAppliedDateDesc();

    List<JobApplication> findByCompanyNameContainingIgnoreCaseOrJobTitleContainingIgnoreCase(
        String company, String title
    );
}
