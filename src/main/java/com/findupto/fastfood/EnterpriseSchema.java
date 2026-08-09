package com.findupto.fastfood;

import java.sql.*;

/** Central, idempotent schema for the enterprise POS. */
public final class EnterpriseSchema {
    private EnterpriseSchema() {}
    public static final String DB = "jdbc:sqlite:fastfood.db";

    public static void migrate() throws SQLException {
        try (Connection c = DriverManager.getConnection(DB); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA journal_mode=WAL");
            s.execute("PRAGMA busy_timeout=5000");
            String[] ddl = {
                "CREATE TABLE IF NOT EXISTS categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL,active INTEGER NOT NULL DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS product_images(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,path TEXT NOT NULL,sort_order INTEGER NOT NULL DEFAULT 0)",
                "CREATE TABLE IF NOT EXISTS customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,email TEXT,address TEXT,opening_balance REAL NOT NULL DEFAULT 0,active INTEGER NOT NULL DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS customer_ledger(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL REFERENCES customers(id),at TEXT NOT NULL,type TEXT NOT NULL,ref TEXT,amount REAL NOT NULL,note TEXT,user TEXT)",
                "CREATE TABLE IF NOT EXISTS suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,email TEXT,address TEXT,opening_balance REAL NOT NULL DEFAULT 0,active INTEGER NOT NULL DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS supplier_ledger(id INTEGER PRIMARY KEY AUTOINCREMENT,supplier_id INTEGER NOT NULL REFERENCES suppliers(id),at TEXT NOT NULL,type TEXT NOT NULL,ref TEXT,amount REAL NOT NULL,note TEXT,user TEXT)",
                "CREATE TABLE IF NOT EXISTS expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,category TEXT NOT NULL,amount REAL NOT NULL,note TEXT,user TEXT)",
                "CREATE TABLE IF NOT EXISTS expense_categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL,active INTEGER NOT NULL DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS cash_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,business_date TEXT UNIQUE NOT NULL,opened_at TEXT NOT NULL,opening_cash REAL NOT NULL DEFAULT 0,closing_cash REAL,closed_at TEXT,status TEXT NOT NULL DEFAULT 'OPEN',user TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS cash_transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,type TEXT NOT NULL,amount REAL NOT NULL,note TEXT,user TEXT NOT NULL,reference TEXT)",
                "CREATE TABLE IF NOT EXISTS stock_movements(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER NOT NULL REFERENCES products(id),at TEXT NOT NULL,type TEXT NOT NULL,qty INTEGER NOT NULL,cost REAL NOT NULL DEFAULT 0,note TEXT,user TEXT)",
                "CREATE TABLE IF NOT EXISTS purchases(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,supplier_id INTEGER REFERENCES suppliers(id),total REAL NOT NULL,paid REAL NOT NULL DEFAULT 0,status TEXT NOT NULL,user TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS purchase_items(id INTEGER PRIMARY KEY AUTOINCREMENT,purchase_id INTEGER NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,product_id INTEGER NOT NULL REFERENCES products(id),qty INTEGER NOT NULL,cost REAL NOT NULL,amount REAL NOT NULL)",
                "CREATE TABLE IF NOT EXISTS payments(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,direction TEXT NOT NULL,method TEXT NOT NULL,amount REAL NOT NULL,customer_id INTEGER REFERENCES customers(id),supplier_id INTEGER REFERENCES suppliers(id),sale_id INTEGER REFERENCES sales(id),note TEXT,user TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS refunds(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,sale_id INTEGER NOT NULL REFERENCES sales(id),amount REAL NOT NULL,reason TEXT,user TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS refund_items(id INTEGER PRIMARY KEY AUTOINCREMENT,refund_id INTEGER NOT NULL REFERENCES refunds(id) ON DELETE CASCADE,product_id INTEGER,qty INTEGER NOT NULL,amount REAL NOT NULL)",
                "CREATE TABLE IF NOT EXISTS shifts(id INTEGER PRIMARY KEY AUTOINCREMENT,user TEXT NOT NULL,opened_at TEXT NOT NULL,closed_at TEXT,status TEXT NOT NULL DEFAULT 'OPEN',opening_cash REAL NOT NULL DEFAULT 0,closing_cash REAL)",
                "CREATE TABLE IF NOT EXISTS recipes(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,ingredient_name TEXT NOT NULL,qty REAL NOT NULL,unit TEXT NOT NULL,cost REAL NOT NULL DEFAULT 0)",
                "CREATE TABLE IF NOT EXISTS order_types(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL,active INTEGER NOT NULL DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS printer_settings(id INTEGER PRIMARY KEY CHECK(id=1),name TEXT,mac TEXT,connection_type TEXT NOT NULL DEFAULT 'SYSTEM',paper_width INTEGER NOT NULL DEFAULT 80,auto_reconnect INTEGER NOT NULL DEFAULT 1,last_connected_at TEXT)",
                "CREATE TABLE IF NOT EXISTS audit_log(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,user TEXT NOT NULL,action TEXT NOT NULL,details TEXT)",
                "CREATE TABLE IF NOT EXISTS backups(id INTEGER PRIMARY KEY AUTOINCREMENT,at TEXT NOT NULL,path TEXT NOT NULL,user TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS app_meta(key TEXT PRIMARY KEY,value TEXT NOT NULL)",
                "CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sold_at)",
                "CREATE INDEX IF NOT EXISTS idx_sales_cashier ON sales(cashier)",
                "CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items(product_id)",
                "CREATE INDEX IF NOT EXISTS idx_stock_product_date ON stock_movements(product_id,at)",
                "CREATE INDEX IF NOT EXISTS idx_customer_ledger ON customer_ledger(customer_id,at)",
                "CREATE INDEX IF NOT EXISTS idx_supplier_ledger ON supplier_ledger(supplier_id,at)",
                "CREATE INDEX IF NOT EXISTS idx_cash_transactions_date ON cash_transactions(at)",
                "CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(at)"
            };
            for (String sql : ddl) s.execute(sql);
            defaultSetting(c, "business_name", "MK Pizza & Ice Bar");
            defaultSetting(c, "business_address", "Collage Road Abbas Chowk, Bhakkar, Pakistan");
            defaultSetting(c, "business_phone", "0316 9700025");
            defaultSetting(c, "currency", "Rs.");
            defaultSetting(c, "tax_rate", "0");
            defaultSetting(c, "business_day_start", "06:00");
            defaultSetting(c, "printer_width", "80mm");
            defaultSetting(c, "printer_auto_reconnect", "true");
            defaultSetting(c, "printer_bluetooth_mac", "");
            defaultSetting(c, "receipt_footer", "Thank you! Visit again.");
            defaultCategory(c, "Burgers"); defaultCategory(c, "Pizza"); defaultCategory(c, "Fries"); defaultCategory(c, "Drinks");
            defaultOrderType(c, "Dine In"); defaultOrderType(c, "Takeaway"); defaultOrderType(c, "Delivery");
        }
    }
    private static void defaultSetting(Connection c,String k,String v)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO settings(key,value) VALUES(?,?)")){p.setString(1,k);p.setString(2,v);p.executeUpdate();}}
    private static void defaultCategory(Connection c,String n)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO categories(name) VALUES(?)")){p.setString(1,n);p.executeUpdate();}}
    private static void defaultOrderType(Connection c,String n)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO order_types(name) VALUES(?)")){p.setString(1,n);p.executeUpdate();}}
}
