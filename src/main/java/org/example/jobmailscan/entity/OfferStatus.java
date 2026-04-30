package org.example.jobmailscan.entity;

public enum OfferStatus {
	APPLIED,
	APPLICATION_VIEWED,
	WAITING_RESPONSE,
	INTERVIEW_INVITATION,
	REJECTED,
	ALL          // sentinel for "no filter" queries — do NOT persist
}
