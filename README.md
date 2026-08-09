# MK Pizza & Ice Bar POS

Java 17 + SQLite desktop POS for **MK Pizza & Ice Bar**, Collage Road Abbas Chowk, Bhakkar, Pakistan.

## Business defaults
- Business: MK Pizza & Ice Bar
- Address: Collage Road Abbas Chowk, Bhakkar, Pakistan
- Phone: 0316 9700025
- Currency: Rs.
- Tax: 0%
- Business day: 06:00 by default, configurable
- Receipt: 80mm thermal layout

## Login
- `admin / 0099` — Admin
- `owner / 0099` — Owner

## POS features
- Cash checkout with change calculation
- Discount and configurable tax
- Stock validation inside the checkout transaction
- Receipt numbers and sales history
- Refund with stock restoration
- Product/menu management
- Bulk CSV menu import/export
- Bulk product image linking by product code
- Customers and customer ledger
- Suppliers and supplier ledger
- Purchases/stock-in foundation
- Expenses
- Opening/closing cash and business-day sessions
- Profit/Loss and cash-flow reporting
- Backup
- Audit log
- Role-protected administration

## 80mm thermal printer
The POS now includes a dedicated **Printer** menu with:

- Live Java serial/Bluetooth-SPP port discovery
- Saved COM/RFCOMM port
- Bluetooth MAC storage
- Auto-reconnect on POS launch
- Connect/reconnect command
- Connection status
- 80mm ESC/POS test receipt
- Raw ESC/POS printing through an RFCOMM/SPP serial port
- Operating-system printer fallback
- System printer selection
- Admin/Owner printer settings

### Recommended Bluetooth setup
1. Pair the 80mm printer with Windows/Linux first.
2. Ensure the OS exposes its Bluetooth SPP/RFCOMM connection as a COM/serial port.
3. Start the POS and open **🖨 Printer → Discover Printers**.
4. Select the printer COM/RFCOMM port.
5. Open **🖨 Printer → Printer Settings** and save the Bluetooth MAC and port.
6. Keep **Auto reconnect** enabled.
7. Use **Print 80mm Test Receipt** before taking sales.

The application uses **jSerialComm** for the serial/RFCOMM connection and sends ESC/POS bytes directly. This is much more reliable for supported Bluetooth SPP thermal printers than Java's generic page printer for receipt formatting.

### Hardware limitation
Bluetooth pairing, RFCOMM/COM exposure, permissions and printer firmware are controlled by the operating system and printer model. A Bluetooth printer that does not expose an SPP/RFCOMM serial service cannot be made into a serial ESC/POS device by Java alone. For those models, install the manufacturer's Windows printer driver and use the system-printer fallback.

## Bulk menu
CSV template:

```csv
code,name,category,cost_price,selling_price,stock,image
B01,Zinger Burger,Burgers,300,450,50,B01.jpg
B02,Chicken Burger,Burgers,250,380,50,B02.jpg
```

Images can be placed in one folder and named using the product code, for example `B01.jpg`, `B02.png`, `P01.webp`.

## Run

```bash
mvn clean compile
mvn exec:java
```

Main class: `com.findupto.fastfood.AppLauncher`

## Database
The application uses `fastfood.db`. Database migrations preserve existing settings/data and create missing tables/settings automatically.

## Accounting
Profit/Loss depends on accurate product cost prices and recorded purchases/stock-in. Review costs regularly when supplier prices change.
