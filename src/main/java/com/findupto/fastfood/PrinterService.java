package com.findupto.fastfood;

import com.fazecast.jSerialComm.SerialPort;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 80mm ESC/POS printer manager.
 *
 * Bluetooth thermal printers that expose an RFCOMM/SPP serial port are handled
 * through jSerialComm. Windows/Linux printers exposed to Java Print Service are
 * handled as normal printers. The selected connection is persisted and retried
 * when the POS starts.
 */
public final class PrinterService {
    private static final String DB = "jdbc:sqlite:fastfood.db";
    private static final String SET_PORT = "printer_serial_port";
    private static final String SET_NAME = "printer_system_name";
    private static final String SET_MAC = "printer_bluetooth_mac";
    private static final String SET_AUTO = "printer_auto_reconnect";
    private static final int BAUD = 9600;
    private static SerialPort serial;

    private PrinterService() {}

    public static void install(JFrame frame, String user, String role) {
        JMenuBar bar = frame.getJMenuBar();
        if (bar == null) { bar = new JMenuBar(); frame.setJMenuBar(bar); }
        JMenu menu = new JMenu("🖨 Printer");
        JMenuItem discover = new JMenuItem("Discover Printers");
        JMenuItem connect = new JMenuItem("Connect Saved Bluetooth Printer");
        JMenuItem test = new JMenuItem("Print 80mm Test Receipt");
        JMenuItem system = new JMenuItem("Select System Printer");
        JMenuItem status = new JMenuItem("Printer Status");
        JMenuItem settings = new JMenuItem("Printer Settings");
        discover.addActionListener(e -> discoverDialog(frame));
        connect.addActionListener(e -> { boolean ok = reconnect(); message(frame, ok ? "Bluetooth/serial printer connected." : "Could not connect to the saved printer."); });
        test.addActionListener(e -> testPrint(frame));
        system.addActionListener(e -> selectSystemPrinter(frame));
        status.addActionListener(e -> message(frame, statusText()));
        settings.addActionListener(e -> settingsDialog(frame, role));
        menu.add(discover); menu.add(connect); menu.add(test); menu.add(system); menu.addSeparator(); menu.add(status); menu.add(settings);
        bar.add(menu); frame.setJMenuBar(bar);
        if (Boolean.parseBoolean(setting(SET_AUTO, "true"))) SwingUtilities.invokeLater(PrinterService::reconnect);
    }

    private static void discoverDialog(JFrame owner) {
        List<SerialPort> ports = List.of(SerialPort.getCommPorts());
        List<String> choices = new ArrayList<>();
        for (SerialPort p : ports) choices.add(p.getSystemPortName() + " — " + p.getDescriptivePortName());
        if (choices.isEmpty()) choices.add("No serial/Bluetooth SPP ports detected");
        Object pick = JOptionPane.showInputDialog(owner, "Available Bluetooth/serial ports:", "Printer Discovery", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0));
        if (pick == null || choices.get(0).startsWith("No ")) return;
        String port = choices.get(choices.indexOf(pick)).split(" ")[0];
        save(SET_PORT, port);
        message(owner, "Saved printer port: " + port + "\nUse Connect to test it.");
    }

    private static boolean reconnect() {
        closeSerial();
        String portName = setting(SET_PORT, "").trim();
        if (portName.isEmpty()) return false;
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getSystemPortName().equalsIgnoreCase(portName)) {
                p.setComPortParameters(BAUD, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 3000);
                if (p.openPort()) { serial = p; return true; }
            }
        }
        return false;
    }

    private static void testPrint(JFrame owner) {
        String text = "========================================\n" +
                center(setting("business_name", "MK Pizza & Ice Bar"), 40) + "\n" +
                center("80mm THERMAL PRINTER TEST", 40) + "\n" +
                "----------------------------------------\n" +
                "Connection: " + statusText() + "\n" +
                "Paper: 80mm / 58-64 chars\n" +
                "----------------------------------------\n" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
                "0123456789  Rs. 1,234.00\n" +
                "----------------------------------------\n" +
                "Printer integration OK\n\n\n";
        try {
            if (serial == null && !reconnect()) throw new IllegalStateException("No Bluetooth/serial printer connected");
            raw(text);
            raw(new String(new byte[]{0x1d, 0x56, 0x00}, StandardCharsets.ISO_8859_1));
            message(owner, "Test receipt sent to the thermal printer.");
        } catch (Exception e) {
            try { systemPrint(text); message(owner, "Bluetooth failed. Sent test through the selected system printer."); }
            catch (Exception x) { message(owner, "Printer test failed: " + x.getMessage()); }
        }
    }

    /** Send a plain-text receipt to the saved 80mm ESC/POS serial connection. */
    public static boolean printReceipt(String receipt) {
        try {
            if (serial == null && !reconnect()) return false;
            raw("\u001b@" + receipt + "\n\n");
            return true;
        } catch (Exception e) { closeSerial(); return false; }
    }

    private static void raw(String text) throws Exception { serial.writeBytes(text.getBytes(StandardCharsets.UTF_8), text.getBytes(StandardCharsets.UTF_8).length); }
    private static void closeSerial() { if (serial != null) { try { serial.closePort(); } catch (Exception ignored) {} serial = null; } }

    private static void selectSystemPrinter(JFrame owner) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services.length == 0) { message(owner, "No operating-system printers found."); return; }
        String[] names = new String[services.length]; for (int i=0;i<services.length;i++) names[i]=services[i].getName();
        String selected = (String) JOptionPane.showInputDialog(owner, "Select printer:", "System Printer", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (selected != null) { save(SET_NAME, selected); message(owner, "Saved system printer: " + selected); }
    }

    private static void systemPrint(String text) throws Exception {
        String wanted = setting(SET_NAME, "");
        PrintService selected = null;
        for (PrintService p : PrintServiceLookup.lookupPrintServices(null, null)) if (p.getName().equals(wanted)) selected=p;
        if (selected == null) selected = PrintServiceLookup.lookupDefaultPrintService();
        if (selected == null) throw new IllegalStateException("No system printer selected");
        DocPrintJob job = selected.createPrintJob();
        Doc doc = new SimpleDoc(text, DocFlavor.STRING.TEXT_PLAIN, null);
        job.print(doc, null);
    }

    private static void settingsDialog(JFrame owner, String role) {
        if (!("ADMIN".equalsIgnoreCase(role) || "OWNER".equalsIgnoreCase(role))) { message(owner, "Only Admin/Owner can change printer settings."); return; }
        JTextField mac = new JTextField(setting(SET_MAC, ""), 20);
        JTextField port = new JTextField(setting(SET_PORT, ""), 12);
        JCheckBox auto = new JCheckBox("Auto reconnect when POS starts", Boolean.parseBoolean(setting(SET_AUTO, "true")));
        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Bluetooth MAC")); p.add(mac); p.add(new JLabel("RFCOMM/COM Port")); p.add(port); p.add(new JLabel("Reconnect")); p.add(auto);
        if (JOptionPane.showConfirmDialog(owner,p,"80mm Printer Settings",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            save(SET_MAC, mac.getText().trim()); save(SET_PORT, port.getText().trim()); save(SET_AUTO, Boolean.toString(auto.isSelected()));
            message(owner,"Printer settings saved. The POS will retry the saved port on launch.");
        }
    }

    private static String statusText() {
        if (serial != null && serial.isOpen()) return "CONNECTED — " + serial.getSystemPortName() + " / " + serial.getDescriptivePortName();
        return "DISCONNECTED — saved port: " + setting(SET_PORT, "none") + ", MAC: " + setting(SET_MAC, "none");
    }

    private static String center(String s,int width){ if(s.length()>=width)return s; int n=(width-s.length())/2; return " ".repeat(n)+s; }
    private static void message(Component c,String s){JOptionPane.showMessageDialog(c,s,"Printer",JOptionPane.INFORMATION_MESSAGE);}
    private static String setting(String k,String d){try(Connection c=DriverManager.getConnection(DB);PreparedStatement p=c.prepareStatement("SELECT value FROM settings WHERE key=?")){p.setString(1,k);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):d;}}catch(Exception e){return d;}}
    private static void save(String k,String v){try(Connection c=DriverManager.getConnection(DB);PreparedStatement p=c.prepareStatement("INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")){p.setString(1,k);p.setString(2,v);p.executeUpdate();}catch(Exception e){throw new RuntimeException(e);}}
}
