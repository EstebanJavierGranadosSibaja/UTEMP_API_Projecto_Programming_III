📚 UTEMP: University Task and Evaluation Management Platform

Welcome to UTEMP! This project aims to simplify university task management and grading through a comprehensive digital platform for students and professors. Developed as part of a software design and development course, UTEMP provides essential functionalities to manage courses, assignments, and grades within a university model.

🌟 Project Overview

🎯 Objectives

Knowledge Application: Apply course concepts in software design and development, leveraging modern technologies and best practices.
Platform Development: Create a digital platform to manage student assignments and grades. This includes university modeling, course and assignment management, as well as a system for students to submit their work for review by professors and automated grading using a simulated AI system.

💡 What UTEMP Solves

Managing student assignments and grades can be time-consuming and complex. UTEMP streamlines this process by allowing students to submit assignments directly to the platform, enabling professors to review and grade work seamlessly, with the aid of AI emulation for preliminary grading. It bridges the gap between students and professors, facilitating efficient communication, grading, and task management.

🔑 Key Features

User Management 👥

Registration & Authentication: Students and professors can register and log in to the platform, with secure session management via JWT.
Roles & Permissions: Role-based access control for granular permissions management (students, professors, administrators).

University Management 🏫

University Information: Define one or more universities, including faculties and departments.
Course Management 📚

Course Creation: Professors can create and manage courses, assign students, and edit course details.
Student Enrollment: Students can enroll in available courses.

Assignment Management 📄

Assignment Creation: Professors can create assignments, set deadlines, and specify requirements.
Work Submission: Students can submit assignments directly to the platform.
Assignment Review: Students can view a list of their courses and grades, and professors can review all assignments per course.

Automated and Manual Grading 🎓

AI-Emulated Auto-Grading: Simulated AI (using random functions and memorization) provides preliminary grading and feedback. Once an assignment is uploaded, it generates consistent feedback and scores, mimicking an AI-based review.
Manual Review: Professors can manually review, grade, and comment on student work.

Asynchronous Processes ⚙️

Handling Long Tasks: Async processes manage lengthy tasks, like auto-grading, keeping the application responsive.
Email Notifications: Notify professors when a student uploads work, receives a grade, or is enrolled in a course.

Pagination and File Management 📑

Pagination: Enables efficient navigation of large datasets on both backend and GUI.
Secure File Uploads: Students upload their work in secure chunks (up to 512KB each), storing only metadata in the database
