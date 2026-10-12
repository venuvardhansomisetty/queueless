QueueLess

A fullstack queue and token management system that helps people book a place in line and avoid standing in long queues.

Why I built it: I once lost two days at my college collecting certificates, because of long queues in the morning and afternoon and counters closing for lunch. QueueLess lets students join a service queue from their phone, see their position and estimated waiting time, and get a reminder before their turn.

Features

Student

Register and log in
Pick a service, join its queue and get a token number
See live position and estimated waiting time (updates every 5 seconds)
Cancel a waiting token
In-app notification and email about 10 minutes before the turn, and when it is their turn

Staff

See the services assigned to them
Call the next person, mark them completed, or skip them if they do not arrive
Personal work history with a "Clear history" option

Admin

Create services and set the average service time
Create staff accounts and assign staff to services
Watch live queues
See full work history and a performance summary of all staff

Rules built in: one active token per person per service, no duplicate token numbers even when two people join at the same moment (database row lock plus unique constraint), role-based access on every API.

Tech stack
Part	Technology
Backend	Java 21, Spring Boot, Spring Data JPA, Spring Security
Authentication	JWT (jjwt), BCrypt password hashing
Database	MySQL
Frontend	React (Vite), React Router
Updates	Polling every 5 seconds, scheduled job every minute for reminders
Folder structure
queueless/
├── backend/    Spring Boot (model, repository, service, controller, dto, security)
└── frontend/   React (pages, components)
How it works
React page  ->  Controller  ->  Service (rules)  ->  Repository  ->  MySQL
Estimated wait = (people ahead + person being served) x average service minutes
A scheduler runs every minute and sends a reminder once when the estimated wait is 10 minutes or less
Run it on your computer

Requirements: JDK 21, MySQL, Node.js

Create the database:
sql
   CREATE DATABASE queueless_db;
In backend/src/main/resources/, create a file named local.properties (it is ignored by Git) and write your own values:
properties
   spring.datasource.url=jdbc:mysql://localhost:3306/queueless_db
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   jwt.secret=put-a-long-random-text-of-at-least-32-characters-here

(Change the port in the URL if your MySQL does not use 3306.) 3. Start the backend:

   cd backend
   ./mvnw spring-boot:run

On the first start it creates the tables and one admin account: admin@queueless.com / admin123. Change this password for any real use. 4. Start the frontend in a new terminal:

   cd frontend
   npm install
   npm run dev
Open http://localhost:3000

Optional email reminders (Gmail): create a Gmail app password and set the environment variables MAIL_USERNAME and MAIL_PASSWORD before starting the backend. Without them, emails are only printed in the backend console.

Main APIs
Role	APIs
Public	POST /api/auth/register, POST /api/auth/login
Student	GET /api/services, POST /api/queue/join/{serviceId}, GET /api/queue/my, PUT /api/queue/cancel/{entryId}
Staff	GET /api/staff/services, PUT /api/staff/services/{id}/call-next, PUT /api/staff/entries/{id}/complete, PUT /api/staff/entries/{id}/skip, GET /api/staff/history, PUT /api/staff/history/clear
Admin	POST /api/admin/services, POST /api/admin/staff, PUT /api/admin/services/{id}/assign-staff, GET /api/admin/queue/{serviceId}, GET /api/admin/history
Screenshots

<img width="1365" height="717" alt="image" src="https://github.com/user-attachments/assets/6c4754d1-d779-425c-9b6f-45557a85100e" />

<img width="1365" height="724" alt="image" src="https://github.com/user-attachments/assets/2c41618c-8bfc-4059-9943-9eafcfd06cac" />

<img width="1193" height="632" alt="image" src="https://github.com/user-attachments/assets/ace6f86a-aac7-4493-b90e-2181bc52219d" />

<img width="1365" height="724" alt="image" src="https://github.com/user-attachments/assets/4dd1628a-8e31-4d11-814a-837b4f491651" />


Planned improvements
Counter opening hours and lunch break handling
Documents checklist for each service
Admin analytics (busiest hours, average waiting time)
SMS / WhatsApp / call reminders
Live "Now serving" display screen
Deployment and automated tests
Author

Venuvardhan
