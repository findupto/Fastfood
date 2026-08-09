# MK Pizza & Ice Bar POS

Complete Java 17 desktop Point of Sale system for **MK Pizza & Ice Bar**, Collage Road Abbas Chowk, Bhakkar, Pakistan.

## Business Defaults

- Business: **MK Pizza & Ice Bar**
- Address: **Collage Road Abbas Chowk, Bhakkar, Pakistan**
- Phone: **0316 9700025**
- Currency: **Rs.**
- Tax: **0%**
- Printer: configure Bluetooth MAC in **Settings**

## Default Users

| Username | Role | Password |
|---|---|---|
| `admin` | Admin | `0099` |
| `owner` | Owner | `0099` |

Passwords are stored as SHA-256 hashes in the completed POS database.

## Completed POS Features

- Secure login with Admin and Owner roles
- Product/menu management with categories, prices and stock
- Add/update/deactivate/reactivate products
- Live stock validation at checkout
- Cart quantity editing and removal
- Percentage discounts
- Configurable tax (0% default)
- Cash payment and automatic change
- Persistent SQLite sales database
- Unique receipt numbers
- Business-branded receipt generation and printing
- Sales history with receipt search
- Full-sale refund with automatic stock restoration
- Refund status protection against duplicate refunds
- Daily sales dashboard
- Top-selling products
- Low-stock report
- User management and account disabling
- Business/tax/currency/receipt/printer settings
- SQLite database backup
- Audit log for sales and refunds
- Transaction-safe checkout and refund operations

## Run

```bash
mvn clean compile
mvn exec:java
```

Main class: `com.findupto.fastfood.CompletePOS`

## Technology

- Java 17+
- Java Swing
- Maven
- SQLite via Xerial JDBC

## Default Menu

| Code | Item | Category | Price (Rs.) | Initial Stock |
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

## Printer

Settings stores the Bluetooth printer MAC address. Java's standard desktop printing opens the operating-system printer dialog. Direct Bluetooth ESC/POS printing remains platform/device-specific and is not falsely claimed as implemented.

## Database

The application creates/migrates these tables automatically:

- `users`
- `products`
- `sales`
- `sale_items`
- `settings`
- `audit_log`

The database file is `fastfood.db` in the application's working directory. Admin/Owner can create a backup from **Backup**.
