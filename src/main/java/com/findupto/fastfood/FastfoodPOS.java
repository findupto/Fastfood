package com.findupto.fastfood;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Fastfood Store POS: login, products, inventory, sales, receipts and reports. */
public class FastfoodPOS extends JFrame {
    private static final String DB = "jdbc:sqlite:fastfood.db";
    private static final double TAX = .05;
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String user;
    private final List<Item> cart = new ArrayList<>();
    private final DefaultTableModel cartModel = model("Item", "Qty", "Price", "Amount");
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel sub = new JLabel(), tax = new JLabel(), disc = new JLabel(), total = new JLabel();
    private final JTextField discount = new JTextField("0", 5), cash = new JTextField(8);
    private final JTextArea receipt = new JTextArea();
    private final JLabel status = new JLabel(" Ready");

    record Product(int id, String code, String name, String category, double price, int stock) {}
    record Item(Product product, int qty) {}

    public FastfoodPOS(String user) {
        this.user = user;
        setTitle("FASTFOOD STORE POS - " + user);
        setDefaultCloseOperation(EXIT_ON_CLOSE); setMinimumSize(new Dimension(1200, 760)); setLocationRelativeTo(null);
        build(); refreshTotals();
    }
    private static DefaultTableModel model(String... cols) { return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } }; }
    private static Connection db() throws SQLException { return DriverManager.getConnection(DB); }

    private static void initDb() {
        try (Connection c = db(); Statement s = c.createStatement()) {
            s.executeUpdate("PRAGMA foreign_keys=ON");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY,username TEXT UNIQUE,password TEXT,role TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT UNIQUE,name TEXT NOT NULL,category TEXT NOT NULL,price REAL NOT NULL,stock INTEGER NOT NULL DEFAULT 0,active INTEGER NOT NULL DEFAULT 1)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS sales(id INTEGER PRIMARY KEY AUTOINCREMENT,receipt_no TEXT UNIQUE,sold_at TEXT,total REAL,discount REAL,tax REAL,cash REAL,change_amount REAL,payment TEXT,cashier TEXT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS sale_items(id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER REFERENCES sales(id),product_id INTEGER,product_name TEXT,qty INTEGER,price REAL,amount REAL)");
            s.execute("INSERT OR IGNORE INTO users(username,password,role) VALUES('admin','admin','ADMIN')");
            s.execute("INSERT OR IGNORE INTO users(username,password,role) VALUES('cashier','cashier','CASHIER')");
            try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO products(code,name,category,price,stock) VALUES(?,?,?,?,?)")) {
                Object[][] seed={{"B01","Zinger Burger","Burgers",450,50},{"B02","Chicken Burger","Burgers",380,50},{"B03","Beef Burger","Burgers",520,30},{"F01","Chicken Fries","Fries",300,60},{"F02","Loaded Fries","Fries",420,40},{"P01","Chicken Pizza","Pizza",850,25},{"P02","Pepperoni Pizza","Pizza",950,25},{"D01","Cold Drink","Drinks",120,100},{"D02","Fresh Lemonade","Drinks",180,80},{"D03","Mineral Water","Drinks",80,100}};
                for(Object[] x:seed){for(int i=0;i<x.length;i++)p.setObject(i+1,x[i]);p.addBatch();} p.executeBatch();
            }
        } catch(SQLException e){ throw new RuntimeException("Database initialization failed: "+e.getMessage(),e); }
    }
    private static boolean login(String u,String p){
        try(Connection c=db();PreparedStatement s=c.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")){s.setString(1,u);s.setString(2,p);try(ResultSet r=s.executeQuery()){return r.next();}} catch(SQLException e){return false;}
    }
    private static List<Product> products(){
        List<Product> out=new ArrayList<>();
        try(Connection c=db();PreparedStatement s=c.prepareStatement("SELECT id,code,name,category,price,stock FROM products WHERE active=1 ORDER BY category,name");ResultSet r=s.executeQuery()){
            while(r.next())out.add(new Product(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getDouble(5),r.getInt(6)));
        }catch(SQLException e){error(e.getMessage());} return out;
    }

    private void build(){
        JPanel root=new JPanel(new BorderLayout(10,10));root.setBorder(new EmptyBorder(10,10,10,10));
        JLabel title=new JLabel("FASTFOOD STORE POS");title.setFont(new Font("SansSerif",Font.BOLD,25));
        JPanel head=new JPanel(new BorderLayout());head.add(title,BorderLayout.WEST);head.add(new JLabel("Cashier: "+user),BorderLayout.EAST);root.add(head,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("POS",posPanel());tabs.addTab("Products & Stock",productsPanel());tabs.addTab("Reports",reportsPanel());root.add(tabs,BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEtchedBorder());root.add(status,BorderLayout.SOUTH);setContentPane(root);
    }
    private JPanel posPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8));JPanel menu=new JPanel(new GridLayout(0,3,6,6));menu.setBorder(BorderFactory.createTitledBorder("Menu"));
        for(Product x:products()){JButton b=new JButton("<html><b>"+x.name()+"</b><br>"+MONEY.format(x.price())+"<br>Stock: "+x.stock()+"</html>");b.setEnabled(x.stock()>0);b.addActionListener(e->add(x));menu.add(b);}
        p.add(new JScrollPane(menu),BorderLayout.CENTER);JPanel right=new JPanel(new BorderLayout(5,5));right.setPreferredSize(new Dimension(470,600));right.add(new JScrollPane(cartTable),BorderLayout.CENTER);cartTable.setRowHeight(28);
        JPanel controls=new JPanel(new GridLayout(0,2,5,5));controls.add(new JLabel("Discount %"));controls.add(discount);controls.add(new JLabel("Cash Received"));controls.add(cash);controls.add(new JLabel("Subtotal"));controls.add(sub);controls.add(new JLabel("Tax (5%)"));controls.add(tax);controls.add(new JLabel("Discount"));controls.add(disc);controls.add(new JLabel("TOTAL"));controls.add(total);
        JButton checkout=new JButton("CHECKOUT / SAVE SALE");checkout.addActionListener(e->checkout());JButton remove=new JButton("Remove");remove.addActionListener(e->{int r=cartTable.getSelectedRow();if(r>=0){cart.remove(r);refreshCart();}});JButton clear=new JButton("Clear");clear.addActionListener(e->{cart.clear();refreshCart();});JButton print=new JButton("Print Receipt");print.addActionListener(e->printReceipt());
        JPanel actions=new JPanel(new GridLayout(1,4,5,5));actions.add(remove);actions.add(clear);actions.add(print);actions.add(checkout);controls.add(actions);controls.add(new JLabel());right.add(controls,BorderLayout.SOUTH);p.add(right,BorderLayout.EAST);return p;
    }
    private JPanel productsPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8));DefaultTableModel m=model("ID","Code","Name","Category","Price","Stock");JTable t=new JTable(m);refreshProductTable(m);p.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));JTextField code=new JTextField(6),name=new JTextField(14),cat=new JTextField(10),price=new JTextField(7),stock=new JTextField(5);
        form.add(new JLabel("Code"));form.add(code);form.add(new JLabel("Name"));form.add(name);form.add(new JLabel("Category"));form.add(cat);form.add(new JLabel("Price"));form.add(price);form.add(new JLabel("Stock"));form.add(stock);
        JButton save=new JButton("Add / Update");save.addActionListener(e->{try{saveProduct(m,code,name,cat,price,stock);}catch(Exception ex){error("Invalid product data: "+ex.getMessage());}});
        JButton del=new JButton("Deactivate Selected");del.addActionListener(e->{int r=t.getSelectedRow();if(r>=0){int id=(int)m.getValueAt(r,0);try(Connection c=db();PreparedStatement s=c.prepareStatement("UPDATE products SET active=0 WHERE id=?")){s.setInt(1,id);s.executeUpdate();refreshProductTable(m);}catch(SQLException ex){error(ex.getMessage());}}});
        form.add(save);form.add(del);p.add(form,BorderLayout.SOUTH);return p;
    }
    private void refreshProductTable(DefaultTableModel m){m.setRowCount(0);for(Product x:products())m.addRow(new Object[]{x.id(),x.code(),x.name(),x.category(),MONEY.format(x.price()),x.stock()});}
    private void saveProduct(DefaultTableModel m,JTextField code,JTextField name,JTextField cat,JTextField price,JTextField stock)throws SQLException{
        String q="INSERT INTO products(code,name,category,price,stock) VALUES(?,?,?,?,?) ON CONFLICT(code) DO UPDATE SET name=excluded.name,category=excluded.category,price=excluded.price,stock=excluded.stock";
        try(Connection c=db();PreparedStatement s=c.prepareStatement(q)){s.setString(1,code.getText().trim());s.setString(2,name.getText().trim());s.setString(3,cat.getText().trim());s.setDouble(4,Double.parseDouble(price.getText()));s.setInt(5,Integer.parseInt(stock.getText()));s.executeUpdate();}refreshProductTable(m);status.setText(" Product saved");
    }
    private JPanel reportsPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8));JTextArea out=new JTextArea();out.setEditable(false);out.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));JButton refresh=new JButton("Refresh Dashboard");refresh.addActionListener(e->out.setText(report()));p.add(refresh,BorderLayout.NORTH);p.add(new JScrollPane(out),BorderLayout.CENTER);out.setText(report());return p;
    }
    private String report(){
        StringBuilder b=new StringBuilder("FASTFOOD SALES DASHBOARD\n=========================\n\n");
        try(Connection c=db();Statement s=c.createStatement()){
            try(ResultSet r=s.executeQuery("SELECT COUNT(*),COALESCE(SUM(total),0),COALESCE(AVG(total),0) FROM sales WHERE date(sold_at)=date('now','localtime')")){if(r.next())b.append("Today Orders : ").append(r.getInt(1)).append("\nToday Sales  : ").append(MONEY.format(r.getDouble(2))).append("\nAvg Order    : ").append(MONEY.format(r.getDouble(3))).append("\n\n");}
            b.append("RECENT SALES\n");try(ResultSet r=s.executeQuery("SELECT receipt_no,sold_at,total,payment,cashier FROM sales ORDER BY id DESC LIMIT 15")){while(r.next())b.append(String.format("%-18s %-19s %10s %-8s %s%n",r.getString(1),r.getString(2),MONEY.format(r.getDouble(3)),r.getString(4),r.getString(5)));}
            b.append("\nTOP SELLING ITEMS\n");try(ResultSet r=s.executeQuery("SELECT product_name,SUM(qty) qty FROM sale_items GROUP BY product_id ORDER BY qty DESC LIMIT 10")){while(r.next())b.append(String.format("%-25s %5d%n",r.getString(1),r.getInt(2)));}
            b.append("\nLOW STOCK (<=10)\n");try(ResultSet r=s.executeQuery("SELECT code,name,stock FROM products WHERE active=1 AND stock<=10 ORDER BY stock")){while(r.next())b.append(String.format("%-6s %-25s %5d%n",r.getString(1),r.getString(2),r.getInt(3)));}
        }catch(SQLException e){b.append("Database error: ").append(e.getMessage());}return b.toString();
    }
    private void add(Product p){
        for(Item i:cart)if(i.product.id()==p.id()){if(i.qty()>=p.stock()){error("Not enough stock.");return;}cart.set(cart.indexOf(i),new Item(i.product(),i.qty()+1));refreshCart();return;}cart.add(new Item(p,1));refreshCart();
    }
    private void refreshCart(){cartModel.setRowCount(0);for(Item i:cart)cartModel.addRow(new Object[]{i.product.name(),i.qty(),MONEY.format(i.product.price()),MONEY.format(i.product.price()*i.qty())});refreshTotals();}
    private double subtotal(){return cart.stream().mapToDouble(i->i.product.price()*i.qty()).sum();}
    private double discountAmount(){try{return Math.max(0,Math.min(100,Double.parseDouble(discount.getText())))*subtotal()/100;}catch(Exception e){return 0;}}
    private double total(){double s=subtotal(),d=discountAmount();return s-d+(s-d)*TAX;}
    private void refreshTotals(){double s=subtotal(),d=discountAmount(),t=(s-d)*TAX;sub.setText(MONEY.format(s));tax.setText(MONEY.format(t));disc.setText(MONEY.format(d));total.setText(MONEY.format(s-d+t));total.setFont(total.getFont().deriveFont(Font.BOLD,16f));}

    private void checkout(){
        if(cart.isEmpty()){error("Order is empty.");return;}double cashValue;try{cashValue=Double.parseDouble(cash.getText());}catch(Exception e){error("Enter valid cash received.");return;}if(cashValue<total()){error("Insufficient cash. Required: "+MONEY.format(total()));return;}
        double s=subtotal(),d=discountAmount(),t=(s-d)*TAX,tot=s-d+t,change=cashValue-tot;String receiptNo="FF-"+System.currentTimeMillis();
        try(Connection c=db()){c.setAutoCommit(false);try(PreparedStatement sale=c.prepareStatement("INSERT INTO sales(receipt_no,sold_at,total,discount,tax,cash,change_amount,payment,cashier) VALUES(?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                sale.setString(1,receiptNo);sale.setString(2,LocalDateTime.now().format(DT));sale.setDouble(3,tot);sale.setDouble(4,d);sale.setDouble(5,t);sale.setDouble(6,cashValue);sale.setDouble(7,change);sale.setString(8,"CASH");sale.setString(9,user);sale.executeUpdate();
                int saleId;try(ResultSet keys=sale.getGeneratedKeys()){if(!keys.next())throw new SQLException("Could not create sale ID");saleId=keys.getInt(1);}
                try(PreparedStatement item=c.prepareStatement("INSERT INTO sale_items(sale_id,product_id,product_name,qty,price,amount) VALUES(?,?,?,?,?,?)");PreparedStatement stock=c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=? AND stock>=?")){
                    for(Item x:cart){stock.setInt(1,x.qty());stock.setInt(2,x.product.id());stock.setInt(3,x.qty());if(stock.executeUpdate()!=1)throw new SQLException("Stock changed for "+x.product.name());item.setInt(1,saleId);item.setInt(2,x.product.id());item.setString(3,x.product.name());item.setInt(4,x.qty());item.setDouble(5,x.product.price());item.setDouble(6,x.product.price()*x.qty());item.addBatch();}item.executeBatch();}
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }catch(Exception e){error("Could not save sale: "+e.getMessage());return;}
        receipt.setText(buildReceipt(receiptNo,cashValue,change));cart.clear();cash.setText("");discount.setText("0");refreshCart();status.setText(" Sale saved: "+receiptNo);JOptionPane.showMessageDialog(this,"Sale completed\nReceipt: "+receiptNo,"Success",JOptionPane.INFORMATION_MESSAGE);
    }
    private String buildReceipt(String no,double cash,double change){StringBuilder b=new StringBuilder();b.append("========================================\n FASTFOOD STORE POS\n Receipt: ").append(no).append("\n").append(LocalDateTime.now().format(DT)).append("\n Cashier: ").append(user).append("\n========================================\n");for(Item i:cart)b.append(String.format("%-22s %2dx %10s%n",i.product.name(),i.qty(),MONEY.format(i.product.price()*i.qty())));b.append("----------------------------------------\n");b.append(String.format("%-25s %10s%n","Subtotal",MONEY.format(subtotal())));b.append(String.format("%-25s %10s%n","Discount",MONEY.format(discountAmount())));b.append(String.format("%-25s %10s%n","Tax",MONEY.format((subtotal()-discountAmount())*TAX)));b.append(String.format("%-25s %10s%n","TOTAL",MONEY.format(total())));b.append(String.format("%-25s %10s%n","Cash",MONEY.format(cash)));b.append(String.format("%-25s %10s%n","Change",MONEY.format(change)));b.append("========================================\nThank you! Visit again.\n");return b.toString();}
    private void printReceipt(){if(receipt.getText().isBlank()){error("Checkout an order first.");return;}try{if(!receipt.print())status.setText(" Printing cancelled");}catch(PrinterException e){error(e.getMessage());}}
    private static void error(String s){JOptionPane.showMessageDialog(null,s,"Fastfood POS",JOptionPane.WARNING_MESSAGE);}
    public static void main(String[] args){initDb();SwingUtilities.invokeLater(()->{try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}JTextField u=new JTextField("admin"),p=new JPasswordField();Object[] msg={"Username:",u,"Password:",p,"Default demo login: admin / admin"};int x=JOptionPane.showConfirmDialog(null,msg,"Fastfood POS Login",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(x==JOptionPane.OK_OPTION&&login(u.getText().trim(),p.getText()))new FastfoodPOS(u.getText().trim()).setVisible(true);else System.exit(0);});}
}
