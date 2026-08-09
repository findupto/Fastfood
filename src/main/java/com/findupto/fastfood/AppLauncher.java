package com.findupto.fastfood;

import java.sql.*;

/** Starts the POS after migrating databases created by earlier versions. */
public final class AppLauncher {
    private AppLauncher() {}
    public static void main(String[] args) {
        migrate();
        CompletePOS.main(args);
    }
    private static void migrate() {
        try (Connection c=DriverManager.getConnection("jdbc:sqlite:fastfood.db"); Statement s=c.createStatement()) {
            if (tableExists(c,"users") && !columnExists(c,"users","active")) s.executeUpdate("ALTER TABLE users ADD COLUMN active INTEGER DEFAULT 1");
            if (tableExists(c,"sales") && !columnExists(c,"sales","status")) s.executeUpdate("ALTER TABLE sales ADD COLUMN status TEXT DEFAULT 'COMPLETED'");
            if (tableExists(c,"users")) s.executeUpdate("UPDATE users SET active=1 WHERE active IS NULL");
            if (tableExists(c,"sales")) s.executeUpdate("UPDATE sales SET status='COMPLETED' WHERE status IS NULL OR status='' ");
        } catch (SQLException e) {
            throw new RuntimeException("Database migration failed: "+e.getMessage(), e);
        }
    }
    private static boolean tableExists(Connection c,String table)throws SQLException {
        try (PreparedStatement p=c.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            p.setString(1,table); try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }
    private static boolean columnExists(Connection c,String table,String column)throws SQLException {
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery("PRAGMA table_info("+table+")")) {
            while(r.next()) if(column.equalsIgnoreCase(r.getString("name"))) return true;
            return false;
        }
    }
}
