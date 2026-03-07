# SupportStack System Design Specification

## 1. Executive Summary

### 1.1 Project Overview
SupportStack is a web-based helpdesk/ticketing system for a school environment that enables students to submit support requests and track their progress while allowing staff and administrators to manage, update, and resolve tickets through internal dashboards.

The system includes:
- Spring Boot backend REST API
- React web application
- Android mobile application

SupportStack uses PostgreSQL (Supabase) for persistence and implements secure authentication using local credentials (username/email + password) and Google OAuth/OpenID Connect (Google Sign-In).

### 1.2 Objectives
- Develop a functional ticketing system for students and staff with clear ticket lifecycle management.
- Implement secure authentication using local credentials and Google OAuth/OpenID Connect.
- Enforce role-based access control for Student, Staff, and Admin users.
- Maintain a full ticket change history (audit log) for accountability and traceability.
- Implement soft deletion with an Admin-managed Trash view (restore + permanent delete).
- Design responsive and consistent UI/UX across web and mobile platforms.
- Deploy system components to production-ready environments.

### 1.3 Scope

#### Included Features
- User registration and authentication (username, email, password, confirm password)
- User login using username/password or email/password
- Google Sign-In authentication (OAuth/OpenID Connect)
- Role-based access control (Student, Staff, Admin)
- Admin-seeded initial administrator and admin role promotion (with "last admin" protection)
- First-time login profile completion (e.g., first name, last name)
- Ticket creation and tracking for students
- Ticket assignment/management workflow for staff/admin (status updates)
- Student ticket cancellation (status set to `CANCELED`)
- Ticket history/audit log (who changed what, when)
- Soft delete for tickets with Admin Trash view:
  - Restore
  - Permanent delete with confirmation
- Email notifications via Brevo SMTP for key ticket events (ticket created, ticket status changed)
- PostgreSQL database with relational schema (Supabase)
- Responsive web interface
- Native Android mobile application

#### Excluded Features
- GitHub API integration (repository metadata display)
- Microsoft OAuth/OpenID Connect authentication
- Real-time chat support
- Push notifications (mobile/desktop)
- Knowledge base / FAQ authoring module
- File attachment uploads (unless added later)
- Advanced analytics / SLA automation

## 2. Introduction

### 2.1 Purpose
This document is the comprehensive design specification for SupportStack. It defines:
- Project scope
- Core user journeys
- Functional requirements
- Non-functional requirements
- Architecture
- API contracts
- Database design
- UI/UX wireframes
- Implementation roadmap

The goal is to ensure consistent behavior across authentication, ticket workflow, auditing, email notifications, and administrative management.

## 3. Functional Requirements Specification

### 3.1 Project Overview
- Project Name: SupportStack
- Domain: School Helpdesk / Ticketing

Primary Users:
- Students
- Staff
- Administrators

Problem Statement:
Students and staff need a structured, trackable way to request and deliver technical or administrative support without relying on informal channels (ad-hoc messages, unmanaged emails).

Solution:
SupportStack provides a centralized ticketing workflow with role-based access, email notifications for key events, and audit history to ensure transparency, traceability, and improved response handling.

### 3.2 Core User Journeys

#### Journey 1: First-time Student Ticket Submission (Local Registration)
1. Student visits the web application.
2. Student clicks "Sign Up" and creates an account (username, email, password, confirm password).
3. System validates required fields and prevents duplicate email/username registration.
4. Student logs in using username/password or email/password.
5. Student is prompted to complete profile setup (e.g., first name, last name) if required.
6. Student creates a new support ticket (title + description + department).
7. System confirms ticket creation and sends an email notification to the student.
8. Student views ticket status and updates over time.

#### Journey 2: First-time Student Ticket Submission (Google Sign-In)
1. Student visits the web application.
2. Student clicks "Continue with Google" and signs in.
3. System verifies Google identity and creates a local Student profile automatically (JIT provisioning) if first login.
4. Student is prompted to complete profile setup (e.g., first name, last name) if required.
5. Student creates a new support ticket.
6. System confirms ticket creation and sends an email notification to the student.
7. Student views ticket status and updates over time.

#### Journey 3: Student Tracks and Cancels Ticket
1. Student logs in (local or Google).
2. Student views a list of submitted tickets.
3. Student opens a ticket to view details and history.
4. If needed, student cancels the ticket (status becomes `CANCELED`).
5. Ticket remains visible in history; student cannot delete the record.

#### Journey 4: Staff Processes Ticket
1. Staff account exists (created/promoted by an Admin).
2. Staff logs in (local or Google).
3. Staff views assigned/unassigned tickets (ticket queue).
4. Staff updates ticket status (`OPEN` -> `IN_PROGRESS` -> `RESOLVED`).
5. System logs each change to ticket history and sends status-change email to the student.
6. If a ticket is `CANCELED`, staff may soft-delete it (removes from normal views).

#### Journey 5: Admin Oversight and Trash Management
1. Seeded Admin logs in (created on first deployment if no Admin exists).
2. Admin creates Staff accounts and manages roles.
3. Admin views Trash (soft-deleted tickets).
4. Admin restores tickets if deletion was accidental.
5. Admin permanently deletes tickets from Trash with confirmation.
6. Admin may promote another user to Admin, but system prevents removal/demotion of the last Admin.

### 3.3 Feature List (MoSCoW)

#### Must Have
- User authentication (register, login, logout)
- Login via username/password or email/password
- Password hashing (BCrypt)
- Google OAuth/OpenID Connect login
- JIT provisioning for Google sign-in (create local user on first login)
- First-time login profile completion
- Role-based access control: Student, Staff, Admin
- Ticket creation and student ticket viewing
- Ticket status management by Staff/Admin
- Student can cancel own ticket (`status -> CANCELED`)
- Ticket history/audit logging (who/what/when)
- Email notifications via Brevo SMTP:
  - Ticket created (to student)
  - Ticket status changed (to student)
- Soft delete + Trash:
  - Staff: soft-delete canceled tickets only
  - Admin: soft-delete any ticket
  - Admin: restore and permanently delete from Trash

#### Should Have
- Ticket assignment to staff members (if workflow supports it)
- Search/filter tickets (by status, date, department)
- Validation and clear user feedback (errors/success states)

#### Could Have
- Ticket categories, priority levels, tagging
- Internal staff notes on tickets (not visible to students)
- Basic reporting counts (open vs resolved tickets)
- Email templates with branding

#### Won't Have (for this version)
- GitHub API integration
- Microsoft sign-in
- Real-time chat, push notifications
- Knowledge base / FAQ module
- Attachments/file uploads (unless later required)
- Advanced analytics / SLA automation

### 3.4 Detailed Feature Specifications

#### Feature: User Authentication (Local)
Screens:
- Registration
- Login

Registration Fields:
- Username
- Email
- Password
- Confirm Password

Login Fields:
- Username or Email
- Password

Validation:
- Required fields cannot be empty
- Email must be valid format and unique
- Username must be unique
- Password minimum length (e.g., 8+)
- Confirm password must match password

API Endpoints:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`

Security:
- Password hashing using BCrypt
- JWT access token for protected endpoints

#### Feature: User Authentication (Google OAuth/OIDC)
Screens:
- Login

Validation:
- Verify Google ID token signature, issuer, audience, and expiry

API Endpoints:
- `POST /api/v1/auth/google`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

Security:
- Verify Google ID token
- Issue SupportStack JWT
- Enforce role-based authorization checks

#### Feature: Role and User Management (Admin)
Screens:
- Admin User Management (Users List, Role Management)

Fields:
- Email
- Role (`STAFF`, `ADMIN`)
- Account Status (optional)

Validation:
- Email uniqueness
- Prevent removal/demotion of last admin

API Endpoints:
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/users/{userId}`
- `PATCH /api/v1/admin/users/{userId}/role`
- `PATCH /api/v1/admin/users/{userId}/status`

Access Control:
- Admin role required

#### Feature: First-Time Profile Completion
Screens:
- Complete Profile

Fields:
- First Name
- Last Name
- Optional profile fields (if enabled)

Validation:
- Required fields cannot be empty
- Length constraints
- Input sanitization

API Endpoint:
- `PATCH /api/v1/users/me/profile`

Business Rule:
- Required on first login before full access (e.g., before creating tickets)

#### Feature: Ticket Submission (Student)
Screens:
- Create Ticket
- My Tickets (List)
- Ticket Details

Fields:
- Title/Subject
- Description
- Department

Validation:
- Required fields cannot be empty
- Description length limits
- Input sanitization

API Endpoints:
- `POST /api/v1/tickets`
- `GET /api/v1/tickets`
- `GET /api/v1/tickets/{ticketId}`

Business Rules:
- Students can only view their own tickets
- Ticket creation triggers "ticket created" email notification

#### Feature: Ticket Workflow Management (Staff/Admin)
Screens:
- Ticket Queue/List
- Ticket Details/Update

Functions:
- View tickets
- Update ticket fields
- Update ticket status
- Assign ticket to staff (optional)

Validation:
- Only Staff/Admin can update workflow fields
- Status must be a valid allowed value

API Endpoints:
- `PATCH /api/v1/tickets/{ticketId}`
- `PATCH /api/v1/tickets/{ticketId}/status`
- `PATCH /api/v1/tickets/{ticketId}/assign` (optional)

Business Rules:
- Status updates trigger "ticket status changed" email notification
- All edits are recorded in ticket history

#### Feature: Ticket Cancellation (Student)
Screens:
- Ticket Details

Functions:
- Cancel own ticket

Validation:
- Only ticket owner can cancel
- Ticket cannot be canceled if already resolved/deleted (policy-based)

API Endpoint:
- `PATCH /api/v1/tickets/{ticketId}/cancel`

Business Rules:
- Cancel changes status to `CANCELED`
- Cancellation is recorded in ticket history
- Student cannot delete tickets

#### Feature: Ticket History / Audit Log
Screens:
- Ticket Details (History section)

Data Captured:
- Changed by (user)
- Timestamp
- Field(s) changed
- Old value -> New value

Validation:
- History records are immutable once written

API Endpoint:
- `GET /api/v1/tickets/{ticketId}/history`

Business Rules:
- Students can view history for their own tickets
- Staff/Admin can view history for managed tickets

#### Feature: Ticket Deletion + Trash (Soft Delete)
Screens:
- Ticket Details (role-based delete action)
- Trash (Admin)

Functions:
- Soft delete
- View Trash
- Restore
- Permanent delete

Validation:
- Staff may delete only `CANCELED` tickets
- Admin may delete any ticket
- Permanent delete requires confirmation

API Endpoints:
- `DELETE /api/v1/tickets/{ticketId}` (soft delete)
- `GET /api/v1/admin/trash/tickets`
- `PATCH /api/v1/admin/trash/tickets/{ticketId}/restore`
- `DELETE /api/v1/admin/trash/tickets/{ticketId}/permanent`

Business Rules:
- Soft-deleted tickets are hidden from normal lists
- Admin can restore
- Admin can permanently delete from Trash

#### Feature: Email Notifications (Brevo SMTP)
Screens:
- None (system-driven)

Triggers:
- Ticket created
- Ticket status changed

Validation/Resilience:
- Handle SMTP failures gracefully (retry/log)

Integration:
- Brevo SMTP credentials stored in environment variables

### 3.5 Acceptance Criteria
- AC-1: Successful User Registration (Local)
  - Given I am a new user
  - When I enter a unique username and valid unique email, with a strong matching password
  - Then my account should be created successfully and I should be able to login

- AC-2: Successful User Login (Local)
  - Given I am a registered user
  - When I login using email/password or username/password
  - Then I should be authenticated successfully and redirected to the dashboard

- AC-3: Successful Google Login
  - Given I am a user
  - When I sign in using a valid Google account
  - Then the system should authenticate me successfully and create a local user profile if first login

- AC-4: First-Time Profile Completion Required
  - Given I am a first-time user
  - When I submit valid profile details (e.g., first name and last name)
  - Then my profile should be saved and I should be allowed to access dashboard and create tickets

- AC-5: Student Ticket Creation + Email Notification
  - Given I am logged in as a student and profile is complete
  - When I submit a valid ticket
  - Then the ticket should be created with initial status (e.g., `OPEN`), appear in my list, and send confirmation email

- AC-6: Staff Updates Ticket Status + Email + History
  - Given a ticket exists and I am logged in as staff
  - When I change status (e.g., `OPEN -> IN_PROGRESS`)
  - Then status should be saved, history entry created (old/new + timestamp + actor), and student emailed

- AC-7: Student Cancels Own Ticket
  - Given I am logged in as a student and have an existing ticket
  - When I cancel my ticket
  - Then status becomes `CANCELED`, history is recorded, and ticket remains visible in student history/list

- AC-8: Soft Delete Rules Enforced (Staff vs Admin)
  - Given I am staff
  - When I attempt to delete a non-`CANCELED` ticket
  - Then action is blocked with authorization/validation message
  - Given ticket is `CANCELED`
  - When I delete it
  - Then it is soft-deleted and removed from normal lists
  - Given I am admin
  - When I delete any ticket
  - Then it is soft-deleted and appears in Trash

- AC-9: Trash Restore and Permanent Delete
  - Given I am admin and a ticket exists in Trash
  - When I restore it
  - Then it is removed from Trash and returned to normal lists
  - When I permanently delete and confirm prompt
  - Then it is removed permanently and cannot be restored

- AC-10: Prevent Removing the Last Admin
  - Given I am admin and only one admin exists
  - When I attempt to demote/delete that admin account
  - Then the system prevents the action and displays a clear "last admin cannot be removed" message

## 4. Non-Functional Requirements

### 4.1 Performance Requirements
- API response time: <= 2 seconds for 95% of requests
- Web page load time: <= 3 seconds on broadband
- Mobile app cold start: <= 3 seconds
- Support at least 100 concurrent users
- Database queries complete within 500 ms

### 4.2 Security Requirements
- HTTPS for all communications
- JWT token authentication for protected endpoints
- Password hashing with BCrypt (recommended salt rounds `10-12`)
- SQL injection prevention via parameterized queries / Spring Data JPA
- XSS mitigation via input validation, output encoding, and secure headers where applicable
- Rate limiting: 100 requests/minute per IP
- Admin endpoints require role verification

### 4.3 Compatibility Requirements
- Web Browsers: Chrome, Firefox, Safari, Edge (latest 2 versions)
- Android: API Level 24+ (Android 7.0+)
- Screen Sizes: Mobile (360px+), Tablet (768px+), Desktop (1024px+)
- Operating Systems: Windows 10+, macOS 10.15+, Linux Ubuntu 20.04+

### 4.4 Usability Requirements
- Complete first ticket submission within 5 minutes for new users
- WCAG 2.1 Level AA compliance for web
- Consistent navigation across pages
- Clear error messages with recovery options
- Touch targets minimum `44x44px` on mobile
- Keyboard navigation support

## 5. System Architecture

### 5.1 Component Diagram (Target)
Note: The architecture should be represented as a component diagram in documentation.

Technology Stack:
- Backend: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- Database: PostgreSQL 14+ (Supabase)
- Web Frontend: React 18, TypeScript, Tailwind CSS, Axios
- Mobile: Kotlin, Jetpack Compose, Retrofit, Room
- Build Tools: Maven (Backend), npm/yarn (Web), Gradle (Android)
- Deployment: Railway/Heroku (Backend), Vercel/Netlify (Web), APK (Mobile)

## 6. API Contract and Communication

### 6.1 API Standards
Base URL:
- `https://[server_hostname]:[port]/api/v1`

Data Format:
- JSON for all requests/responses

Authentication:
- Bearer token (JWT) in `Authorization` header

Standard Response Structure:
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "<iso8601>"
}
```

Error Response Structure:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "<error-code>",
    "message": "<message>",
    "details": null
  },
  "timestamp": "<iso8601>"
}
```

### 6.2 Endpoint Specifications

#### Authentication Endpoints

User Registration:
- Description: Register a new local user account
- API URL: `/auth/register`
- HTTP Method: `POST`
- Authentication: None

Request Payload:
```json
{
  "username": "<username>",
  "email": "<email>",
  "password": "<password>",
  "confirmPassword": "<confirmPassword>"
}
```

Success Response:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "<id>",
      "username": "<username>",
      "email": "<email>",
      "role": "STUDENT"
    },
    "accessToken": "<token>"
  },
  "error": null,
  "timestamp": "<iso8601>"
}
```

User Login:
- Description: Login via username or email
- API URL: `/auth/login`
- HTTP Method: `POST`
- Authentication: None

Request Payload:
```json
{
  "login": "<username-or-email>",
  "password": "<password>"
}
```

Success Response:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "<id>",
      "username": "<username>",
      "email": "<email>",
      "role": "<role>"
    },
    "accessToken": "<token>"
  },
  "error": null,
  "timestamp": "<iso8601>"
}
```

Google Sign-In:
- Description: Authenticate using Google ID token; create local user if first login
- API URL: `/auth/google`
- HTTP Method: `POST`
- Authentication: None

Request Payload:
```json
{
  "idToken": "<google_id_token>"
}
```

Success Response:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "<id>",
      "email": "<email>",
      "role": "STUDENT"
    },
    "accessToken": "<token>"
  },
  "error": null,
  "timestamp": "<iso8601>"
}
```

Get Current User:
- Description: Return currently authenticated user profile and role
- API URL: `/auth/me`
- HTTP Method: `GET`
- Authentication: Bearer token (JWT)

Logout:
- Description: Log out the authenticated user
- API URL: `/auth/logout`
- HTTP Method: `POST`
- Authentication: Bearer token (JWT)

### 6.3 Error Handling
HTTP Status Codes:
- `200 OK` - Successful request
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Authentication required/failed
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource does not exist
- `409 Conflict` - Duplicate resource
- `500 Internal Server Error` - Server error

Common Error Codes:
- `AUTH-001`: Invalid credentials
- `AUTH-002`: Token expired/invalid
- `AUTH-003`: Insufficient permissions
- `VALID-001`: Validation failed
- `DB-001`: Resource not found
- `DB-002`: Duplicate entry
- `SYSTEM-001`: Internal server error

## 7. Database Design

### 7.1 Entity Relationship Diagram (Target)
Note: The schema should be represented as an ERD in documentation.

#### Tables and Columns

`users`
- `id` (PK)
- `username` (UNIQUE)
- `email` (UNIQUE)
- `password_hash` (nullable for Google-only users)
- `first_name` (nullable)
- `last_name` (nullable)
- `role` (`STUDENT | STAFF | ADMIN`)
- `is_profile_complete` (BOOLEAN)
- `google_sub` (nullable)
- `created_at`
- `updated_at`
- `last_login_at`

Recommended Constraint:
- Unique `google_sub` when non-null

`departments`
- `id` (PK)
- `code` (UNIQUE)
- `name`
- `description` (nullable)
- `is_active` (BOOLEAN)
- `created_by_user_id` (FK -> `users.id`)
- `updated_by_user_id` (FK -> `users.id`, nullable)
- `created_at`
- `updated_at`

`tickets`
- `id` (PK)
- `ticket_number` (UNIQUE)
- `created_by_user_id` (FK -> `users.id`)
- `department_id` (FK -> `departments.id`)
- `title`
- `description`
- `status` (`OPEN | IN_PROGRESS | RESOLVED | CANCELED`)
- `created_at`
- `updated_at`
- `is_deleted` (BOOLEAN, default `false`)
- `deleted_at` (nullable timestamp)
- `deleted_by_user_id` (FK -> `users.id`, nullable)

`ticket_history` (append-only)
- `id` (PK)
- `ticket_id` (FK -> `tickets.id`)
- `changed_by_user_id` (FK -> `users.id`)
- `change_type` (`CREATED | UPDATED | STATUS_CHANGED | DELETED | RESTORED | CANCELED`)
- `field_name` (nullable)
- `old_value` (nullable)
- `new_value` (nullable)
- `created_at`

#### Relationships (Cardinality)
- `users 1 -> many tickets` (`tickets.created_by_user_id -> users.id`)
- `departments 1 -> many tickets` (`tickets.department_id -> departments.id`)
- `tickets 1 -> many ticket_history` (`ticket_history.ticket_id -> tickets.id`)
- `users 1 -> many ticket_history` (`ticket_history.changed_by_user_id -> users.id`)
- `users 1 -> many departments` (audit)
  - `departments.created_by_user_id -> users.id`
  - `departments.updated_by_user_id -> users.id`

## 8. UI/UX Design

### 8.1 Web Application Wireframes (Target)
Note: These should be represented as Figma wireframes.

Landing/Login Page:
- Header: Logo
- Content: App description, login form
- Fields: Username/Email, Password
- Buttons: Login, Continue with Google
- Link: Create account
- Footer: Links, copyright

Registration Page:
- Fields: Username, Email, Password, Confirm Password
- Buttons: Create Account, link back to Login
- Inline validation messages

Student Dashboard (Tickets Listing):
- Header: Logo, Search Tickets, Notifications, User Menu
- Page title: My Tickets
- Filters: Status, Department
- Ticket list (table/cards): ticket number, title, department, status badge, created/updated date
- Primary button: Create Ticket
- Empty state supported

Create Ticket Page:
- Back button
- Fields: Department dropdown, Title, Description
- Buttons: Submit Ticket, Cancel
- Validation messages

Ticket Detail Page (with History):
- Back button
- Header info: ticket number, title, status badge
- Metadata: department, creator, created time
- Body: description
- Actions by role:
  - Student: cancel ticket
  - Staff/Admin: change status
  - Admin: soft delete/restore (as applicable)
- History section: action, actor, timestamp, old/new values

Staff/Admin Ticket Queue Page:
- Header: Logo, Search, User Menu
- Filters: Department, Status, date range (optional)
- Table: ticket number, title, creator, department, status, updated at

Admin Dashboard (Departments Management):
- Sidebar: Dashboard, Departments, Tickets, Users
- Departments table: code, name, active/inactive, updated at
- Actions: add, edit, enable/disable

Admin Trash View:
- List soft-deleted tickets
- Actions: Restore, Permanent Delete (with confirmation)

### 8.2 Mobile Application Wireframes (Target)
Note: These should be represented as Figma wireframes.

Bottom Navigation:
- Home/Tickets
- Create
- Notifications
- Profile

Home / My Tickets Screen:
- Search bar
- Filters (Status, Department)
- Ticket cards: title, ticket number, status badge, department, updated date
- Pull-to-refresh

Create Ticket Screen:
- Department dropdown
- Title input
- Description input
- Sticky submit button

Ticket Detail Screen:
- Back arrow
- Ticket summary (status, department, dates)
- Description section
- Status update controls (staff/admin)
- Scrollable history feed

Authentication Screens:
- Login: username/email + password, login, continue with Google
- Registration: username, email, password, confirm password

Profile Screen:
- Name, email, role
- Profile completion indicator
- Sign out button

Mobile-Specific UX:
- Touch targets minimum `44x44px`
- Pull-to-refresh on ticket lists
- Sticky bottom primary actions
- Optional offline caching for previously opened ticket details

Design System:
- Colors:
  - Primary: `#2563EB`
  - Secondary: `#7C3AED`
  - Success: `#10B981`
  - Error: `#EF4444`
- Typography: Inter font family, responsive sizing
- Spacing: 8px grid system
- Components: buttons, inputs, dropdowns, badges, cards, modals
- Responsive breakpoints: 640px, 768px, 1024px

## 9. Project Plan

### 9.1 Timeline

#### Phase 1: Planning and Design (Week 1-2)
Week 1: Requirements and Architecture
- Day 1: Project setup and documentation
- Day 2: Gather requirements, define user roles, use cases
- Day 3: Complete FRS
- Day 4: Complete NFR
- Day 5: System architecture design
- Day 6: Authentication design (Local + Google OAuth), role plan

Week 2: Detailed Design
- Day 1: API specification
- Day 2: API error handling and validation rules
- Day 3: Database design (ERD, constraints)
- Day 4: Audit log and soft delete rules
- Day 5: UI/UX web wireframes (Figma)
- Day 6: UI/UX mobile wireframes (Figma)

#### Phase 2: Backend Development (Week 3-5)
Week 3: Foundation
- Day 1: Spring Boot setup and dependencies
- Day 2: Supabase Postgres config and entities
- Day 3: Security config and JWT utilities
- Day 4: Registration endpoint and validations
- Day 5: Login endpoint and JWT issue
- Day 6: Auth tests and Swagger

Week 4: Core Ticketing Features
- Day 1: Department management (Admin)
- Day 2: Ticket creation and My Tickets
- Day 3: Ticket detail and history read
- Day 4: Status update and history write
- Day 5: Soft delete and Trash endpoints
- Day 6: Brevo email integration

Week 5: Hardening
- Day 1: Google OAuth token verification endpoint
- Day 2: Role guards and last-admin protection
- Day 3: Error handling polish
- Day 4: Performance checks and pagination
- Day 5: Integration testing
- Day 6: Buffer/fixes

#### Phase 3: Web Application Development (Week 6-8)
Week 6: Web Foundation
- Day 1: React + TypeScript setup
- Day 2: Login and Register pages
- Day 3: Student dashboard (tickets list)
- Day 4: Create ticket page
- Day 5: Ticket detail and history UI
- Day 6: Staff queue UI

Week 7: Admin Features
- Day 1: Departments page
- Day 2: Trash view
- Day 3: Role management UI (optional)
- Day 4: Responsive behavior and UI polish
- Day 5: Web integration testing
- Day 6: Buffer/fixes

Week 8: Finalize Web
- Day 1: Accessibility and validation message polish
- Day 2: End-to-end flow testing
- Day 3: Bug fixes
- Day 4: Documentation screenshots
- Day 5: Deployment setup (web + backend)
- Day 6: Buffer

#### Phase 4: Mobile Application (Week 9-10)
Week 9: Android Foundation
- Day 1: Project setup
- Day 2: Login and Register screens
- Day 3: Ticket list and filters
- Day 4: Create ticket
- Day 5: Ticket details and history
- Day 6: Google sign-in (if required on mobile)

Week 10: Complete Mobile App
- Day 1: Staff/Admin features (if required)
- Day 2: UI polish
- Day 3: Testing on device/emulator
- Day 4: APK generation
- Day 5: Documentation screenshots
- Day 6: Buffer

#### Phase 5: Integration and Deployment (Week 11)
Week 11: Integration, Deployment, Submission
- Day 1: End-to-end testing
- Day 2: Bug fixes and optimization
- Day 3: Deployment (backend + web)
- Day 4: Final documentation (ERD, API docs, Figma exports)
- Day 5: Project submission
- Day 6: Buffer
