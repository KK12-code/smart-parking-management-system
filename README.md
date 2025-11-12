# Smart Parking Management System 
Java-based OOP project demonstrating object-oriented design, file persistence, and CLI-based interaction.

## Features
- Abstract Vehicle hierarchy (Car, Bike, Truck)
- Thread-safe ParkingLot (Singleton pattern)
- Ticket & Payment modules
- File persistence for state and logs
- Config-based lot sizing
- Mini test utilities

## How to Run
javac -d out $(find src/main/java -name "*.java")
java -cp out com.parking.Main

## Skills Demonstrated
- Encapsulation, Inheritance, Polymorphism
- Exception Handling & File I/O
- Design Patterns (Singleton, Factory)
- Input Validation & CLI UX
