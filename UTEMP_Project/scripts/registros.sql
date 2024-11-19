INSERT INTO users (name, email, password, identification_number, state, role, permissions, created_at, last_update)
VALUES
    ('yo', 'yo@example.com', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK',
     '123456789', 'ACTIVE', 'ADMIN',
     'ALL_PERMISSIONS,MANAGE_USERS,GET_TEACHER_COURSES', NOW(), NOW()),

    ('Bob Smith', 'bob.smith@example.com', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK',
     '987654321', 'INACTIVE', 'TEACHER',
     'MANAGE_UNIVERSITIES,MANAGE_FACULTIES,GET_STUDENT_ENROLLMENTS', NOW(), NOW()),

    ('Charlie Brown', 'charlie.brown@example.com', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK',
     '123123123', 'ACTIVE', 'TEACHER',
     'MANAGE_COURSES,MANAGE_ASSIGNMENTS,ADD_TEACHER_COURSES', NOW(), NOW()),

    ('Diana Prince', 'diana.prince@example.com', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK',
     '456456456', 'INACTIVE', 'STUDENT',
     'REMOVE_STUDENT_COURSES,ADD_UNIVERSITY_FACULTIES', NOW(), NOW()),

    ('Edward Norton', 'edward.norton@example.com', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK',
     '789789789', 'ACTIVE', 'STUDENT',
     'GET_FACULTY_DEPARTMENTS,ADD_DEPARTMENT_COURSES,REMOVE_DEPARTMENT_COURSES', NOW(), NOW());


INSERT INTO universities (name, location, created_at, last_update)
VALUES ('University A', 'Location A', NOW(), NOW()),
       ('University B', 'Location B', NOW(), NOW()),
       ('University C', 'Location C', NOW(), NOW()),
       ('University D', 'Location D', NOW(), NOW()),
       ('University E', 'Location E', NOW(), NOW());

INSERT INTO faculties (name, university_id, created_at, last_update)
VALUES ('Faculty of Engineering', 1, NOW(), NOW()),
       ('Faculty of Sciences', 2, NOW(), NOW()),
       ('Faculty of Arts', 3, NOW(), NOW()),
       ('Faculty of Business', 4, NOW(), NOW()),
       ('Faculty of Medicine', 5, NOW(), NOW());

INSERT INTO departments (name, faculty_id, created_at, last_update)
VALUES ('Computer Science', 1, NOW(), NOW()),
       ('Physics', 2, NOW(), NOW()),
       ('Literature', 3, NOW(), NOW()),
       ('Economics', 4, NOW(), NOW()),
       ('Neuroscience', 5, NOW(), NOW());

INSERT INTO courses (name, description, state, department_id, teacher_id, created_at, last_update)
VALUES ('Algorithms', 'Introduction to algorithms', 'ACTIVE', 1, 1, NOW(), NOW()),
       ('Quantum Mechanics', 'Fundamentals of quantum mechanics', 'ACTIVE', 2, 2, NOW(), NOW()),
       ('Creative Writing', 'Basics of creative writing', 'ARCHIVED', 3, 3, NOW(), NOW()),
       ('Macroeconomics', 'Global economic concepts', 'INACTIVE', 4, 4, NOW(), NOW()),
       ('Neurobiology', 'Study of the nervous system', 'ACTIVE', 5, 5, NOW(), NOW());

INSERT INTO assignments (title, description, state, deadline, course_id, created_at, last_update)
VALUES ('Assignment 1', 'Introduction assignment', 'PENDING', NOW() + INTERVAL '7 DAY', 1, NOW(), NOW()),
       ('Assignment 2', 'Advanced topics', 'ONGOING', NOW() + INTERVAL '14 DAY', 2, NOW(), NOW()),
       ('Assignment 3', 'Creative project', 'COMPLETED', NOW() + INTERVAL '30 DAY', 3, NOW(), NOW()),
       ('Assignment 4', 'Economic models', 'CANCELLED', NOW() + INTERVAL '7 DAY', 4, NOW(), NOW()),
       ('Assignment 5', 'Lab research', 'PENDING', NOW() + INTERVAL '21 DAY', 5, NOW(), NOW());

INSERT INTO enrollments (state, course_id, student_id, created_at, last_update)
VALUES ('ENROLLED', 1, 1, NOW(), NOW()),
       ('COMPLETED', 2, 2, NOW(), NOW()),
       ('DROPPED', 3, 3, NOW(), NOW()),
       ('ENROLLED', 4, 4, NOW(), NOW()),
       ('ENROLLED', 5, 5, NOW(), NOW());
