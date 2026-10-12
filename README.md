# QueueLess - how to put this on your computer

## A. Backend (Spring Boot)

1. In VS Code open your folder  D:\projects\queueless  (the parent folder).
2. Inside  backend\src\main\java\com\queueless  DELETE everything that is there
   (for example the old folder named "backend" that contains BackendApplication.java).
   Also DELETE the folder  backend\src\test  (we do not need the auto-made test).
3. Copy the folders from this zip into your project:
      backend\src\main\java\com\queueless\*     ->  same place in your project
      backend\src\main\resources\application.properties  ->  replace your file
   After copying, this file must exist:  backend\src\main\java\com\queueless\BackendApplication.java
4. Open backend\pom.xml and add the dependencies written in backend\pom-additions.txt
5. Open application.properties and write your real MySQL password in spring.datasource.password
6. Make sure the database exists:   CREATE DATABASE queueless_db;
7. In the terminal:
      cd backend
      .\mvnw spring-boot:run
   Wait for "Started BackendApplication" and the line "First admin created".
8. Check  http://localhost:8080/api/test  ->  QueueLess backend is running

## B. Frontend (React)

1. Copy the  frontend  folder from this zip next to  backend  (D:\projects\queueless\frontend).
2. In a NEW terminal:
      cd frontend
      npm install
      npm run dev
3. Open  http://localhost:3000

## C. Test the full flow

1. Log in as admin:  admin@queueless.com / admin123
2. Add a service (example: Fee Counter, 5 minutes). Add a staff member. Assign the staff to the service.
3. Open a second browser (or a private window) and register 3 students. Each one joins the queue.
   The 3rd student has 2 people ahead, so the estimated wait is 10 minutes and the
   10-minute reminder arrives within 1 minute (check the notifications box).
4. Log in as the staff member (another browser). Press "Call next", then "Mark completed".
   The student's screen updates by itself every 5 seconds.

## D. Email reminders (optional)

Without setup the reminder is only printed in the backend console. To send real emails with Gmail:
turn on 2-step verification, create an "App password", then in the SAME terminal where you start the backend:
      $env:MAIL_USERNAME="yourgmail@gmail.com"
      $env:MAIL_PASSWORD="your-16-letter-app-password"
      .\mvnw spring-boot:run

## E. Before you push to GitHub
Remove your MySQL password from application.properties and never write the Gmail app password in any file.
