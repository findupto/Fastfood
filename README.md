# MK Pizza & Ice Bar POS

Java 17 + SQLite desktop POS/management system for **MK Pizza & Ice Bar**, Collage Road Abbas Chowk, Bhakkar, Pakistan.

## Business defaults
- Business: MK Pizza & Ice Bar
- Address: Collage Road Abbas Chowk, Bhakkar, Pakistan
- Phone: 0316 9700025
- Currency: Rs.
- Tax: 0%
- Business day starts: 06:00 (configurable)
- Printer: 80mm profile; Bluetooth MAC is stored in Settings

## Login
- `admin / 0099` — Admin
- `owner / 0099` — Owner

## Modules
- Dashboard
- POS foundation / existing sales database
- Products & Menu
- Product cost price, selling price and stock
- Product history
- Bulk CSV menu import/export
- Bulk product-image linking by filename/code
- Customers and customer ledger
- Customer dues / payments / advances
- Suppliers and supplier ledger
- Supplier purchases / stock-in
- Expenses
- Opening cash and business-day sessions
- Cash in / cash out
- End-of-day cash reconciliation
- Profit & Loss: revenue, COGS, gross profit, expenses, net profit/loss
- Cash-flow summary
- Printer detection for OS printers
- 80mm print test
- Persisted Bluetooth printer MAC and auto-reconnect preference
- Business Settings
- Existing SQLite database migration

## Bulk menu
Use **Products / Menu → Bulk Import CSV**. Template:

```csv
code,name,category,cost_price,selling_price,stock,image
B01,Zinger Burger,Burgers,300,450,50,B01.jpg
B02,Chicken Burger,Burgers,250,380,50,B02.jpg
```

Use **Bulk Export CSV** to download the current menu. Use **Bulk Images** and select a folder where filenames match product codes, for example `B01.jpg`, `B02.png`, `P01.webp`.

## Run

```bash
mvn clean compile
mvn exec:java
```

Main class: `com.findupto.fastfood.AppLauncher`

## Database
The application uses `fastfood.db`. Newer modules are migrated automatically without replacing existing settings. Existing business settings are preserved; defaults are only inserted when a setting does not already exist.

## Printer note
The application can detect operating-system printers and persist the Bluetooth MAC/reconnect preference. Universal automatic Bluetooth discovery and direct ESC/POS connection cannot be guaranteed by standard Java alone because Windows/Linux Bluetooth permissions, drivers, printer profiles and ESC/POS implementations differ. The printer module deliberately does not fake a connection; a platform-specific Bluetooth/ESC-POS adapter is the remaining hardware integration step.

## Important accounting note
Profit/Loss uses product `cost_price` for COGS. For reliable profit, maintain accurate product costs and record stock-in/purchases through the Purchases module.
