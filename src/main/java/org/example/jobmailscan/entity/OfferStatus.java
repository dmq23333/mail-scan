package org.example.jobmailscan.entity;

/**
 * Lifecycle states for a job application, ordered roughly by progression.
 * ALL is a query-only sentinel — never stored on an entity.
 */
public enum OfferStatus {
    APPLIED,
    APPLICATION_VIEWED,
    WAITING_RESPONSE,
    INTERVIEW_INVITATION,
    REJECTED,
    ALL          // sentinel for "no filter" queries — do NOT persist
}
