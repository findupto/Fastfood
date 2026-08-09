package com.findupto.fastfood;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

/** Application entry point: initializes the operational database, authenticates the user and installs hardware services. */
public final class AppLauncher {
    private static final String DB="jdbc:sqlite:fastfood.db";
    private AppLauncher(){}
    public static void main(String[] args){
        try{ CompletePOS.init(); EnterpriseSchema.migrate(); initializePrinterSettings(); }
        catch(Exception e){ JOptionPane.showMessageDialog(null,e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE); return; }
        SwingUtilities.invokeLater(AppLauncher::login);
    }
    private static void initializePrinterSettings() throws SQLException {
        try(Connection c=DriverManager.getConnection(DB); PreparedStatement q=c.prepareStatement("INSERT OR IGNORE INTO settings(key,value) VALUES(?,?)")){
            String[][] d={{"printer_bluetooth_mac",""},{"printer_serial_port",""},{"printer_system_name",""},{"printer_auto_reconnect","true"},{"printer_width","80mm"},{"printer_baud","9600"}};
            for(String[] x:d){q.setString(1,x[0]);q.setString(2,x[1]);q.addBatch();} q.executeBatch();
        }
    }
    static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v));return x.toString();}catch(Exception e){throw new RuntimeException(e);}}
    private static void login(){
        try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
        JTextField u=new JTextField("admin"); JPasswordField p=new JPasswordField();
        u.setPreferredSize(new Dimension(220,30)); p.setPreferredSize(new Dimension(220,30));
        JPanel x=new JPanel(new GridBagLayout()); x.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,7,7,7); g.fill=GridBagConstraints.HORIZONTAL;
        g.gridx=0;g.gridy=0;x.add(new JLabel("Username"),g);g.gridx=1;x.add(u,g);g.gridx=0;g.gridy=1;x.add(new JLabel("Password"),g);g.gridx=1;x.add(p,g);
        JLabel title=new JLabel("MK Pizza & Ice Bar POS"); title.setFont(title.getFont().deriveFont(Font.BOLD,20f));
        JPanel wrapper=new JPanel(new BorderLayout(8,8)); wrapper.setBorder(BorderFactory.createEmptyBorder(8,8,8,8)); wrapper.add(title,BorderLayout.NORTH); wrapper.add(x,BorderLayout.CENTER);
        int ok=JOptionPane.showConfirmDialog(null,wrapper,"POS Login",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ok!=JOptionPane.OK_OPTION){System.exit(0);return;}
        String user=u.getText().trim(),pass=new String(p.getPassword());
        try(Connection c=DriverManager.getConnection(DB);PreparedStatement q=c.prepareStatement("SELECT role FROM users WHERE username=? AND password=? AND active=1")){
            q.setString(1,user);q.setString(2,hash(pass));
            try(ResultSet r=q.executeQuery()){if(r.next()){CompletePOS app=new CompletePOS(user,r.getString(1));PrinterService.install(app,user,r.getString(1));app.setVisible(true);return;}}
        }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Login Error",JOptionPane.ERROR_MESSAGE);return;}
        JOptionPane.showMessageDialog(null,"Invalid username or password","Login",JOptionPane.WARNING_MESSAGE); login();
    }
}
