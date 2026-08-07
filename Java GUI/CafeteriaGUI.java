// CafeGUI.java - Complete Cafeteria GUI with CSV Integration
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class CafeteriaGUI extends JFrame {
    // CSV file paths
    private static final String MENU_CSV = "menu.csv";
    private static final String ORDERS_CSV = "orders.csv";
    private static final String SALES_CSV = "sales.csv";
    
    // GUI Components
    private JTabbedPane tabbedPane;
    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private JLabel totalLabel, timeLabel, dateLabel, statusLabel;
    private double totalAmount = 0.0;
    private Map<Integer, Integer> cartItems = new HashMap<>(); // itemId -> quantity
    
    public CafeteriaGUI() {
        setTitle("Cafeteria Management System - Java GUI");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Create menu bar
        createMenuBar();
        
        // Create header panel
        createHeaderPanel();
        
        // Create main tabbed interface
        createTabbedPane();
        
        // Create status bar
        createStatusBar();
        
        // Start clock
        startClock();
        
        // Load initial data
        loadMenuFromCSV();
    }
    
    // ==================== CSV OPERATIONS ====================
    
    private void loadMenuFromCSV() {
        try {
            File file = new File(MENU_CSV);
            if (!file.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "menu.csv not found. Create sample menu?",
                    "File Not Found",
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    createSampleMenuCSV();
                }
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            menuTableModel.setRowCount(0);
            
            // Skip header if exists
            line = reader.readLine();
            if (line == null || !line.contains("ID")) {
                // No header or invalid format
                reader.close();
                return;
            }
            
            int itemCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    try {
                        Object[] rowData = {
                            Integer.parseInt(data[0].trim()),
                            data[1].trim(),
                            Integer.parseInt(data[2].trim()),
                            Integer.parseInt(data[3].trim()),
                            data.length > 4 ? data[4].trim() : "General"
                        };
                        menuTableModel.addRow(rowData);
                        itemCount++;
                    } catch (NumberFormatException e) {
                        // Skip invalid rows
                    }
                }
            }
            
            reader.close();
            statusLabel.setText("Loaded " + itemCount + " items from menu.csv");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading menu: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveMenuToCSV() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(MENU_CSV));
            writer.println("ID,Name,Price,Stock,Category");
            
            for (int i = 0; i < menuTableModel.getRowCount(); i++) {
                writer.printf("%d,%s,%d,%d,%s\n",
                    (Integer) menuTableModel.getValueAt(i, 0),
                    (String) menuTableModel.getValueAt(i, 1),
                    (Integer) menuTableModel.getValueAt(i, 2),
                    (Integer) menuTableModel.getValueAt(i, 3),
                    (String) menuTableModel.getValueAt(i, 4));
            }
            
            writer.close();
            
            statusLabel.setText("Menu saved to " + MENU_CSV);
            JOptionPane.showMessageDialog(this,
                "✅ Menu saved successfully!\nReady for C++ program to read.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving menu: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createSampleMenuCSV() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(MENU_CSV));
            writer.println("ID,Name,Price,Stock,Category");
            
            // Sample data matching C++ program format
            String[][] sampleData = {
                {"1", "Burger", "200", "50", "Fast Food"},
                {"2", "Pizza", "500", "30", "Fast Food"},
                {"3", "Fries", "150", "100", "Side"},
                {"4", "Sandwich", "180", "40", "Fast Food"},
                {"5", "Pasta", "350", "25", "Main Course"},
                {"6", "Nuggets", "250", "60", "Fast Food"},
                {"7", "Shawarma", "300", "35", "Fast Food"},
                {"8", "Cold Drink", "100", "120", "Beverage"},
                {"9", "Coffee", "120", "80", "Beverage"},
                {"10", "Ice Cream", "160", "45", "Dessert"}
            };
            
            for (String[] data : sampleData) {
                writer.println(String.join(",", data));
            }
            
            writer.close();
            
            // Load the created file
            loadMenuFromCSV();
            
            JOptionPane.showMessageDialog(this,
                "✅ Sample menu created with 10 items!\nYour C++ program can now read this file.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error creating sample menu: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveOrderToCSV(int orderId, String itemsString, double total, 
                               String customerName, String tableNo, String phone) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());
            
            // Save detailed order to orders.csv
            PrintWriter orderWriter = new PrintWriter(new FileWriter(ORDERS_CSV, true));
            orderWriter.printf("%d,\"%s\",%s,%.2f,%s,%s,%s\n",
                orderId, itemsString, timestamp, total, customerName, tableNo, phone);
            orderWriter.close();
            
            // Save summary to sales.csv
            PrintWriter salesWriter = new PrintWriter(new FileWriter(SALES_CSV, true));
            salesWriter.printf("%d,%.2f,%s,%s,%s,%s\n",
                orderId, total, timestamp, customerName, tableNo, phone);
            salesWriter.close();
            
            // Update stock in menu.csv
            updateStockInMenu();
            
            statusLabel.setText("Order #" + orderId + " saved successfully!");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving order: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStockInMenu() {
        try {
            // Read menu, update stock, write back
            File inputFile = new File(MENU_CSV);
            File tempFile = new File("temp_menu.csv");
            
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
            
            String line = reader.readLine(); // Read header
            writer.println(line);
            
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    try {
                        int itemId = Integer.parseInt(data[0].trim());
                        int currentStock = Integer.parseInt(data[3].trim());
                        
                        // Reduce stock if item is in cart
                        if (cartItems.containsKey(itemId)) {
                            int quantity = cartItems.get(itemId);
                            currentStock -= quantity;
                            data[3] = String.valueOf(currentStock);
                        }
                        
                        writer.println(String.join(",", data));
                    } catch (NumberFormatException e) {
                        writer.println(line);
                    }
                }
            }
            
            reader.close();
            writer.close();
            
            // Replace original file
            if (inputFile.delete()) {
                tempFile.renameTo(inputFile);
            }
            
            // Reload menu to show updated stock
            loadMenuFromCSV();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ==================== ORDER OPERATIONS ====================
    
    private void addSelectedToCart() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            int id = (Integer) menuTableModel.getValueAt(modelRow, 0);
            String name = (String) menuTableModel.getValueAt(modelRow, 1);
            int price = (Integer) menuTableModel.getValueAt(modelRow, 2);
            int stock = (Integer) menuTableModel.getValueAt(modelRow, 3);
            
            String qtyStr = JOptionPane.showInputDialog(this,
                "Enter quantity for " + name + " (Available: " + stock + "):",
                "1");
            
            if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                try {
                    int quantity = Integer.parseInt(qtyStr);
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantity must be positive!");
                        return;
                    }
                    
                    if (quantity > stock) {
                        JOptionPane.showMessageDialog(this,
                            "Not enough stock! Available: " + stock);
                        return;
                    }
                    
                    // Add to cart
                    double itemTotal = price * quantity;
                    String cartItem = String.format("%s (ID: %d) x%d = Rs.%.2f",
                        name, id, quantity, itemTotal);
                    
                    cartModel.addElement(cartItem);
                    
                    // Track item quantities for stock update
                    cartItems.put(id, cartItems.getOrDefault(id, 0) + quantity);
                    
                    totalAmount += itemTotal;
                    updateTotalLabel();
                    
                    statusLabel.setText("Added " + name + " x" + quantity + " to cart");
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item from the menu!");
        }
    }
    
    private void removeFromCart() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String cartItem = cartModel.get(selectedIndex);
            
            // Extract item ID and quantity from cart item
            try {
                String itemPart = cartItem.split("\\(")[1].split("\\)")[0];
                int itemId = Integer.parseInt(itemPart.replace("ID: ", "").trim());
                
                // Extract quantity from format "x3"
                String qtyPart = cartItem.split("x")[1].split("=")[0].trim();
                int quantity = Integer.parseInt(qtyPart);
                
                // Remove from cart tracking
                if (cartItems.containsKey(itemId)) {
                    cartItems.put(itemId, cartItems.get(itemId) - quantity);
                    if (cartItems.get(itemId) <= 0) {
                        cartItems.remove(itemId);
                    }
                }
                
                // Extract total from cart item
                String[] parts = cartItem.split(" = Rs.");
                if (parts.length == 2) {
                    double itemTotal = Double.parseDouble(parts[1]);
                    totalAmount -= itemTotal;
                }
                
            } catch (Exception e) {
                // If parsing fails, estimate 50% refund
                totalAmount *= 0.5;
            }
            
            cartModel.remove(selectedIndex);
            updateTotalLabel();
            statusLabel.setText("Item removed from cart");
        }
    }
    
    private void clearCart() {
        if (!cartModel.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from cart?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                cartModel.clear();
                cartItems.clear();
                totalAmount = 0.0;
                updateTotalLabel();
                statusLabel.setText("Cart cleared");
            }
        }
    }
    
    private void placeOrder(String customerName, String tableNo, String phone) {
        if (cartModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        
        if (customerName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name!");
            return;
        }
        
        // Generate order ID
        Random rand = new Random();
        int orderId = 1000 + rand.nextInt(9000);
        
        // Create items string for CSV
        StringBuilder itemsBuilder = new StringBuilder();
        for (int i = 0; i < cartModel.size(); i++) {
            if (i > 0) itemsBuilder.append(" + ");
            String item = cartModel.get(i);
            // Remove total from cart item for CSV
            String itemWithoutTotal = item.split(" = ")[0];
            itemsBuilder.append(itemWithoutTotal);
        }
        String itemsString = itemsBuilder.toString();
        
        // Save order to CSV
        saveOrderToCSV(orderId, itemsString, totalAmount, customerName, tableNo, phone);
        
        // Show confirmation
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        
        JOptionPane.showMessageDialog(this,
            String.format("✅ ORDER #%d CONFIRMED!\n\n" +
                        "Customer: %s\n" +
                        "Table: %s\n" +
                        "Phone: %s\n" +
                        "Items: %s\n" +
                        "Total: Rs.%.2f\n" +
                        "Time: %s\n\n" +
                        "📁 Data saved to:\n• orders.csv\n• sales.csv\n• menu.csv (stock updated)",
                orderId, customerName, tableNo, phone, 
                itemsString, totalAmount, timestamp),
            "Order Confirmed",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Clear cart after successful order
        clearCart();
    }
    
    // ==================== GUI CREATION METHODS ====================
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("📥 Load CSV");
        JMenuItem saveItem = new JMenuItem("💾 Save CSV");
        JMenuItem exportItem = new JMenuItem("📤 Export Data");
        JMenuItem exitItem = new JMenuItem("❌ Exit");
        
        loadItem.addActionListener(e -> loadMenuFromCSV());
        saveItem.addActionListener(e -> saveMenuToCSV());
        exportItem.addActionListener(e -> exportData());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem sortItem = new JMenuItem("🔢 Sort by Price");
        JMenuItem searchItem = new JMenuItem("🔍 Search Item");
        JMenuItem statsItem = new JMenuItem("📊 View Statistics");
        
        sortItem.addActionListener(e -> sortMenuByPrice());
        searchItem.addActionListener(e -> searchMenuItem());
        statsItem.addActionListener(e -> showStatistics());
        
        toolsMenu.add(sortItem);
        toolsMenu.add(searchItem);
        toolsMenu.add(statsItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("🔄 Refresh All");
        JMenuItem cartItem = new JMenuItem("🛒 View Cart");
        
        refreshItem.addActionListener(e -> refreshAll());
        cartItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        
        viewMenu.add(refreshItem);
        viewMenu.add(cartItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("☕ CAFETERIA MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        // Time and Date
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setOpaque(false);
        
        timeLabel = new JLabel("00:00:00");
        dateLabel = new JLabel(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
        
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        dateLabel.setForeground(Color.WHITE);
        
        timePanel.add(dateLabel);
        timePanel.add(Box.createHorizontalStrut(20));
        timePanel.add(timeLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(timePanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        
        // Create tabs
        tabbedPane.addTab("📋 Menu", createMenuPanel());
        tabbedPane.addTab("🛒 Order", createOrderPanel());
        tabbedPane.addTab("📊 Sales", createSalesPanel());
        tabbedPane.addTab("⚙️ Manage", createManagePanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Table for menu items
        String[] columns = {"ID", "Name", "Price (Rs.)", "Stock", "Category"};
        menuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        menuTable = new JTable(menuTableModel);
        menuTable.setRowHeight(35);
        menuTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Menu Items"));
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton refreshBtn = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        JButton addToCartBtn = createStyledButton("➕ Add to Cart", new Color(46, 204, 113));
        JButton viewDetailsBtn = createStyledButton("🔍 View Details", new Color(241, 196, 15));
        
        refreshBtn.addActionListener(e -> loadMenuFromCSV());
        addToCartBtn.addActionListener(e -> addSelectedToCart());
        viewDetailsBtn.addActionListener(e -> viewItemDetails());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addToCartBtn);
        buttonPanel.add(viewDetailsBtn);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        
        searchBtn.addActionListener(e -> searchInTable(searchField.getText()));
        searchField.addActionListener(e -> searchInTable(searchField.getText()));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        // Add components
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.WEST);
        southPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Cart list
        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane cartScroll = new JScrollPane(cartList);
        cartScroll.setBorder(BorderFactory.createTitledBorder("🛒 Current Order"));
        
        // Order control buttons
        JPanel orderButtons = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JButton removeBtn = createStyledButton("❌ Remove Item", new Color(231, 76, 60));
        JButton clearBtn = createStyledButton("🗑️ Clear Cart", new Color(149, 165, 166));
        JButton updateBtn = createStyledButton("✏️ Update Qty", new Color(241, 196, 15));
        
        removeBtn.addActionListener(e -> removeFromCart());
        clearBtn.addActionListener(e -> clearCart());
        updateBtn.addActionListener(e -> updateQuantity());
        
        orderButtons.add(removeBtn);
        orderButtons.add(clearBtn);
        orderButtons.add(updateBtn);
        
        // Customer info
        JPanel customerPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Information"));
        
        JTextField nameField = new JTextField();
        JTextField tableField = new JTextField("1");
        JTextField phoneField = new JTextField();
        
        customerPanel.add(new JLabel("Name*:"));
        customerPanel.add(nameField);
        customerPanel.add(new JLabel("Table No:"));
        customerPanel.add(tableField);
        customerPanel.add(new JLabel("Phone:"));
        customerPanel.add(phoneField);
        
        // Total and checkout
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        totalLabel = new JLabel("Total: Rs. 0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(new Color(46, 204, 113));
        
        JButton checkoutBtn = new JButton("✅ PLACE ORDER");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        checkoutBtn.setBackground(new Color(46, 204, 113));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        checkoutBtn.addActionListener(e -> placeOrder(
            nameField.getText(), tableField.getText(), phoneField.getText()));
        
        checkoutPanel.add(totalLabel, BorderLayout.CENTER);
        checkoutPanel.add(checkoutBtn, BorderLayout.SOUTH);
        
        // Layout setup
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(cartScroll, BorderLayout.CENTER);
        leftPanel.add(orderButtons, BorderLayout.EAST);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(customerPanel, BorderLayout.NORTH);
        rightPanel.add(checkoutPanel, BorderLayout.CENTER);
        
        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Text area for sales data
        JTextArea salesArea = new JTextArea();
        salesArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        salesArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(salesArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Sales Data"));
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton loadSalesBtn = createStyledButton("📊 Load Sales", new Color(155, 89, 182));
        JButton todaySalesBtn = createStyledButton("📈 Today's Sales", new Color(52, 152, 219));
        JButton exportBtn = createStyledButton("💾 Export Data", new Color(241, 196, 15));
        
        loadSalesBtn.addActionListener(e -> loadSalesData(salesArea));
        todaySalesBtn.addActionListener(e -> loadTodaySales(salesArea));
        exportBtn.addActionListener(e -> exportData());
        
        buttonPanel.add(loadSalesBtn);
        buttonPanel.add(todaySalesBtn);
        buttonPanel.add(exportBtn);
        
        // Add components
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load initial data
        loadSalesData(salesArea);
        
        return panel;
    }
    
    private JPanel createManagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Management options
        JPanel optionsPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        
        JButton addItemBtn = createStyledButton("➕ Add New Item", new Color(46, 204, 113));
        JButton editItemBtn = createStyledButton("✏️ Edit Item", new Color(241, 196, 15));
        JButton deleteItemBtn = createStyledButton("🗑️ Delete Item", new Color(231, 76, 60));
        JButton updateStockBtn = createStyledButton("📦 Update Stock", new Color(52, 152, 219));
        JButton viewOrdersBtn = createStyledButton("📋 View Orders", new Color(155, 89, 182));
        JButton generateReportBtn = createStyledButton("📊 Generate Report", new Color(230, 126, 34));
        JButton backupBtn = createStyledButton("💾 Backup Data", new Color(149, 165, 166));
        JButton settingsBtn = createStyledButton("⚙️ Settings", new Color(52, 73, 94));
        
        addItemBtn.addActionListener(e -> addNewItem());
        editItemBtn.addActionListener(e -> editSelectedItem());
        deleteItemBtn.addActionListener(e -> deleteSelectedItem());
        updateStockBtn.addActionListener(e -> updateStock());
        viewOrdersBtn.addActionListener(e -> viewAllOrders());
        generateReportBtn.addActionListener(e -> generateReport());
        backupBtn.addActionListener(e -> backupData());
        settingsBtn.addActionListener(e -> showSettings());
        
        optionsPanel.add(addItemBtn);
        optionsPanel.add(editItemBtn);
        optionsPanel.add(deleteItemBtn);
        optionsPanel.add(updateStockBtn);
        optionsPanel.add(viewOrdersBtn);
        optionsPanel.add(generateReportBtn);
        optionsPanel.add(backupBtn);
        optionsPanel.add(settingsBtn);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("System Information"));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoArea.setEditable(false);
        
        // Check CSV files
        File menuFile = new File(MENU_CSV);
        File ordersFile = new File(ORDERS_CSV);
        File salesFile = new File(SALES_CSV);
        
        String infoText = String.format("CSV Files Status:\n" +
                        "• %s: %s\n" +
                        "• %s: %s\n" +
                        "• %s: %s\n\n" +
                        "System ready to connect with C++ backend.\n" +
                        "Total Menu Items: %d",
                MENU_CSV, menuFile.exists() ? "✓ Found" : "✗ Not Found",
                ORDERS_CSV, ordersFile.exists() ? "✓ Found" : "✗ Not Found",
                SALES_CSV, salesFile.exists() ? "✓ Found" : "✗ Not Found",
                menuTableModel.getRowCount());
        
        infoArea.setText(infoText);
        
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        // Add components
        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ==================== HELPER METHODS ====================
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private void updateTotalLabel() {
        totalLabel.setText(String.format("Total: Rs. %.2f", totalAmount));
    }
    
    private void searchInTable(String query) {
        if (query.trim().isEmpty()) {
            menuTable.setRowSorter(null);
        } else {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(menuTableModel);
            menuTable.setRowSorter(sorter);
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }
    
    private void sortMenuByPrice() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(menuTableModel);
        menuTable.setRowSorter(sorter);
        sorter.setComparator(2, Comparator.naturalOrder());
        sorter.sort();
        statusLabel.setText("Menu sorted by price");
    }
    
    private void searchMenuItem() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter item name or ID to search:");
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            searchInTable(searchTerm);
        }
    }
    
    private void showStatistics() {
        // Load and display statistics
        try {
            File salesFile = new File(SALES_CSV);
            if (!salesFile.exists()) {
                JOptionPane.showMessageDialog(this, "No sales data available yet.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(salesFile));
            String line;
            double totalRevenue = 0;
            int orderCount = 0;
            
            // Skip header
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        totalRevenue += Double.parseDouble(parts[1]);
                        orderCount++;
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                    }
                }
            }
            reader.close();
            
            String stats = String.format(
                "📊 SALES STATISTICS\n\n" +
                "Total Orders: %d\n" +
                "Total Revenue: Rs.%.2f\n" +
                "Average Order: Rs.%.2f\n\n" +
                "Data File: %s",
                orderCount, totalRevenue,
                orderCount > 0 ? totalRevenue / orderCount : 0,
                SALES_CSV);
            
            JOptionPane.showMessageDialog(this, stats, "Statistics", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage());
        }
    }
    
    private void viewItemDetails() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            String details = String.format("📋 ITEM DETAILS\n\n" +
                "ID: %d\n" +
                "Name: %s\n" +
                "Price: Rs.%d\n" +
                "Stock: %d\n" +
                "Category: %s\n\n" +
                "CSV File: %s",
                menuTableModel.getValueAt(modelRow, 0),
                menuTableModel.getValueAt(modelRow, 1),
                menuTableModel.getValueAt(modelRow, 2),
                menuTableModel.getValueAt(modelRow, 3),
                menuTableModel.getValueAt(modelRow, 4),
                MENU_CSV);
            
            JOptionPane.showMessageDialog(this, details, "Item Details", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateQuantity() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String cartItem = cartModel.get(selectedIndex);
            
            try {
                // Extract current quantity
                String qtyPart = cartItem.split("x")[1].split("=")[0].trim();
                int currentQty = Integer.parseInt(qtyPart);
                
                String newQtyStr = JOptionPane.showInputDialog(this,
                    "Enter new quantity:", String.valueOf(currentQty));
                
                if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                    int newQty = Integer.parseInt(newQtyStr);
                    if (newQty > 0) {
                        // Update cart item
                        String[] parts = cartItem.split("x");
                        String newItem = parts[0] + "x" + newQty + " = Rs." + 
                            (Double.parseDouble(parts[1].split("=")[1].replace("Rs.", "").trim()) / 
                             currentQty * newQty);
                        
                        cartModel.set(selectedIndex, newItem);
                        
                        // Recalculate total
                        totalAmount = 0;
                        for (int i = 0; i < cartModel.size(); i++) {
                            String item = cartModel.get(i);
                            String[] itemParts = item.split(" = Rs.");
                            if (itemParts.length == 2) {
                                totalAmount += Double.parseDouble(itemParts[1]);
                            }
                        }
                        updateTotalLabel();
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating quantity!");
            }
        }
    }
    
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(236, 240, 241));
        
        statusLabel = new JLabel("Ready - Connected to CSV files");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void startClock() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    timeLabel.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                });
            }
        }, 0, 1000);
    }
    
    private void refreshAll() {
        loadMenuFromCSV();
        statusLabel.setText("All data refreshed");
    }
    
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data");
        fileChooser.setSelectedFile(new File("cafeteria_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.println("Cafeteria Data Export - " + new Date());
                writer.println("==============================");
                
                // Export menu
                writer.println("\nMENU ITEMS:");
                writer.println("ID,Name,Price,Stock,Category");
                for (int i = 0; i < menuTableModel.getRowCount(); i++) {
                    writer.printf("%d,%s,%d,%d,%s\n",
                        menuTableModel.getValueAt(i, 0),
                        menuTableModel.getValueAt(i, 1),
                        menuTableModel.getValueAt(i, 2),
                        menuTableModel.getValueAt(i, 3),
                        menuTableModel.getValueAt(i, 4));
                }
                
                writer.close();
                statusLabel.setText("Data exported to " + file.getName());
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage());
            }
        }
    }
    
    private void loadSalesData(JTextArea salesArea) {
        try {
            File file = new File(SALES_CSV);
            if (!file.exists()) {
                salesArea.setText("No sales data available yet.\nPlace orders to generate data.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder("📊 SALES DATA\n");
            content.append("============\n\n");
            
            // Read header
            String header = reader.readLine();
            content.append(header).append("\n");
            content.append("-".repeat(80)).append("\n");
            
            double totalRevenue = 0;
            int orderCount = 0;
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        totalRevenue += Double.parseDouble(parts[1]);
                        orderCount++;
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                    }
                }
            }
            
            reader.close();
            
            content.append("\n\n📈 SUMMARY\n");
            content.append("=========\n");
            content.append("Total Orders: ").append(orderCount).append("\n");
            content.append("Total Revenue: Rs.").append(String.format("%.2f", totalRevenue)).append("\n");
            content.append("Average Order: Rs.").append(String.format("%.2f", 
                orderCount > 0 ? totalRevenue / orderCount : 0)).append("\n");
            
            salesArea.setText(content.toString());
            
        } catch (IOException e) {
            salesArea.setText("Error loading sales data: " + e.getMessage());
        }
    }
    
    private void loadTodaySales(JTextArea salesArea) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        
        try {
            File file = new File(SALES_CSV);
            if (!file.exists()) {
                salesArea.setText("No sales data available.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder("📅 TODAY'S SALES - ");
            content.append(today).append("\n");
            content.append("=====================\n\n");
            
            // Read header
            String header = reader.readLine();
            content.append(header).append("\n");
            content.append("-".repeat(80)).append("\n");
            
            double totalRevenue = 0;
            int orderCount = 0;
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(today)) {
                    content.append(line).append("\n");
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        try {
                            totalRevenue += Double.parseDouble(parts[1]);
                            orderCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid lines
                        }
                    }
                }
            }
            
            reader.close();
            
            content.append("\n\n📊 TODAY'S SUMMARY\n");
            content.append("=================\n");
            content.append("Orders Today: ").append(orderCount).append("\n");
            content.append("Revenue Today: Rs.").append(String.format("%.2f", totalRevenue)).append("\n");
            
            if (orderCount == 0) {
                content.append("\n⚠️ No sales recorded for today.");
            }
            
            salesArea.setText(content.toString());
            
        } catch (IOException e) {
            salesArea.setText("Error loading today's sales: " + e.getMessage());
        }
    }
    
    // ==================== MANAGEMENT METHODS ====================
    
    private void addNewItem() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField categoryField = new JTextField("General");
        
        Object[] message = {
            "ID:", idField,
            "Name:", nameField,
            "Price (Rs.):", priceField,
            "Stock:", stockField,
            "Category:", categoryField
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, 
            "Add New Item", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                int price = Integer.parseInt(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String category = categoryField.getText();
                
                if (name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                    return;
                }
                
                if (price <= 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Price must be positive and stock non-negative!");
                    return;
                }
                
                // Check if ID already exists
                for (int i = 0; i < menuTableModel.getRowCount(); i++) {
                    if ((Integer) menuTableModel.getValueAt(i, 0) == id) {
                        JOptionPane.showMessageDialog(this, "ID already exists!");
                        return;
                    }
                }
                
                menuTableModel.addRow(new Object[]{id, name, price, stock, category});
                statusLabel.setText("Added new item: " + name);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for ID, Price and Stock!");
            }
        }
    }
    
    private void editSelectedItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            
            int currentId = (Integer) menuTableModel.getValueAt(modelRow, 0);
            String currentName = (String) menuTableModel.getValueAt(modelRow, 1);
            int currentPrice = (Integer) menuTableModel.getValueAt(modelRow, 2);
            int currentStock = (Integer) menuTableModel.getValueAt(modelRow, 3);
            String currentCategory = (String) menuTableModel.getValueAt(modelRow, 4);
            
            JTextField nameField = new JTextField(currentName);
            JTextField priceField = new JTextField(String.valueOf(currentPrice));
            JTextField stockField = new JTextField(String.valueOf(currentStock));
            JTextField categoryField = new JTextField(currentCategory);
            
            Object[] message = {
                "Name:", nameField,
                "Price (Rs.):", priceField,
                "Stock:", stockField,
                "Category:", categoryField
            };
            
            int option = JOptionPane.showConfirmDialog(this, message, 
                "Edit Item (ID: " + currentId + ")", JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    int price = Integer.parseInt(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    String category = categoryField.getText();
                    
                    if (name.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                        return;
                    }
                    
                    if (price <= 0 || stock < 0) {
                        JOptionPane.showMessageDialog(this, "Price must be positive and stock non-negative!");
                        return;
                    }
                    
                    menuTableModel.setValueAt(name, modelRow, 1);
                    menuTableModel.setValueAt(price, modelRow, 2);
                    menuTableModel.setValueAt(stock, modelRow, 3);
                    menuTableModel.setValueAt(category, modelRow, 4);
                    
                    statusLabel.setText("Updated item: " + name);
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to edit!");
        }
    }
    
    private void deleteSelectedItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            String itemName = (String) menuTableModel.getValueAt(modelRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete item: " + itemName + "?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                menuTableModel.removeRow(modelRow);
                statusLabel.setText("Deleted item: " + itemName);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete!");
        }
    }
    
    private void updateStock() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = menuTable.convertRowIndexToModel(selectedRow);
            String itemName = (String) menuTableModel.getValueAt(modelRow, 1);
            int currentStock = (Integer) menuTableModel.getValueAt(modelRow, 3);
            
            String newStockStr = JOptionPane.showInputDialog(this,
                "Enter new stock for " + itemName + ":", 
                String.valueOf(currentStock));
            
            if (newStockStr != null && !newStockStr.trim().isEmpty()) {
                try {
                    int newStock = Integer.parseInt(newStockStr);
                    if (newStock >= 0) {
                        menuTableModel.setValueAt(newStock, modelRow, 3);
                        statusLabel.setText("Updated stock for " + itemName);
                    } else {
                        JOptionPane.showMessageDialog(this, "Stock cannot be negative!");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to update stock!");
        }
    }
    
    private void viewAllOrders() {
        try {
            File file = new File(ORDERS_CSV);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "No orders data available yet.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder("📋 ALL ORDERS\n");
            content.append("=============\n\n");
            
            String line;
            int orderCount = 0;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                orderCount++;
            }
            reader.close();
            
            content.append("\n\n📊 TOTAL ORDERS: ").append(orderCount - 1); // Subtract header
            
            JTextArea textArea = new JTextArea(content.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 600));
            
            JOptionPane.showMessageDialog(this, scrollPane, "All Orders", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }
    
    private void generateReport() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());
            String reportFile = "cafeteria_report_" + timestamp + ".txt";
            
            PrintWriter writer = new PrintWriter(new FileWriter(reportFile));
            
            writer.println("=".repeat(60));
            writer.println("CAFETERIA MANAGEMENT SYSTEM - REPORT");
            writer.println("Generated: " + new Date());
            writer.println("=".repeat(60));
            
            // Menu Summary
            writer.println("\n1. MENU SUMMARY");
            writer.println("-".repeat(40));
            writer.printf("Total Items: %d\n", menuTableModel.getRowCount());
            int totalStock = 0;
            for (int i = 0; i < menuTableModel.getRowCount(); i++) {
                totalStock += (Integer) menuTableModel.getValueAt(i, 3);
            }
            writer.printf("Total Stock: %d\n", totalStock);
            
            // Sales Summary
            writer.println("\n2. SALES SUMMARY");
            writer.println("-".repeat(40));
            
            File salesFile = new File(SALES_CSV);
            if (salesFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(salesFile));
                reader.readLine(); // Skip header
                
                double totalRevenue = 0;
                int orderCount = 0;
                String line;
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        try {
                            totalRevenue += Double.parseDouble(parts[1]);
                            orderCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid lines
                        }
                    }
                }
                reader.close();
                
                writer.printf("Total Orders: %d\n", orderCount);
                writer.printf("Total Revenue: Rs.%.2f\n", totalRevenue);
                writer.printf("Average Order: Rs.%.2f\n", 
                    orderCount > 0 ? totalRevenue / orderCount : 0);
            } else {
                writer.println("No sales data available.");
            }
            
            // Current Cart
            writer.println("\n3. CURRENT CART STATUS");
            writer.println("-".repeat(40));
            writer.printf("Cart Items: %d\n", cartModel.size());
            writer.printf("Cart Total: Rs.%.2f\n", totalAmount);
            
            writer.println("\n" + "=".repeat(60));
            writer.println("End of Report");
            writer.close();
            
            JOptionPane.showMessageDialog(this,
                "✅ Report generated successfully!\nFile: " + reportFile,
                "Report Generated",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
        }
    }
    
    private void backupData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        
        String backupDir = "backup_" + date;
        new File(backupDir).mkdirs();
        
        try {
            // Copy menu.csv
            copyFile(MENU_CSV, backupDir + "/menu_backup.csv");
            
            // Copy orders.csv if exists
            File ordersFile = new File(ORDERS_CSV);
            if (ordersFile.exists()) {
                copyFile(ORDERS_CSV, backupDir + "/orders_backup.csv");
            }
            
            // Copy sales.csv if exists
            File salesFile = new File(SALES_CSV);
            if (salesFile.exists()) {
                copyFile(SALES_CSV, backupDir + "/sales_backup.csv");
            }
            
            statusLabel.setText("Backup created in: " + backupDir);
            JOptionPane.showMessageDialog(this,
                "✅ Backup created successfully!\nLocation: " + backupDir,
                "Backup Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error creating backup: " + e.getMessage());
        }
    }
    
    private void copyFile(String sourcePath, String destPath) throws IOException {
        FileInputStream fis = new FileInputStream(sourcePath);
        FileOutputStream fos = new FileOutputStream(destPath);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        
        fis.close();
        fos.close();
    }
    
    private void showSettings() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JCheckBox autoSave = new JCheckBox("Auto-save changes");
        JCheckBox notifyStock = new JCheckBox("Notify when stock is low");
        JTextField lowStockThreshold = new JTextField("10");
        
        panel.add(new JLabel("Auto Save:"));
        panel.add(autoSave);
        panel.add(new JLabel("Low Stock Alert:"));
        panel.add(notifyStock);
        panel.add(new JLabel("Low Stock Threshold:"));
        panel.add(lowStockThreshold);
        
        int option = JOptionPane.showConfirmDialog(this, panel, 
            "Settings", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            statusLabel.setText("Settings updated");
        }
    }
    
    // ==================== MAIN METHOD ====================
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CafeteriaGUI gui = new CafeteriaGUI();
            gui.setVisible(true);
        });
    }
}