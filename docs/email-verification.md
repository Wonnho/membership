# Email verification setup

Signup sends a six-digit email code. A new member cannot log in until the code
is verified. The code expires after 10 minutes, allows five guesses, and works
once. Resends replace the old code, have a 60-second cooldown, and are limited
to five per account per hour. The seeded administrator is verified during
provisioning. Changing an email requires verification again. Password resets
do not bypass email verification.

Configure these environment variables in IntelliJ's MemberApplication run
configuration, using values from your SMTP provider:

| Variable | Value |
| --- | --- |
| MAIL_HOST | Your SMTP server hostname |
| MAIL_PORT | 587 for STARTTLS, or your provider's specified port |
| MAIL_USERNAME | Your SMTP login name |
| MAIL_PASSWORD | Your SMTP/app password; keep it out of source control |
| MAIL_FROM | A sender address permitted by your provider |
| MAIL_SMTP_AUTH | true by default; false only for a local test mail server |
| MAIL_STARTTLS | true by default (port 587) |
| MAIL_SSL | false by default; use true for port 465 and MAIL_STARTTLS=false |

Reload Gradle, rebuild the IntelliJ project, and restart on port 8081.
The current in-memory H2 database resets on restart.

No real email can be sent without SMTP configuration. Missing configuration or
a send failure produces a signup error and rolls back the account creation.
Codes are never displayed in pages or logs. SMTP acceptance does not guarantee
inbox delivery: users can check spam and request another code.

Flow: /membership/signup → /membership/verify → /login?verified → /coffee.

Integration tests mock the mailer and do not send emails. SMTP behavior follows
[Spring Boot mail configuration](https://docs.spring.io/spring-boot/reference/io/email.html).
