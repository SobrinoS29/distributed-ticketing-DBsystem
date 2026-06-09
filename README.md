# distributed-ticketing-DBsystem

## Overview

This repository contains the ticketing and reservations backend for the distributed ticketing platform.
It is one part of a three-repository system and must be used together with:

- distributed-ticketing-FrontEnd: Angular client application
- distributed-ticketing-UserBEsystem: user and authentication backend

The service is responsible for event and ticket data, reservation lifecycle management, queue-related endpoints, and purchase-related operations.

## Role in the Full System

This backend exposes the APIs consumed by the frontend for browsing scenarios, retrieving available tickets, reserving seats, adopting anonymous reservations after login, and marking tickets as sold after checkout.

It should not be treated as a standalone application. The ticketing flow depends on synchronized behavior with the frontend and the user backend, especially for token handling and authenticated reservation adoption.

## Main Responsibilities

- Manage ticket inventory and event availability
- Expose scenario and event browsing endpoints
- Handle seat reservation and release workflows
- Maintain reservation tokens and ownership tokens
- Support expiration cleanup for temporary reservations
- Provide ticket data needed during the checkout flow
- Update ticket state after a successful purchase

## Architecture

This project is a Spring Boot 3.5.6 application packaged as a WAR file and built for Java 17.
It uses Spring Web, Spring Data JPA, Spring Data REST, WebSocket support, and a MySQL database.

The current configuration runs the service on HTTPS port `8080` and connects to a local MySQL database named `esientradas`.

## UML Sequence Diagram

The control flow diagram is split into three images:

### Part 1

<img width="2711" height="1930" alt="SequenceDiagram EsiEntradas Part1" src="https://github.com/user-attachments/assets/df69f175-bddf-4702-a5ce-24ea422d7db7" />

### Part 2

<img width="2710" height="2634" alt="SequenceDiagram EsiEntradas Part2" src="https://github.com/user-attachments/assets/97324704-7de2-49d3-9a37-00dc96d224e5" />

### Part 3

<img width="3395" height="3706" alt="SequenceDiagram EsiEntradas Part3" src="https://github.com/user-attachments/assets/cf9580a0-2937-48e3-98f9-aed0eeceaa76" />

## Key Endpoints

- `/reservas/reservar`: reserve a ticket and create or reuse a reservation token
- `/reservas/liberar`: release a reserved ticket
- `/reservas/getTicketsFromToken`: fetch reserved ticket details for a token
- `/reservas/adoptReservations`: attach anonymous reservations to a logged-in user token
- `/reservas/cleanupExpiredReservations`: clear expired reservations
- `/reservas/updateTicketsAsSold`: mark reserved tickets as sold after checkout

## Data and Persistence Notes

- Seat reservations are tracked through a dedicated token entity.
- Reservation cleanup must preserve consistency between ticket tokens and seat state.
- Reservation logic is designed to reuse the same token for related seats instead of creating inconsistent duplicate records.
- Query design should remain aligned with the underlying JPA inheritance model to avoid discriminator mapping issues.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- Spring Data REST
- Spring WebSocket
- MySQL
- Stripe integration
- MailGun email integration

## Prerequisites

- JDK 17
- Maven Wrapper or Maven
- MySQL running locally or in an accessible environment
- SSL keystore available as configured in `application.properties`

## Local Setup

1. Create the MySQL database expected by the application.
2. Review `src/main/resources/application.properties` and adjust credentials if needed.
3. Start the application with Maven Wrapper or Maven.
4. Ensure the frontend proxy points to this backend on `https://localhost:8080`.

## Integration With the Other Repositories

- The frontend calls this service for search, reservations, queue, purchase, and ticket state changes.
- The user backend validates user tokens before the frontend associates reservations with an authenticated account.
- Reservation ownership depends on consistent token exchange between the frontend and the user backend.

## Related Repositories

- distributed-ticketing-DBsystem -> `https://github.com/SobrinoS29/distributed-ticketing-DBsystem`
- distributed-ticketing-UserBEsystem -> `https://github.com/SobrinoS29/distributed-ticketing-UserBEsystem`
- distributed-ticketing-FrontEnd -> `https://github.com/SobrinoS29/distributed-ticketing-FrontEnd`

## Security

This backend is part of a distributed system and follows several controls to reduce common risks:

1. HTTPS is enabled with locally trusted certificates generated with `mkcert`, so the service can be accessed securely during development.
2. Reservation and authentication tokens are handled server-side, and the frontend should never expose `userToken` values in routes or shared URLs.
3. Ticket reservations expire automatically after five minutes, and the cleanup flow releases orphaned seats through `ReservasController.cleanupExpiredReservations(ticketToken)` and `ReservasService.cleanupExpiredReservations(ticketToken)`.
4. `ticketTokenDao.deleteExpiredTokens(ticketToken)` removes expired reservation tokens, and `entradaDao.liberarEntradasHuerfanas()` restores orphaned seats to an available state.
5. Passwords are not stored in plain text; the authentication flow uses BCrypt in the user backend.
6. JPA parameter binding is used instead of raw string concatenation to reduce SQL injection risk.
7. Email verification and password reset flows use expiring tokens so the links cannot be reused indefinitely.
8. Sensitive data such as password hashes is stored in a dedicated table with a neutral name, which reduces information disclosure.
9. Registration success responses should remain generic to avoid revealing account existence.
10. Database triggers can assign roles based on approved email lists and keep update timestamps consistent.
11. The schema should avoid predictable public naming for dangerous tables or operations to reduce abuse from client-side dictionary attacks.

## License

Educational project. Images are used for educational purposes only.

## Author

Javier Sobrino Ocaña
