# QueueLess - Project Notes (paste this file into any new chat)

## What it is
Queue/token system. STUDENT joins a service queue, gets a token, sees position + estimated wait.
STAFF calls next, completes or skips. ADMIN creates services/staff, assigns staff, watches queues.

## Stack and rules
- Backend: Spring Boot 4.1.1 (Maven, Java 21), Spring Data JPA, Spring Security + JWT (jjwt 0.12.6), MySQL, Spring Mail
- Frontend: React (Vite), react-router-dom, plain CSS, polling every 5 s
- MySQL on port 3307, database: queueless_db. Backend port 8080, frontend port 3000
- Simple code, no Lombok. Base package: com.queueless
- Folders: queueless/backend (model, repository, service, controller, dto, security), queueless/frontend (pages, components)

## Tables
users(id, name, email, password, role) | services(id, name, description, avg_service_minutes, staff_id)
queue_entries(id, token_number, user_id, service_id, status, joined_at, called_at, notified, served_by_id, finished_at, hidden_from_staff)
notifications(id, user_id, message, is_read, created_at)
Status: WAITING, CALLED, COMPLETED, CANCELLED, SKIPPED. Roles: STUDENT, STAFF, ADMIN.

## APIs
Public: POST /api/auth/register, /api/auth/login, GET /api/test
Any logged-in: GET /api/services, GET /api/notifications, PUT /api/notifications/read-all
Student: POST /api/queue/join/{serviceId}, GET /api/queue/my, PUT /api/queue/cancel/{entryId}
Staff: GET /api/staff/services, GET /api/staff/services/{id}/queue, PUT /api/staff/services/{id}/call-next,
       PUT /api/staff/entries/{id}/complete, PUT /api/staff/entries/{id}/skip,
       GET /api/staff/history, PUT /api/staff/history/clear
Admin: POST/GET /api/admin/services, PUT /api/admin/services/{id}/assign-staff,
       POST/GET /api/admin/staff, GET /api/admin/queue/{serviceId}, GET /api/admin/history
First admin (auto-created): admin@queueless.com / admin123

## Logic
- Position = WAITING people with smaller token + 1
- Estimated wait = (people ahead + person being served) x avg_service_minutes
- Join locks the service row (PESSIMISTIC_WRITE) so two people never get the same token; unique(service_id, token_number) is the backup
- Rules: one active token per user per service; student can cancel a WAITING token; staff can skip; call-next blocked while someone is CALLED
- Scheduler (every 60 s): WAITING + notified=false + estimated wait <= 10 min -> save notification + email, set notified=true
- Work history: servedBy + finishedAt are saved when staff call/complete/skip. Staff see only their own COMPLETED/SKIPPED rows; "Clear history" only sets hidden_from_staff=true (admin still sees everything, latest 200)
- Email is optional (env vars MAIL_USERNAME, MAIL_PASSWORD, Gmail app password). Without it, emails are only logged.
- Automated phone call (Twilio): optional, demo-only, not built

## Progress
- History feature added (staff history + clear, admin full history and staff performance table)
- Phase 1-11 code written and delivered as one project (frontend build verified, backend not yet run)
- Next: run it, fix any errors, test the full flow, push to GitHub (remove passwords first), then interview questions per module
