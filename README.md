# MantenimientodeUsuario

A Java desktop application for employee management, built with Ant.

## Features
- Employee registration and management
- Login system
- Employee data stored in text files

## Requirements
- Java 8 or higher
- Ant

## Build & Run

1. **Build the project:**
   ```bash
   ant
   ```
2. **Run the application:**
   ```bash
   ant run
   ```
   Or, if no run target is defined:
   ```bash
   java -cp build/classes archivo.Login
   ```

## Project Structure
- `src/archivo/` - Source code
- `build/` - Compiled classes
- `Empleadosguardados.txt` - Employee data

## Notes
- All employee data is stored in plain text files.
- GUI forms are managed in `.form` files.

---
Feel free to contribute or modify as needed!

