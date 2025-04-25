# BTO Management System

A comprehensive system for managing Build-To-Order (BTO) housing projects in Singapore.

## Overview

The BTO Management System is designed to streamline the process of managing BTO housing projects, applications, and enquiries. It provides a platform for applicants to apply for BTO flats, HDB officers to manage projects, and HDB managers to oversee the entire process.

## Features

- **User Management**: Support for different user roles (Applicant, HDB Officer, HDB Manager) with role-based access control.
- **Project Management**: Create, update, and manage BTO projects with details like location, flat types, and application periods.
- **Application Processing**: Handle applications from submission to approval/rejection and booking.
- **Enquiry Management**: Track and respond to enquiries from applicants.
- **Officer Registration**: Allow officers to register for projects they want to manage.
- **Reporting**: Generate various reports on applications, projects, and officer performance.

## Architecture

The system follows a layered architecture with clear separation of concerns:

- **Boundary Layer**: User interfaces and menus
- **Control Layer**: Business logic and controllers
- **Entity Layer**: Domain objects and data models
- **Repository Layer**: Data access and persistence
- **Service Layer**: Business services and operations
- **Utility Layer**: Helper classes and utilities

### Package Structure
- `com.SC2002.bto.boundary`: User interfaces and menu classes
- `com.SC2002.bto.control`: Controller classes for business logic
- `com.SC2002.bto.di`: Dependency injection framework
- `com.SC2002.bto.entities`: Domain model classes
- `com.SC2002.bto.repository`: Data access interfaces and implementations
- `com.SC2002.bto.service`: Business service interfaces and implementations
- `com.SC2002.bto.utils`: Utility classes and helpers

## Design Patterns

The system implements several design patterns:

- **Dependency Injection**: Using a service locator to manage dependencies
- **Repository Pattern**: For data access and persistence
- **Factory Pattern**: For creating objects
- **Singleton Pattern**: For managing shared resources
- **Strategy Pattern**: For implementing different algorithms

## SOLID Principles

The system adheres to SOLID principles:

- **Single Responsibility Principle**: Each class has a single responsibility
- **Open/Closed Principle**: Classes are open for extension but closed for modification
- **Liskov Substitution Principle**: Subtypes can be substituted for their base types
- **Interface Segregation Principle**: Clients are not forced to depend on interfaces they don't use
- **Dependency Inversion Principle**: High-level modules depend on abstractions, not concrete implementations

## Documentation

The codebase is thoroughly documented using JavaDoc comments, providing:
- Detailed descriptions of all classes, methods, and fields
- Parameter and return value documentation
- Explanations of business rules and constraints
- Design pattern implementations and SOLID principles application

JavaDoc documentation has been generated and is available in the `BTOManagementSystem/doc/` directory.

## Getting Started

1. Clone the repository
2. Compile the Java files
3. Run the Main class
4. To view the documentation:
   - Open `BTOManagementSystem/doc/index.html` in a web browser
   - Navigate through the package structure to explore class documentation

## Usage

The system provides a command-line interface (CLI) for interacting with the system. Users can log in as an applicant, HDB officer, or HDB manager, and perform various operations based on their role.

## Data Storage

The system uses CSV files for data storage:

- `ApplicantList.csv`: Stores applicant information
- `OfficerList.csv`: Stores HDB officer information
- `ManagerList.csv`: Stores HDB manager information
- `ProjectList.csv`: Stores project information
- `EnquiryList.csv`: Stores enquiry information
- `RegistrationList.csv`: Stores officer registration information

## Recent Improvements

- **Enhanced Documentation**: Added comprehensive JavaDoc comments throughout the codebase
- **Code Quality**: Improved adherence to coding standards and best practices
- **Maintainability**: Better documentation of complex business rules and constraints

## Contributors

- SC2002 Group Project Team
