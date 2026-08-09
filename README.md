# MK Pizza & Ice Bar POS

Java 17 desktop Point of Sale system for **MK Pizza & Ice Bar**, Collage Road Abbas Chowk, Bhakkar, Pakistan.

## Business Defaults

- **Business:** MK Pizza & Ice Bar
- **Address:** Collage Road Abbas Chowk, Bhakkar, Pakistan
- **Phone:** 0316 9700025
- **Currency:** Rs.
- **Tax:** 0%
- **Printer:** Bluetooth MAC can be configured from **Settings**

## Features

- Login with Admin and Owner roles
- SQLite database created automatically as `fastfood.db`
- Fast-food menu with live stock
- Cart and quantity handling
- 0% tax by default
- Percentage discount
- Cash payment and change calculation
- Persistent sales and sale items
- Unique receipt numbers
- Printable receipts containing business information
- Product add/update and stock management
- Product deactivation
- Daily sales dashboard
- Recent sales
- Top-selling products
- Low-stock report
- Settings for business details, currency, tax, receipt footer and Bluetooth printer MAC
- Transaction-safe checkout and stock deduction

## Default Users

| Username | Role | Password |
|---|---|---|
| `admin` | Admin | `0099` |
| `owner` | Owner | `0099` |

## Run

```bash
mvn clean compile
mvn exec:java
```

Main class: `com.findupto.fastfood.FastfoodPOS`

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

Open **Settings** after logging in as Admin or Owner and enter the Bluetooth printer MAC address. The MAC is persisted as the POS printer configuration. Java desktop printing uses the printer selected by the operating system's print dialog; direct Bluetooth printing requires a platform-specific Bluetooth/ESC-POS integration.

## Database Tables

- `users`
- `products`
- `sales`
- `sale_items`
- `settings`

The application initializes the business defaults and credentials on startup. Existing `admin` and `owner` credentials are synchronized to `0099`.
