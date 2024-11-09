# VehicleRent - System wypożyczalni samochodów

## Opis projektu

**VehicleRent** to aplikacja stworzona w języku Java, która umożliwia zarządzanie wypożyczalnią samochodów. System pozwala na:
- Rejestrowanie i zarządzanie samochodami w bazie danych.
- Wynajmowanie i zwracanie samochodów przez klientów.
- Przechowywanie informacji o klientach i wypożyczeniach.
- Umożliwienie adminowi dodawania i usuwania samochodów z bazy danych.
- Zarządzanie dostępnością samochodów w zależności od wynajmu.

Aplikacja korzysta z bazy danych MySQL do przechowywania danych o samochodach, klientach i wypożyczeniach.

## Funkcjonalności

1. **Zarządzanie samochodami (dla admina)**:
   - Dodawanie nowych samochodów do bazy danych.
   - Usuwanie samochodów z bazy danych.
   - Sprawdzanie dostępnych samochodów.
   
2. **Wynajem samochodów (dla klientów)**:
   - Rejestracja klienta na podstawie adresu e-mail.
   - Wybór dostępnego samochodu do wynajmu.
   - Zwracanie wypożyczonego samochodu.

3. **Zarządzanie wypożyczeniami**:
   - Przechowywanie informacji o dacie wynajmu i dacie zwrotu samochodu.
   - Weryfikowanie dostępności samochodów przed wynajmem.

4. **Bezpieczeństwo danych**:
   - Walidacja formatu e-maila wprowadzanych przez klientów.

## Wymagania

Aby uruchomić aplikację, będziesz potrzebować:

- Java 8 lub wyższa
- MySQL Server
- Środowisko IDE (np. IntelliJ IDEA, Eclipse)
- JDK (Java Development Kit)

## Instalacja

1. **Skonfiguruj MySQL**:
   - Upewnij się, że masz zainstalowany MySQL Server.
   - Stwórz bazę danych `car_rental`:
     ```sql
     CREATE DATABASE IF NOT EXISTS car_rental;
     USE car_rental;
     ```
   - Zaimportuj struktury tabel i dane (jak w pliku SQL w projekcie).

2. **Skonfiguruj połączenie z bazą danych**:
   - W pliku `Database.java` wprowadź dane do połączenia z Twoją bazą danych MySQL:
     ```java
     private String url = "jdbc:mysql://localhost:3306/car_rental";
     private String user = "root";  // Twoja nazwa użytkownika MySQL
     private String password = "password";  // Twoje hasło MySQL
     ```

3. **Uruchom aplikację**:
   - Skorzystaj z klasy `Main.java` do uruchomienia aplikacji w terminalu lub IDE.
   - Wybierz, czy chcesz działać jako klient czy admin, a następnie wykonuj dostępne operacje.

## Jak korzystać

1. **Wybór roli**:
   - Po uruchomieniu aplikacji wybierz, czy chcesz działać jako klient czy admin.
   
2. **Dla admina**:
   - Jako admin masz dostęp do opcji takich jak dodawanie nowych samochodów, usuwanie samochodów oraz przeglądanie aktywnych wypożyczeń.

3. **Dla klienta**:
   - Możesz wynająć dostępny samochód, podając swoje dane (imię, nazwisko, email) lub zwrócić wypożyczony samochód.

4. **Zarządzanie samochodami**:
   - Sprawdź dostępność samochodów i wybierz jeden z nich do wynajmu.
   - Możesz także dodać nowe pojazdy jako admin.

## Przykładowe zapytania SQL

**Dodawanie samochodów**:
```sql
INSERT INTO cars (brand, model, licensePlate, numberOfDoors, seats, isAvailable) 
VALUES ('Toyota', 'Corolla', 'ABC123', 4, 5, TRUE);
