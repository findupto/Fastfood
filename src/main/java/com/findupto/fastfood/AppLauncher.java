package com.findupto.fastfood;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.*;

/** Entry point for the enterprise POS and database migration/login. */
public final class AppLauncher {
    private static final String DB="jdbc:sqlite:fastfood.db";
    private AppLauncher() {}
    public static void main(String[] args){try{migrate();}catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);return;}SwingUtilities.invokeLater(AppLauncher::login);}
    static void migrate() throws SQLException{try(Connection c=DriverManager.getConnection(DB);Statement s=c.createStatement()){
        s.execute("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE,password TEXT,role TEXT,active INTEGER DEFAULT 1)");s.execute("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT UNIQUE,name TEXT,category TEXT,price REAL,stock INTEGER,active INTEGER DEFAULT 1)");s.execute("CREATE TABLE IF NOT EXISTS sales(id INTEGER PRIMARY KEY AUTOINCREMENT,receipt_no TEXT UNIQUE,sold_at TEXT,total REAL,discount REAL,tax REAL,cash REAL,change_amount REAL,payment TEXT,cashier TEXT,status TEXT DEFAULT 'COMPLETED')");s.execute("CREATE TABLE IF NOT EXISTS sale_items(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER,product_id INTEGER,product_name TEXT,qty INTEGER,price REAL,amount REAL)");s.execute("CREATE TABLE IF NOT EXISTS settings(key TEXT PRIMARY KEY,value TEXT NOT NULL)");
        if(!columnExists(c,"products","cost_price"))s.execute("ALTER TABLE products ADD COLUMN cost_price REAL DEFAULT 0");if(!columnExists(c,"users","active"))s.execute("ALTER TABLE users ADD COLUMN active INTEGER DEFAULT 1");if(!columnExists(c,"sales","status"))s.execute("ALTER TABLE sales ADD COLUMN status TEXT DEFAULT 'COMPLETED'");
        defaultSetting(c,"business_name","MK Pizza & Ice Bar");defaultSetting(c,"business_address","Collage Road Abbas Chowk, Bhakkar, Pakistan");defaultSetting(c,"business_phone","0316 9700025");defaultSetting(c,"currency","Rs.");defaultSetting(c,"tax_rate","0");defaultSetting(c,"receipt_footer","Thank you! Visit again.");defaultSetting(c,"business_day_start","06:00");defaultSetting(c,"printer_auto_reconnect","true");user(c,"admin","0099","ADMIN");user(c,"owner","0099","OWNER");
    }}
    static void defaultSetting(Connection c,String k,String v)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO settings(key,value) VALUES(?,?)")){p.setString(1,k);p.setString(2,v);p.executeUpdate();}}
    static void user(Connection c,String u,String p,String r)throws SQLException{try(PreparedStatement q=c.prepareStatement("INSERT INTO users(username,password,role,active) VALUES(?,?,?,1) ON CONFLICT(username) DO UPDATE SET password=excluded.password,role=excluded.role,active=1")){q.setString(1,u);q.setString(2,hash(p));q.setString(3,r);q.executeUpdate();}}
    static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v));return x.toString();}catch(Exception e){throw new RuntimeException(e);}}
    static boolean columnExists(Connection c,String table,String col)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery("PRAGMA table_info("+table+")")){while(r.next())if(col.equalsIgnoreCase(r.getString("name")))return true;return false;}}
    static void login(){JTextField u=new JTextField("admin"),p=new JPasswordField();JPanel x=new JPanel(new GridLayout(0,2,6,6));x.add(new JLabel("Username"));x.add(u);x.add(new JLabel("Password"));x.add(p);int ok=JOptionPane.showConfirmDialog(null,x,"MK Pizza & Ice Bar Login",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(ok!=JOptionPane.OK_OPTION){System.exit(0);return;}String user=u.getText().trim(),pass=p.getText();try(Connection c=DriverManager.getConnection(DB);PreparedStatement q=c.prepareStatement("SELECT role FROM users WHERE username=? AND password=? AND active=1")){q.setString(1,user);q.setString(2,hash(pass));try(ResultSet r=q.executeQuery()){if(r.next()){EnterprisePOS e=new EnterprisePOS(user,r.getString(1));e.tabs.addTab("Purchases",new PurchasesPanel(user));e.setVisible(true);return;}}}catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage());}JOptionPane.showMessageDialog(null,"Invalid username or password");login();}
}
