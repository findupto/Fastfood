package com.findupto.fastfood;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fastfood POS - a simple desktop Point of Sale application built with Java Swing.
 * No external runtime dependencies are required.
 */
public class FastfoodPOS extends JFrame {
    private static final double TAX_RATE = 0.05;
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
    private static final DateTimeFormatter RECEIPT_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final List<MenuItem> menu = new ArrayList<>();
    private final List<CartItem> cart = new ArrayList<>();
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Item", "Qty", "Price", "Amount"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel subtotalLabel = new JLabel();
    private final JLabel taxLabel = new JLabel();
    private final JLabel discountLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final JTextField discountField = new JTextField("0", 6);
    private final JTextField cashField = new JTextField(8);
    private final JTextArea receiptArea = new JTextArea();

    public FastfoodPOS() {
        seedMenu();
        setTitle("Fastfood POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        buildUi();
        refreshCart();
    }

    private void seedMenu() {
        menu.add(new MenuItem("B01", "Zinger Burger", 450, "Burgers"));
        menu.add(new MenuItem("B02", "Chicken Burger", 380, "Burgers"));
        menu.add(new MenuItem("B03", "Beef Burger", 520, "Burgers"));
        menu.add(new MenuItem("F01", "Chicken Fries", 300, "Fries"));
        menu.add(new MenuItem("F02", "Loaded Fries", 420, "Fries"));
        menu.add(new MenuItem("P01", "Chicken Pizza", 850, "Pizza"));
        menu.add(new MenuItem("P02", "Pepperoni Pizza", 950, "Pizza"));
        menu.add(new MenuItem("D01", "Cold Drink", 120, "Drinks"));
        menu.add(new MenuItem("D02", "Fresh Lemonade", 180, "Drinks"));
        menu.add(new MenuItem("D03", "Mineral Water", 80, "Drinks"));
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("FASTFOOD POS", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        JLabel subtitle = new JLabel("Point of Sale • Quick Order • Receipt");
        subtitle.setForeground(Color.DARK_GRAY);
        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        for (MenuItem item : menu) menuPanel.add(createMenuButton(item));

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder("Current Order"));
        cartTable.setRowHeight(28);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        cartTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && cartTable.getSelectedRow() >= 0) removeSelectedItem();
            }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.add(menuPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(cartScroll, BorderLayout.CENTER);
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton remove = new JButton("Remove Selected");
        JButton clear = new JButton("Clear Order");
        remove.addActionListener(e -> removeSelectedItem());
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        cartActions.add(remove);
        cartActions.add(clear);
        cartActions.add(new JLabel("Tip: double-click a row to remove it"));
        center.add(cartActions, BorderLayout.SOUTH);

        JPanel summary = new JPanel();
        summary.setBorder(BorderFactory.createTitledBorder("Payment"));
        summary.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        addSummaryRow(summary, c, 0, "Subtotal", subtotalLabel);
        addSummaryRow(summary, c, 1, "Tax (5%)", taxLabel);
        addSummaryRow(summary, c, 2, "Discount", discountLabel);
        addSummaryRow(summary, c, 3, "TOTAL", totalLabel);
        c.gridx = 0; c.gridy = 4; summary.add(new JLabel("Discount %"), c);
        c.gridx = 1; summary.add(discountField, c);
        c.gridx = 0; c.gridy = 5; summary.add(new JLabel("Cash Received"), c);
        c.gridx = 1; summary.add(cashField, c);

        JButton checkout = new JButton("CHECKOUT");
        checkout.setFont(new Font("SansSerif", Font.BOLD, 16));
        checkout.addActionListener(e -> checkout());
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL;
        summary.add(checkout, c);

        JButton print = new JButton("Print Receipt");
        print.addActionListener(e -> printReceipt());
        JButton newOrder = new JButton("New Order");
        newOrder.addActionListener(e -> { cart.clear(); cashField.setText(""); discountField.setText("0"); refreshCart(); receiptArea.setText(""); });
        c.gridy = 7; c.gridwidth = 1; summary.add(print, c);
        c.gridx = 1; summary.add(newOrder, c);

        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBorder(BorderFactory.createTitledBorder("Receipt Preview"));
        JScrollPane receiptScroll = new JScrollPane(receiptArea);
        receiptScroll.setPreferredSize(new Dimension(360, 250));

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(summary, BorderLayout.NORTH);
        right.add(receiptScroll, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, center);
        mainSplit.setResizeWeight(0.32);
        JSplitPane contentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSplit, right);
        contentSplit.setResizeWeight(0.68);
        root.add(contentSplit, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JButton createMenuButton(MenuItem item) {
        JButton button = new JButton("<html><b>" + item.name + "</b><br>" + MONEY.format(item.price) +
                " &nbsp; <small>" + item.category + "</small></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(220, 65));
        button.addActionListener(e -> addToCart(item));
        return button;
    }

    private void addSummaryRow(JPanel panel, GridBagConstraints c, int row, String name, JLabel value) {
        c.gridx = 0; c.gridy = row; panel.add(new JLabel(name), c);
        c.gridx = 1; value.setHorizontalAlignment(SwingConstants.RIGHT); panel.add(value, c);
    }

    private void addToCart(MenuItem item) {
        for (CartItem line : cart) {
            if (line.item.code.equals(item.code)) { line.quantity++; refreshCart(); return; }
        }
        cart.add(new CartItem(item, 1));
        refreshCart();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cart.size()) { cart.remove(row); refreshCart(); }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem line : cart) {
            cartModel.addRow(new Object[]{line.item.name, line.quantity, MONEY.format(line.item.price),
                    MONEY.format(line.item.price * line.quantity)});
        }
        double subtotal = subtotal();
        double discount = discountAmount(subtotal);
        double tax = (subtotal - discount) * TAX_RATE;
        double total = subtotal - discount + tax;
        subtotalLabel.setText(MONEY.format(subtotal));
        taxLabel.setText(MONEY.format(tax));
        discountLabel.setText(MONEY.format(discount));
        totalLabel.setText(MONEY.format(total));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
    }

    private double subtotal() {
        return cart.stream().mapToDouble(x -> x.item.price * x.quantity).sum();
    }

    private double discountAmount(double subtotal) {
        try {
            double percent = Double.parseDouble(discountField.getText().trim());
            percent = Math.max(0, Math.min(100, percent));
            return subtotal * percent / 100.0;
        } catch (NumberFormatException e) { return 0; }
    }

    private double total() {
        double sub = subtotal();
        return sub - discountAmount(sub) + (sub - discountAmount(sub)) * TAX_RATE;
    }

    private void checkout() {
        if (cart.isEmpty()) { showError("The order is empty."); return; }
        final double total = total();
        final double cash;
        try { cash = Double.parseDouble(cashField.getText().trim()); }
        catch (NumberFormatException e) { showError("Enter a valid cash amount."); return; }
        if (cash < total) { showError("Insufficient cash. Required: " + MONEY.format(total)); return; }
        double change = cash - total;
        receiptArea.setText(buildReceipt(cash, change));
        JOptionPane.showMessageDialog(this, "Payment successful. Change: " + MONEY.format(change),
                "Sale Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private String buildReceipt(double cash, double change) {
        StringBuilder r = new StringBuilder();
        r.append("========================================\n");
        r.append("              FASTFOOD STORE            \n");
        r.append("========================================\n");
        r.append(LocalDateTime.now().format(RECEIPT_TIME)).append("\n\n");
        for (CartItem line : cart) {
            double amount = line.item.price * line.quantity;
            r.append(String.format("%-22s %2dx %10s%n", line.item.name, line.quantity, MONEY.format(amount)));
        }
        double sub = subtotal();
        double discount = discountAmount(sub);
        double tax = (sub - discount) * TAX_RATE;
        r.append("----------------------------------------\n");
        r.append(String.format("%-25s %10s%n", "Subtotal", MONEY.format(sub)));
        r.append(String.format("%-25s %10s%n", "Discount", MONEY.format(discount)));
        r.append(String.format("%-25s %10s%n", "Tax (5%)", MONEY.format(tax)));
        r.append(String.format("%-25s %10s%n", "TOTAL", MONEY.format(total())));
        r.append(String.format("%-25s %10s%n", "Cash", MONEY.format(cash)));
        r.append(String.format("%-25s %10s%n", "Change", MONEY.format(change)));
        r.append("========================================\n");
        r.append("           Thank you! Visit again.      \n");
        r.append("========================================\n");
        return r.toString();
    }

    private void printReceipt() {
        if (receiptArea.getText().isBlank()) { showError("Complete checkout first."); return; }
        try {
            if (!receiptArea.print()) showError("Printing was cancelled.");
        } catch (Exception e) { showError("Unable to print receipt: " + e.getMessage()); }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "POS", JOptionPane.WARNING_MESSAGE);
    }

    private record MenuItem(String code, String name, double price, String category) {}
    private static class CartItem {
        final MenuItem item;
        int quantity;
        CartItem(MenuItem item, int quantity) { this.item = item; this.quantity = quantity; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new FastfoodPOS().setVisible(true);
        });
    }
}
