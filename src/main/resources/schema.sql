-- =====================================================
-- Event Management System Database
-- Author : Student Project
-- =====================================================

DROP DATABASE IF EXISTS event_management;
CREATE DATABASE event_management;
USE event_management;

-- ===========================
-- USERS
-- ===========================

CREATE TABLE users(
                      user_id INT PRIMARY KEY AUTO_INCREMENT,
                      full_name VARCHAR(100) NOT NULL,
                      email VARCHAR(100) UNIQUE NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      phone VARCHAR(15),
                      role ENUM('ADMIN','ORGANIZER') NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- VENUES
-- ===========================

CREATE TABLE venues(
                       venue_id INT PRIMARY KEY AUTO_INCREMENT,
                       venue_name VARCHAR(100) NOT NULL,
                       address VARCHAR(255),
                       city VARCHAR(50),
                       state VARCHAR(50),
                       capacity INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- HALLS
-- ===========================

CREATE TABLE halls(
                      hall_id INT PRIMARY KEY AUTO_INCREMENT,
                      venue_id INT NOT NULL,
                      hall_name VARCHAR(100),
                      floor_no INT,
                      capacity INT,
                      FOREIGN KEY(venue_id)
                          REFERENCES venues(venue_id)
                          ON DELETE CASCADE
);

-- ===========================
-- EVENTS
-- ===========================

CREATE TABLE events(
                       event_id INT PRIMARY KEY AUTO_INCREMENT,
                       organizer_id INT,
                       venue_id INT,
                       hall_id INT,
                       event_name VARCHAR(150),
                       description TEXT,
                       category VARCHAR(100),
                       start_date DATE,
                       end_date DATE,
                       start_time TIME,
                       end_time TIME,
                       status ENUM('UPCOMING','ONGOING','COMPLETED','CANCELLED'),
                       FOREIGN KEY(organizer_id)
                           REFERENCES users(user_id),
                       FOREIGN KEY(venue_id)
                           REFERENCES venues(venue_id),
                       FOREIGN KEY(hall_id)
                           REFERENCES halls(hall_id)
);

-- ===========================
-- ATTENDEES
-- ===========================

CREATE TABLE attendees(
                          attendee_id INT PRIMARY KEY AUTO_INCREMENT,
                          full_name VARCHAR(100),
                          email VARCHAR(100) UNIQUE,
                          phone VARCHAR(15),
                          gender ENUM('MALE','FEMALE','OTHER'),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- TICKET TYPES
-- ===========================

CREATE TABLE ticket_types(
                             ticket_type_id INT PRIMARY KEY AUTO_INCREMENT,
                             event_id INT,
                             type_name VARCHAR(50),
                             price DECIMAL(10,2),
                             total_quantity INT,
                             available_quantity INT,
                             FOREIGN KEY(event_id)
                                 REFERENCES events(event_id)
);

-- ===========================
-- BOOKINGS
-- ===========================

CREATE TABLE bookings(
                         booking_id INT PRIMARY KEY AUTO_INCREMENT,
                         attendee_id INT,
                         event_id INT,
                         booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         total_amount DECIMAL(10,2),
                         booking_status ENUM('CONFIRMED','PENDING','CANCELLED'),
                         FOREIGN KEY(attendee_id)
                             REFERENCES attendees(attendee_id),
                         FOREIGN KEY(event_id)
                             REFERENCES events(event_id)
);

-- ===========================
-- PAYMENTS
-- ===========================

CREATE TABLE payments(
                         payment_id INT PRIMARY KEY AUTO_INCREMENT,
                         booking_id INT,
                         payment_method ENUM('UPI','CARD','NET BANKING','CASH'),
                         transaction_id VARCHAR(100),
                         amount DECIMAL(10,2),
                         payment_status ENUM('SUCCESS','FAILED','PENDING'),
                         payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY(booking_id)
                             REFERENCES bookings(booking_id)
);

-- ===========================
-- TICKETS
-- ===========================

CREATE TABLE tickets(
                        ticket_id INT PRIMARY KEY AUTO_INCREMENT,
                        booking_id INT,
                        ticket_number VARCHAR(100) UNIQUE,
                        seat_number VARCHAR(20),
                        qr_path VARCHAR(255),
                        pdf_path VARCHAR(255),
                        FOREIGN KEY(booking_id)
                            REFERENCES bookings(booking_id)
);

-- ===========================
-- VENDORS
-- ===========================

CREATE TABLE vendors(
                        vendor_id INT PRIMARY KEY AUTO_INCREMENT,
                        vendor_name VARCHAR(100),
                        service_type VARCHAR(100),
                        phone VARCHAR(20),
                        email VARCHAR(100)
);

-- ===========================
-- EVENT VENDORS
-- ===========================

CREATE TABLE event_vendors(
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              event_id INT,
                              vendor_id INT,
                              contract_amount DECIMAL(10,2),
                              FOREIGN KEY(event_id)
                                  REFERENCES events(event_id),
                              FOREIGN KEY(vendor_id)
                                  REFERENCES vendors(vendor_id)
);

-- ===========================
-- BUDGET
-- ===========================

CREATE TABLE budgets(
                        budget_id INT PRIMARY KEY AUTO_INCREMENT,
                        event_id INT,
                        total_budget DECIMAL(12,2),
                        expected_income DECIMAL(12,2),
                        FOREIGN KEY(event_id)
                            REFERENCES events(event_id)
);

-- ===========================
-- EXPENSES
-- ===========================

CREATE TABLE expenses(
                         expense_id INT PRIMARY KEY AUTO_INCREMENT,
                         budget_id INT,
                         expense_name VARCHAR(100),
                         amount DECIMAL(10,2),
                         expense_date DATE,
                         FOREIGN KEY(budget_id)
                             REFERENCES budgets(budget_id)
);

-- ===========================
-- SESSIONS
-- ===========================

CREATE TABLE sessions(
                         session_id INT PRIMARY KEY AUTO_INCREMENT,
                         event_id INT,
                         session_title VARCHAR(150),
                         speaker_name VARCHAR(100),
                         start_time DATETIME,
                         end_time DATETIME,
                         FOREIGN KEY(event_id)
                             REFERENCES events(event_id)
);

-- ===========================
-- FEEDBACK
-- ===========================

CREATE TABLE feedback(
                         feedback_id INT PRIMARY KEY AUTO_INCREMENT,
                         attendee_id INT,
                         event_id INT,
                         rating INT,
                         comments TEXT,
                         feedback_date DATE,
                         FOREIGN KEY(attendee_id)
                             REFERENCES attendees(attendee_id),
                         FOREIGN KEY(event_id)
                             REFERENCES events(event_id)
);

-- ===========================
-- CERTIFICATES
-- ===========================

CREATE TABLE certificates(
                             certificate_id INT PRIMARY KEY AUTO_INCREMENT,
                             attendee_id INT,
                             event_id INT,
                             certificate_no VARCHAR(100),
                             pdf_path VARCHAR(255),
                             issue_date DATE,
                             FOREIGN KEY(attendee_id)
                                 REFERENCES attendees(attendee_id),
                             FOREIGN KEY(event_id)
                                 REFERENCES events(event_id)
);