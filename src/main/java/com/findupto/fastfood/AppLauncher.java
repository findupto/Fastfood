package com.findupto.fastfood;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

/** Entry point for the enterprise POS and database migration/login. */
public final class AppLauncher {
    private static final String DB="jdbc:sqlite:fastfood.db";
    private AppLauncher() {}
    public static void main(String[] args){
        try { EnterpriseSchema.migrate(); seedCore(); }
        catch(Exception e){ JOptionPane.showMessageDialog(null,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE); return; }
        SwingUtilities.invokeLater(AppLauncher::login);
    }
    private static void seedCore() throws SQLException {
        try(Connection c=DriverManager.getConnection(DB)){
            user(c,"admin","0099","ADMIN");
            user(c,"owner","0099","OWNER");
            if(!columnExists(c,"products","cost_price"))try(Statement s=c.createStatement()){s.execute("ALTER TABLE products ADD COLUMN cost_price REAL DEFAULT 0");}
        }
    }
    static void user(Connection c,String u,String p,String r)throws SQLException{try(PreparedStatement q=c.prepareStatement("INSERT INTO users(username,password,role,active) VALUES(?,?,?,1) ON CONFLICT(username) DO UPDATE SET password=excluded.password,role=excluded.role,active=1")){q.setString(1,u);q.setString(2,hash(p));q.setString(3,r);q.executeUpdate();}}
    static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v));return x.toString();}catch(Exception e){throw new RuntimeException(e);}}
    static boolean columnExists(Connection c,String table,String col)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery("PRAGMA table_info("+table+")")){while(r.next())if(col.equalsIgnoreCase(r.getString("name")))return true;return false;}}
    static void login(){
        JTextField u=new JTextField("admin"); JPasswordField p=new JPasswordField();
        JPanel x=new JPanel(new GridLayout(0,2,6,6)); x.add(new JLabel("Username"));x.add(u);x.add(new JLabel("Password"));x.add(p);
        int ok=JOptionPane.showConfirmDialog(null,x,"MK Pizza & Ice Bar Login",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ok!=JOptionPane.OK_OPTION){System.exit(0);return;}
        String user=u.getText().trim(),pass=p.getText();
        try(Connection c=DriverManager.getConnection(DB);PreparedStatement q=c.prepareStatement("SELECT role FROM users WHERE username=? AND password=? AND active=1")){
            q.setString(1,user);q.setString(2,hash(pass));
            try(ResultSet r=q.executeQuery()){if(r.next()){EnterprisePOS e=new EnterprisePOS(user,r.getString(1));e.setVisible(true);return;}}
        }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Login Error",JOptionPane.ERROR_MESSAGE);return;}
        JOptionPane.showMessageDialog(null,"Invalid username or password","Login",JOptionPane.WARNING_MESSAGE);login();
    }
}
