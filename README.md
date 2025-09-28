# Support Management System [Backend]
Support Management System is designed to efficiently manage technical support tickets, meeting essential requirements for submission, tracking, and resolution. Users can submit tickets and monitor their status, while support operators can respond to tickets and adjust their status, priority, and category. Administrators manage user accounts and configure ticket settings, including status, priority and category lists.

This system was originally created as an individual project for a university course in Programming Platforms. The backend was developed using Spring Boot, Spring Security and an H2 database, with the frontend built using HTML and Thymeleaf.

The project was later refactored to a REST API backend to provide a more modern and streamlined solution. During this process, redundant features were removed, and technologies such as Docker, JWT and JUnit were integrated to enhance the system’s functionality and scalability.

## Technologies used
+ Spring Framework, Spring Boot, Spring Security, Spring Data JPA
+ JUnit, Mockito
+ Test Containers
+ REST Assured
+ Thymeleaf
+ Spring Boot Starter Mail
+ Lombok
+ JWT
+ REST API
+ MySQL
+ Docker, Docker Compose

## Features
- General 
  - view knowledge base,
  - view a list of supported software,
  - login and registration in the system,
  - send e-mail with activation link after registration,
  - send emails with notification about status change and new response for ticket
  - three system roles: user, support operator, system administrator.


- User 
  - add new ticket (with images),
  - reply in own ticket,
  - editing account details.


- Operator
  - response to ticket,
  - change ticket status,
  - close ticket,
  - edit ticket.


- Administrator
  - manage users (show list, add, edit, delete, change role),
  - manage priorities,
  - manage categories,
  - manage statuses,
  - manage supported software,
  - manage knowledge base.

##  Database schema
<img width="730" height="806" alt="db_schema" src="https://github.com/user-attachments/assets/9532aee7-1d8b-4431-9ee6-f23f795a7752" />  
_Entity-relationship diagram (ERD) generated using Apache Workbench._

## Running the project with Docker Compose
1. Clone this repository
   ```bash
    git clone https://github.com/Mr-Victor16/support_management_system
   ```
2. Go to the folder with cloned repository
3. Run docker compose
   ```bash
    docker compose up
   ```
   
## Configuration
To set up the system, configure the following properties in **application.properties**:
- Email Configuration:
```
spring.mail.host=<SMTP_server_host> (e.g. smtp.gmail.com)
spring.mail.port=<SMTP_server_port> (e.g. 587 for Gmail)
spring.mail.username=<your_email> (e.g. your_email@gmail.com)
spring.mail.password=<your_email_password>
```
- JWT Configuration::
```
app.activation-link-base-url=<server_host_and_port>/activate/ (e.g. http://localhost:8080/activate/)
sms.app.jwtSecret=<JWT_secret_key>
sms.app.jwtExpirationMs=<token_expiration_time> (e.g. 86400000)
```

## Login details
- **User**
  - Login: user
  - Password: user

- **Operator**
  - Login: operator
  - Password: operator

- **Administrator**
  - Login: admin
  - Password: admin
