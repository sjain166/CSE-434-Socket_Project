# Socket Programming Project for CSE 434

This repository contains the source code for a Socket Programming project developed as part of the CSE 434 course. The project implements a client-server architecture using Java, focusing on network programming concepts.

## Features

- **Client-Server Communication**: Implements TCP/IP socket communication between clients and a server.
- **Banking Application**: Simulates a simple banking system where clients can perform operations like checking balance, transferring funds, and viewing transaction history.
- **Multi-threading**: Handles multiple client connections concurrently.

## Technologies Used

- **Java**: Core programming language for implementing socket communication.
- **TCP/IP**: Protocol for reliable communication between client and server.
- **Multi-threading**: Ensures efficient handling of multiple clients.

## Project Structure

- `bank.java`: Server-side code to handle client requests.
- `client.java`: Client-side code to communicate with the server.
- `customer.java`: Contains classes related to customer operations.

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/sjain166/CSE-434-Socket_Project.git
   ```
2. Compile the Java files:
   ```bash
   javac *.java
   ```
3. Run the server:
   ```bash
   java bank
   ```
4. Run the client:
   ```bash
   java client
   ```

## Demonstration

You can view a demo of the project [here](https://youtu.be/l16VSTuCzM4?si=5AhWndgmPtyHedpq).

## Contributors

- **Siddharth Jain**
- **Kushal Paliwal**

---

Feel free to customize this README further based on your specific requirements!
