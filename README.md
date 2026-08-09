# Fastfood POS

A Java 17 desktop Point of Sale system for a fast-food store. Version 2 adds persistent SQLite storage, authentication, inventory, product management, sales history and a management dashboard.

## Features

- Login with roles (`ADMIN` and `CASHIER`)
- SQLite database created automatically as `fastfood.db`
- Fast-food menu with categories, prices and live stock
- Cart quantity handling and stock validation
- 5% tax and configurable discount percentage
- Cash payment and automatic change calculation
- Persistent sales and sale-item records
- Unique receipt numbers and printable receipts
- Product add/update and stock management
- Product deactivation
- Daily sales totals and average order value
- Recent sales report
- Top-selling items report
- Low-stock report
- Transaction-safe checkout: sale and stock deduction commit together

## Technology

- Java 17+
- Java Swing
- Maven
- SQLite via Xerial JDBC

## Run

```bash
mvn clean compile
mvn exec:java
```

Main class:

`com.findupto.fastfood.FastfoodPOS`

## Default Login

| Username | Password | Role |
|---|---|---|
| admin | admin | ADMIN |
| cashier | cashier | CASHIER |

For a real deployment, replace the demo credentials with secure password hashing and proper role-based permissions.

## Default Menu

| Code | Item | Category | Price (PKR) | Initial Stock |
|---|---|---|---:|---:|
| B01 | Zinger Burger | Burgers | 450 | 50 |
| B02 | Chicken Burger | Burgers | 380 | 50 |
| B03 | Beef Burger | Burgers | 520 | 30 |
| F01 | Chicken Fries | Fries | 300 | 60 |
| F02 | Loaded Fries | Fries | 420 | 40 |
| P01 | Chicken Pizza | Pizza | 850 | 25 |
| P02 | Pepperoni Pizza | Pizza | 950 | 25 |
| D01 | Cold Drink | Drinks | 120 | 100 |
| D02 | Fresh Lemonade | Drinks | 180 | 80 |
| D03 | Mineral Water | Drinks | 80 | 100 |

## Project Structure

```text
Fastfood/
├── pom.xml
├── README.md
└── src/main/java/com/findupto/fastfood/FastfoodPOS.java
```

## Database

The application creates these tables automatically:

- `users`
- `products`
- `sales`
- `sale_items`

The SQLite file is created in the application's working directory.

## Important

This version is a strong academic/small-store POS foundation. For production use, the next upgrades should include password hashing, proper role permissions, card/mobile-wallet payment integration, barcode scanning, customer/loyalty management, returns/refunds, database backup/restore, audit logs and automated tests.
