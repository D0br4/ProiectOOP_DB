# University Grade Management System (ProiectOOP_DB)

A Java Swing desktop application for university academic record management, developed as an object-oriented programming and database project.

## Features
- User authentication and role-specific interfaces for administrators, teachers and students
- Academic grade and attendance management
- Views for student groups and subjects
- PostgreSQL persistence via JDBC

## Technology
Java, Java Swing, JDBC, PostgreSQL, SQL, IntelliJ IDEA.

## Project contents
- `proiect/src/catalog/model/` — original Java sources
- `sql_backup/baza_date.sql` — PostgreSQL backup with **fictional demonstration data** (as provided by the project author)
- `documentatie/` — original project documentation
- `proiect/` — IntelliJ project configuration and module file

## Local setup
1. Install a JDK, IntelliJ IDEA, PostgreSQL, and the PostgreSQL JDBC driver (42.7.2 was referenced by the original IDE project).
2. Create a PostgreSQL database named `CatalogOnline` and restore `sql_backup/baza_date.sql` using a compatible PostgreSQL version. The backup was generated with PostgreSQL 17.7. You may need to use a database administrator account to restore it.
3. Before launching the app, set environment variables `DB_URL` (default `jdbc:postgresql://localhost:5432/CatalogOnline`), `DB_USER` (default `postgres`), and **`DB_PASSWORD` (required)** in your IntelliJ Run Configuration or your environment.
4. Open `proiect/` in IntelliJ IDEA. Ensure the PostgreSQL JDBC driver is on the classpath, select `catalog.model.Main` as the main class, and run.

**Important:** The database connection code uses environment variables instead of a hard-coded password. This is the only intentional source-code change from the submitted project. The existing application logic is otherwise preserved. The packaged version has not been independently run-tested after this configuration adjustment.

## Notes
- This is an educational project, not production software. The original authentication queries and security model should not be used in production without improvements (e.g. salted password hashing and further input validation).
- Do not upload real personal data or reusable credentials in future commits.
