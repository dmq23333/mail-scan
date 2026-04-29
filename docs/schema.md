# Database Schema

## Table: `job_applications`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Primary key |
| `gmail_message_id` | VARCHAR(255) | UNIQUE, NOT NULL | Gmail message ID used for de-duplication on re-sync |
| `gmail_thread_id` | VARCHAR(255) | | Gmail thread ID |
| `company_name` | VARCHAR(255) | | Company name extracted from email |
| `job_title` | VARCHAR(255) | | Job title extracted from email |
| `platform` | VARCHAR(255) | | Sourcing platform (e.g. LinkedIn, Indeed, Greenhouse) |
| `applied_date` | DATE | | Date the email was received / application was submitted |
| `offer_status` | VARCHAR(255) | NOT NULL | Application lifecycle status (see enum below) |
| `mark` | INTEGER | | User rating 1–5, null until manually set |
| `jd` | TEXT | | Full job description, populated manually |
| `resume_version` | VARCHAR(250) | | Resume version label used for this application |
| `email_subject` | VARCHAR(255) | | Raw email subject line, for reference |
| `email_from` | VARCHAR(255) | | Raw email sender address, for reference |
| `snippet` | TEXT | | Gmail API snippet of the email body |
| `created_at` | DATETIME | NOT NULL | Timestamp set on insert, never updated |
| `updated_at` | DATETIME | NOT NULL | Timestamp updated on every save |

## Enum: `OfferStatus`

| Value | Meaning |
|-------|---------|
| `APPLIED` | Application submitted |
| `APPLICATION_VIEWED` | Employer has viewed the application |
| `WAITING_RESPONSE` | Awaiting employer response |
| `INTERVIEW_INVITATION` | Interview scheduled or invited |
| `REJECTED` | Application rejected |
| `ALL` | Query-only sentinel — never stored in the database |

**Classifier priority** (highest wins when multiple keywords match):  
`INTERVIEW_INVITATION` > `REJECTED` > `APPLICATION_VIEWED` > `APPLIED` > `WAITING_RESPONSE`

## Notes

- **De-duplication:** `gmail_message_id` has a UNIQUE constraint; re-syncing the same email is a no-op.
- **DB:** H2 file database at `./data/jobscan;AUTO_SERVER=TRUE`. JPA `ddl-auto=update` — columns are added automatically on startup.
- **H2 Console:** available at `/h2-console` in dev mode.
