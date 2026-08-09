# Fastfood POS

A desktop Point of Sale (POS) application for a fast-food store, developed in Java 17 with Swing.

## Features

- Fast-food menu with categories and prices
- Add items to an order with quantity handling
- Remove individual items or clear the order
- Automatic subtotal, 5% tax, discount, and grand total calculation
- Cash payment validation and change calculation
- Receipt preview with date/time
- Receipt printing through Java's print support
- No external runtime dependencies

## Technology

- Java 17+
- Java Swing
- Maven

## Run

```bash
mvn clean compile
mvn exec:java
```

Or run the main class directly from an IDE:

`com.findupto.fastfood.FastfoodPOS`

## Project Structure

```text
Fastfood/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/findupto/fastfood/
                └── FastfoodPOS.java
```

## Default Menu

| Code | Item | Price (PKR) |
|---|---|---:|
| B01 | Zinger Burger | 450 |
| B02 | Chicken Burger | 380 |
| B03 | Beef Burger | 520 |
| F01 | Chicken Fries | 300 |
| F02 | Loaded Fries | 420 |
| P01 | Chicken Pizza | 850 |
| P02 | Pepperoni Pizza | 950 |
| D01 | Cold Drink | 120 |
| D02 | Fresh Lemonade | 180 |
| D03 | Mineral Water | 80 |

## Notes

This is a clean starter POS suitable for an academic project or small store prototype. Product management, inventory persistence, user login, database storage, sales reports, barcode scanning, and customer management can be added as the next development phase.
