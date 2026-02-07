import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;



/** Workflow.java – Orchestrates order processing and provides the Admin Dashboard menu */
public class Workflow {
    // Professional yet girlish pastel color codes for CLI output
    public static final String ANSI_SOFT_CORAL   = "\u001B[38;5;215m"; // Soft coral for Pending
    public static final String ANSI_LAVENDER_GRAY = "\u001B[38;5;183m"; // Lavender gray for Completed
    public static final String ANSI_MUTED_PEACH  = "\u001B[38;5;215m"; // Muted peach for Shipped
    public static final String ANSI_PASTEL_MINT  = "\u001B[38;5;156m"; // Pastel mint for Accepted
    public static final String ANSI_DUSTY_ROSE   = "\u001B[38;5;172m"; // Dusty rose for Cancelled
    public static final String ANSI_BACKGROUND   = "\u001B[48;5;231m"; // Soft pastel background
    public static final String ANSI_UNDERLINE    = "\u001B[4m";  // Underlined text
    public static final String ANSI_Yellow ="\u001B[38;5;220m";
    // ===== Pastel Theme (Girlish + Professional) =====
    public static final String RESET = "\u001B[0m";
    public static final String BOLD  = "\u001B[1m";

    // Soft pastel colors
    public static final String PINK      = "\u001B[38;5;211m"; // header / highlight
    public static final String LAVENDER  = "\u001B[38;5;183m"; // menu numbers
    public static final String MINT      = "\u001B[38;5;156m"; // success/allowed
   //  public static final String PEACH     = "\u001B[38;5;216m"; // warnings/restricted
    public static final String ROSE      = "\u001B[38;5;174m"; // exit/error
    public static final String SOFTGRAY  = "\u001B[38;5;250m"; // normal text

    // Background (optional)
    public static final String BG_WHITE  = "\u001B[48;5;231m";


    private DataPersistence dp;
    private Log log;
    private PaymentService paymentService;

    public Workflow(DataPersistence dp, Log log) {
        this.dp = dp;
        this.log = log;
        this.paymentService = new PaymentService(log);
    }

    /** Wrapper for Admin authentication */
    public boolean adminLogin(BufferedReader console) throws Exception {
        return Admin.authenticate(dp, console);
    }
    
    private void printLine() {
    System.out.println(SOFTGRAY + "────────────────────────────────────────" + RESET);
    }

    private void printTitle(String text) {
    printLine();
    System.out.println(PINK + BOLD + text + RESET);
    printLine();
    }
    private void printDashboardBox(Admin admin) {
    String top    = "╔══════════════════════════════════════╗";
    String mid    = "║                                      ║";
    String bottom = "╚══════════════════════════════════════╝";

    System.out.println(SOFTGRAY + top + RESET);

    // Centered Title line
    String title = "ADMIN DASHBOARD";
    System.out.println(SOFTGRAY + "║" + RESET
            + PINK + BOLD + centerText(title, 38) + RESET
            + SOFTGRAY + "║" + RESET);

    System.out.println(SOFTGRAY + mid + RESET);

    // Info line: Logged in as
    String info = "Logged in as: " + admin.username + " (" + admin.role + ")";
    System.out.println(SOFTGRAY + "║" + RESET
            + LAVENDER + centerText(info, 38) + RESET
            + SOFTGRAY + "║" + RESET);

    System.out.println(SOFTGRAY + bottom + RESET);
}
private String centerText(String text, int width) {
    if (text == null) text = "";
    if (text.length() >= width) return text.substring(0, width);

    int left = (width - text.length()) / 2;
    int right = width - text.length() - left;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < left; i++) sb.append(' ');
    sb.append(text);
    for (int i = 0; i < right; i++) sb.append(' ');
    return sb.toString();
}



    /** Admin Dashboard menu loop handling all features */
 public void adminDashboard(BufferedReader console) throws Exception {

    // Always refresh current admin (in case index changes later)
    Admin currentAdmin = dp.admins[dp.currentAdminIndex];

    // 🌸 Pink/Rose -> headings, titles
    // 💜 Lavender  -> menu labels/numbers
    // 🌿 Mint      -> success / allowed actions
    // 🧡 Peach     -> warnings / limited actions
    // 🤍 Soft gray -> normal text
      printDashboardBox(currentAdmin);
      printLine();
    while (true) {

        // refresh current admin each loop (safe)
        currentAdmin = dp.admins[dp.currentAdminIndex];

        // ===== MENU HEADER =====
        System.out.print("\n" + LAVENDER + BOLD + "Menu:" + RESET + "\n");

        // ===== ORDER MANAGEMENT =====
        System.out.print(PINK + BOLD + "ORDER MANAGEMENT" + RESET + "\n");
        System.out.print(LAVENDER + " 1." + RESET + " " + MINT + "Accept New Order" + RESET + "\n");
        System.out.print(LAVENDER + " 2." + RESET + " " + MINT + "Update Order Status" + RESET + "\n");
        System.out.print(LAVENDER + " 3." + RESET + " " + MINT + "View Order Logs" + RESET + "\n");
        System.out.print(LAVENDER + " 4." + RESET + " " + MINT + "Search/Filter Orders" + RESET + "\n");
        System.out.print(LAVENDER + " 5." + RESET + " " + MINT + "Generate Receipt" + RESET + "\n");

        // ===== PRODUCT & STOCK =====
        System.out.print("\n" + PINK + BOLD + "PRODUCT & STOCK" + RESET + "\n");
        System.out.print(LAVENDER + " 6." + RESET + " " + MINT + "Advanced Product Filter" + RESET + "\n");

        // Admin/Manager
        if (currentAdmin.role == Role.ADMIN || currentAdmin.role == Role.MANAGER) {
            System.out.print(LAVENDER + " 7." + RESET + " " + MINT + "Manage Products (Add/Edit/Delete)" + RESET + "\n");
            System.out.print(LAVENDER + " 8." + RESET + " " + MINT + "Low Stock Alerts" + RESET + "\n");
            System.out.print(LAVENDER + " 9." + RESET + " " + MINT + "Restock Product" + RESET + "\n");
            System.out.print(LAVENDER + "10." + RESET + " " + MINT + "Export Stock Report" + RESET + "\n");
        } else {
            // show restricted in peach (professional)
            System.out.print(LAVENDER + " 7." + RESET + " " + ROSE + "Manage Products (Admin/Manager only)" + RESET + "\n");
            System.out.print(LAVENDER + " 8." + RESET + " " + ROSE + "Low Stock Alerts (Admin/Manager only)" + RESET + "\n");
            System.out.print(LAVENDER + " 9." + RESET + " " + ROSE + "Restock Product (Admin/Manager only)" + RESET + "\n");
            System.out.print(LAVENDER + "10." + RESET + " " + ROSE + "Export Stock Report (Admin/Manager only)" + RESET + "\n");
        }

        // ===== OPERATIONS =====
        System.out.print("\n" + PINK + BOLD + "OPERATIONS" + RESET + "\n");
        System.out.print(LAVENDER + "13." + RESET + " " + MINT + "Reorder Previous Order" + RESET + "\n");
        System.out.print(LAVENDER + "14." + RESET + " " + MINT + "Retry Failed Order" + RESET + "\n");
        System.out.print(LAVENDER + "19." + RESET + " " + MINT + "Simulation Mode" + RESET + "\n");
        System.out.print(LAVENDER + "20." + RESET + " " + MINT + "Load Test Data" + RESET + "\n");

        // ===== ADMIN ONLY =====
        System.out.print("\n" + PINK + BOLD + "SYSTEM (ADMIN ONLY)" + RESET + "\n");
        if (currentAdmin.role == Role.ADMIN) {
            System.out.print(LAVENDER + "11." + RESET + " " + MINT + "Bulk Import Orders" + RESET + "\n");
            System.out.print(LAVENDER + "12." + RESET + " " + MINT + "Archive Delivered Orders" + RESET + "\n");
            System.out.print(LAVENDER + "15." + RESET + " " + MINT + "Clear Logs" + RESET + "\n");
            System.out.print(LAVENDER + "16." + RESET + " " + MINT + "Add New Admin" + RESET + "\n");
            System.out.print(LAVENDER + "17." + RESET + " " + MINT + "Change Admin Password" + RESET + "\n");
            System.out.print(LAVENDER + "18." + RESET + " " + MINT + "Generate Report" + RESET + "\n");
        } else {
            System.out.print(LAVENDER + "11." + RESET + " " + ROSE + "Bulk Import Orders (Admin only)" + RESET + "\n");
            System.out.print(LAVENDER + "12." + RESET + " " + ROSE + "Archive Delivered Orders (Admin only)" + RESET + "\n");
            System.out.print(LAVENDER + "15." + RESET + " " + ROSE + "Clear Logs (Admin only)" + RESET + "\n");
            System.out.print(LAVENDER + "16." + RESET + " " + ROSE + "Add New Admin (Admin only)" + RESET + "\n");
            System.out.print(LAVENDER + "17." + RESET + " " + ROSE + "Change Admin Password (Admin only)" + RESET + "\n");
            System.out.print(LAVENDER + "18." + RESET + " " + ROSE + "Generate Report (Admin only)" + RESET + "\n");
        }

        // ===== EXIT =====
        System.out.print("\n" + LAVENDER + " 0." + RESET + " " + ROSE + "Exit" + RESET + "\n");
        printLine();
        System.out.print(PINK + BOLD + "Please select an option → " + RESET);

        String choice = console.readLine();
        if (choice == null) choice = "";
        choice = choice.trim();
        System.out.print("\n");
        if (!choice.equals("")) {
        System.out.println(MINT + "You selected option: " + choice + RESET);
        printLine();   // optional but looks professional
        }
        switch (choice) {
            case "1": acceptNewOrder(console); break;
            case "2": handleStatusUpdate(console); break;

            case "3":
                System.out.println(PINK + BOLD + "==== Available Orders (Sorted by Date) ====" + RESET);
                printLine();
                Order[] sortedOrders = Arrays.copyOf(dp.orders, dp.orderCount);
                Arrays.sort(sortedOrders, Comparator.comparing(o -> o.date));

                for (Order order : sortedOrders) {
                    if (order != null) {
                        System.out.println(SOFTGRAY + order.orderId + RESET + SOFTGRAY + " | Date: " + RESET + MINT + order.date + RESET + SOFTGRAY + " | Status: " + RESET + LAVENDER + order.status + RESET);
                    }
                }

                System.out.print(LAVENDER + "Enter Order ID to view logs: " + RESET);
                String logId = console.readLine();
                if (logId != null && !logId.trim().equals("")) {
                    logId = normalizeOrderId(logId.trim());
                    log.viewLogsByOrder(logId);
                }
                break;

            case "4": handleOrderSearch(console); break;
            case "5": generateReceipt(console); break;
            case "6": handleAdvancedFilter(console); break;

            case "7":
                if (currentAdmin.role == Role.ADMIN || currentAdmin.role == Role.MANAGER) {
                    handleProductManagement(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin/Manager only." + RESET);
                }
                break;

            case "8":
                if (currentAdmin.role == Role.ADMIN || currentAdmin.role == Role.MANAGER) {
                    showLowStockAlerts();
                } else {
                    System.out.println(ROSE + "Restricted: Admin/Manager only." + RESET);
                }
                break;

            case "9":
                if (currentAdmin.role == Role.ADMIN || currentAdmin.role == Role.MANAGER) {
                    handleRestock(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin/Manager only." + RESET);
                }
                break;

            case "10":
                if (currentAdmin.role == Role.ADMIN || currentAdmin.role == Role.MANAGER) {
                    exportStockReport();
                } else {
                    System.out.println(ROSE + "Restricted: Admin/Manager only." + RESET);
                }
                break;

            case "11":
                if (currentAdmin.role == Role.ADMIN) {
                    importOrdersFromFile(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "12":
                if (currentAdmin.role == Role.ADMIN) {
                    archiveDeliveredOrders(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "13": handleReorder(console); break;
            case "14": retryCancelledOrder(console); break;

            case "15":
                if (currentAdmin.role == Role.ADMIN) {
                    clearLogs(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "16":
                if (currentAdmin.role == Role.ADMIN) {
                    addNewAdmin(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "17":
                if (currentAdmin.role == Role.ADMIN) {
                    changeAdminPassword(console);
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "18":
                if (currentAdmin.role == Role.ADMIN) {
                    generateReport();
                } else {
                    System.out.println(ROSE + "Restricted: Admin only." + RESET);
                }
                break;

            case "19": runSimulation(console); break;

            case "20":
                System.out.print(LAVENDER + "Enter test data filename (e.g. testdata.txt): " + RESET);
                String file = console.readLine();
                if (file == null) file = "";
                file = file.trim();
                if (!file.equals("")) {
                    dp.loadTestDataFromFile(file);
                    dp.saveAll();
                    System.out.print(MINT + "Loaded test data successfully\n" + RESET);
                    System.out.print(SOFTGRAY + "-> " + dp.productCount + " products loaded.\n" + RESET);
                    System.out.print(SOFTGRAY + "-> " + dp.orderCount + " orders loaded.\n" + RESET);
                    System.out.print(SOFTGRAY + "-> " + dp.adminCount + " admins loaded.\n" + RESET);
                }
                break;

            case "0":
                System.out.print(LAVENDER + "Exiting Admin Dashboard..." + RESET + "\n");
                System.out.print(LAVENDER+ "Thank you for using E-commerce Order Fulfillment Automation System" + RESET + "\n");
                return;

            default:
                System.out.print(ROSE + "Invalid option. Please try again." + RESET + "\n");
                break;
        }

        printLine();
    }
}

private void addNewAdmin(BufferedReader console) throws Exception {
    // Only allow current admin to add new admin if they have the ADMIN role
    Admin currentAdmin = dp.admins[dp.currentAdminIndex];
    if (currentAdmin == null || !currentAdmin.hasPermission(Role.ADMIN)) {
        System.out.print(ROSE+"Permission denied. Only admins can add new admins."+RESET+"\n");
        return;
    }

    // Proceed with adding the new admin
    System.out.print(SOFTGRAY+"Enter new admin username: "+RESET);
    String username = console.readLine().trim();

    System.out.print(SOFTGRAY+"Enter new admin password: "+RESET);
    String password = console.readLine().trim();

    System.out.print(SOFTGRAY+"Enter role (ADMIN, MANAGER, SUPPORT): "+RESET);
    String roleStr = console.readLine().trim().toUpperCase();
    Role role = Role.valueOf(roleStr);

    // Hash the password before saving
    String hashedPassword = Admin.hashPassword(password);

    // Create new admin object
    Admin newAdmin = new Admin(username, hashedPassword, role);

    // Add new admin to the list
    dp.addAdmin(newAdmin);

    // Save the updated admin list to file
    dp.saveAll();

    System.out.println(MINT+"New admin added successfully."+RESET+"\n");
}

private void handleOrderSearch(BufferedReader console) throws Exception {
    System.out.print(SOFTGRAY+"Enter Order ID or Status to search (or press Enter for advanced filter): "+RESET);
    String query = console.readLine();
    if (query == null) query = "";
    query = query.trim();
    if (query.equals("")) {
        // Advanced filtering by multiple criteria
        System.out.print(SOFTGRAY+"Enter Status to filter (or press Enter for any): "+RESET);
        String statusFilter = console.readLine();
        if (statusFilter == null) statusFilter = "";
        statusFilter = statusFilter.trim();
        System.out.print(SOFTGRAY+"Enter Payment Mode to filter (or press Enter for any): "+RESET);
        String paymentFilter = console.readLine();
        if (paymentFilter == null) paymentFilter = "";
        paymentFilter = paymentFilter.trim();
        System.out.print(SOFTGRAY+"Enter Date to filter (YYYY-MM-DD, or press Enter for any): "+RESET);
        String dateFilter = console.readLine();
        if (dateFilter == null) dateFilter = "";
        dateFilter = dateFilter.trim();
        // Convert filters to uppercase for comparison (except date which is numeric string)
        String statusFilterUC = statusFilter.toUpperCase();
        String paymentFilterUC = paymentFilter.toUpperCase();
        Order[] results = new Order[dp.orderCount];
        int count = 0;
        for (int i = 0; i < dp.orderCount; i++) {
            Order o = dp.orders[i];
            if (o == null) continue;
            // Apply status filter if provided
            if (!statusFilterUC.equals("") && !o.status.toUpperCase().equals(statusFilterUC)) {
                continue;
            }
            // Apply payment filter if provided
            if (!paymentFilterUC.equals("") && !o.paymentMode.toUpperCase().equals(paymentFilterUC)) {
                continue;
            }
            // Apply date filter if provided
            if (!dateFilter.equals("") && !o.date.equals(dateFilter)) {
                continue;
            }
            // If all specified criteria match, include this order
            results[count++] = o;
        }
        if (count == 0) {
            System.out.print(ROSE+"No orders found matching the given criteria."+RESET+"\n");
        } else {
            // Display summary of orders that matched all filters
            String statusCrit = statusFilter.equals("") ? "Any" : statusFilter;
            String payCrit = paymentFilter.equals("") ? "Any" : paymentFilter;
            String dateCrit = dateFilter.equals("") ? "Any" : dateFilter;
            System.out.print(SOFTGRAY+"Orders matching filters - Status: "+RESET + statusCrit  +SOFTGRAY+ ", Payment: "+RESET + payCrit +SOFTGRAY+ ", Date: " +RESET+ dateCrit + ":\n");
            for (int i = 0; i < count; i++) {
                Order o = results[i];
                // Prepare status string (with color coding for output if available)
                String statusStr = o.status;
                if (statusStr.equals("DELIVERED")) {
                    statusStr = ANSI_LAVENDER_GRAY + statusStr + RESET; // Lavender for Delivered
                } else if (statusStr.equals("CANCELLED")) {
                    statusStr = ANSI_DUSTY_ROSE + statusStr + RESET; // Dusty Rose for Cancelled
                } else if (statusStr.equals("PENDING")) {
                    statusStr = ANSI_SOFT_CORAL + statusStr + RESET; // Soft Coral for Pending
                } else if (statusStr.equals("SHIPPED")) {
                    statusStr = ANSI_MUTED_PEACH + statusStr + RESET; // Muted Peach for Shipped
                }
                // Print order summary line with relevant details
                System.out.print("- " + o.orderId + " | Date: " + o.date  + " | Payment: " + o.paymentMode  + " | Status: " + statusStr + " | Total: BDT " + o.totalAmount);
                if (o.status.equals("CANCELLED") && o.cancelReason != null && !o.cancelReason.equals("")) {
                    System.out.print(ANSI_DUSTY_ROSE+" | CancelReason: "+RESET+ ANSI_DUSTY_ROSE + o.cancelReason + RESET); // Cancel reason in pastel purple
                }
                System.out.print("\n");
            }
            // Optionally allow viewing details of one order from the results
            System.out.print(SOFTGRAY+"Enter Order ID to view details (or press Enter to skip): "+RESET);
            String selId = console.readLine();
            if (selId == null) selId = "";
            selId = selId.trim();
            if (!selId.equals("")) {
                selId = normalizeOrderId(selId);
                Order target = null;
                for (int i = 0; i < dp.orderCount; i++) {
                    Order o = dp.orders[i];
                    if (o != null && o.orderId.equalsIgnoreCase(selId)) {
                        target = o;
                        break;
                    }
                }
                if (target != null) {
                    viewOrderDetails(target);
                } else {
                    System.out.print(ROSE+"Order " + selId + " not found in results.\n"+RESET);
                }
            }
        }
        return;
    }

    // Standard search by Order ID or Status (existing functionality)
    String q = query.toUpperCase();
    // If input is numeric, normalize to OrderID format (e.g., "1001" -> "O1001")
    if (!q.startsWith("O") && isNumeric(q)) {
        q = "O" + q;
    }
    // Try to find an exact Order ID match
    Order found = null;
    for (int i = 0; i < dp.orderCount; i++) {
        Order o = dp.orders[i];
        if (o != null && o.orderId.toUpperCase().equals(q)) {
            found = o;
            break;
        }
    }
    if (found != null) {
        // Show full details for the matched order ID
        viewOrderDetails(found);
    } else {
        // Treat the input as a status query (substring match on status)
        String statusQuery = q;
        Order[] results = new Order[dp.orderCount];
        int count = 0;
        for (int i = 0; i < dp.orderCount; i++) {
            Order o = dp.orders[i];
            if (o == null) continue;
            if (o.status.toUpperCase().contains(statusQuery)) {
                results[count++] = o;
            }
        }
        if (count == 0) {
            System.out.print(ROSE+"No orders found matching \""+ query + "\".\n"+RESET);
        } else {
            System.out.print("Orders with status containing \"" + query + "\":\n");
            for (int i = 0; i < count; i++) {
                Order o = results[i];
                String statusStr = o.status;
                if (statusStr.equals("DELIVERED")) {
                    statusStr = ANSI_LAVENDER_GRAY + statusStr + RESET; // Lavender for Delivered
                } else if (statusStr.equals("CANCELLED")) {
                    statusStr = ANSI_DUSTY_ROSE + statusStr + RESET; // Dusty Rose for Cancelled
                } else if (statusStr.equals("PENDING")) {
                    statusStr = ANSI_SOFT_CORAL + statusStr + RESET; // Soft Coral for Pending
                } else if (statusStr.equals("SHIPPED")) {
                    statusStr = ANSI_MUTED_PEACH + statusStr + RESET; // Muted Peach for Shipped
                }
                System.out.print(SOFTGRAY+"- " + o.orderId + " | Status: " + statusStr + " | Total: BDT " + o.totalAmount+RESET);
                if (o.cancelReason != null && !o.cancelReason.equals("")) {
                    System.out.print(ANSI_DUSTY_ROSE+" | CancelReason: " + o.cancelReason + RESET); // Cancel reason in pastel purple
                }
                System.out.print("\n");
            }
            // Allow viewing details of a selected order from the list
            System.out.print(SOFTGRAY+"Enter Order ID to view details (or press Enter to skip): "+RESET);
            String selId = console.readLine();
            if (selId == null) selId = "";
            selId = selId.trim();
            if (!selId.equals("")) {
                selId = normalizeOrderId(selId);
                Order target = null;
                for (int i = 0; i < dp.orderCount; i++) {
                    Order o = dp.orders[i];
                    if (o != null && o.orderId.equalsIgnoreCase(selId)) {
                        target = o;
                        break;
                    }
                }
                if (target != null) {
                    viewOrderDetails(target);
                } else {
                    System.out.print(ROSE+"Order " + selId + " not found in results.\n"+RESET);
                }
            }
        }
    }
}


    /** Feature 6: Manually progress an order status through the workflow (PENDING -> PACKED -> SHIPPED -> OUT_FOR_DELIVERY -> DELIVERED) */
    private void handleStatusUpdate(BufferedReader console) throws Exception {
    System.out.print(SOFTGRAY+"Enter Order ID to update status: "+RESET);
    String id = console.readLine();
    if (id == null) id = "";
    id = id.trim();
    if (id.equals("")) {
        System.out.print(ROSE+"Order ID cannot be empty.\n"+RESET);
        return;
    }
    id = normalizeOrderId(id);
    // Find the order by ID
    Order order = null;
    for (int i = 0; i < dp.orderCount; i++) {
        Order o = dp.orders[i];
        if (o != null && o.orderId.equalsIgnoreCase(id)) {
            order = o;
            break;
        }
    }
    if (order == null) {
        System.out.print(ROSE+"Order " + id + " not found.\n"+RESET);
        return;
    }
    String currentStatus = order.status;
    // If order already delivered or cancelled, no further updates allowed
    if (currentStatus.equals("DELIVERED") || currentStatus.equals("CANCELLED")) {
        System.out.print(ROSE+"Order " + id + " is " + currentStatus + "; status cannot be changed.\n"+RESET);
        return;
    }
    // If order is PENDING, attempt to process it (inventory check & payment)
    if (currentStatus.equals("PENDING")) {
        boolean processed = processPendingOrder(order, console);
        if (!processed) {
            // If processing failed, order status is now CANCELLED (reason set in processPendingOrder)
            System.out.print(ROSE+"Order processing failed. Status updated to CANCELLED ("+ order.cancelReason + ").\n"+RESET);
            dp.saveOrders();
            return;
        }
        // If processing succeeded, the order status is now PACKED
        currentStatus = order.status;
    }
    // Determine the next status in the workflow sequence
    String nextStatus = null;
    if (currentStatus.equals("PACKED")) {
        nextStatus = "SHIPPED";
    } else if (currentStatus.equals("SHIPPED")) {
        nextStatus = "OUT_FOR_DELIVERY";
    } else if (currentStatus.equals("OUT_FOR_DELIVERY")) {
        nextStatus = "DELIVERED";
    }
    if (nextStatus == null) {
        System.out.print("No further status transition available for " + currentStatus + ".\n");
        return;
    }
    // Update order status to the next stage
    order.status = nextStatus;
    if (nextStatus.equals("SHIPPED")) {
        // Assign a tracking ID once the order is shipped
        order.trackingId = "TRK" + order.orderId.substring(1);  // e.g., O1005 -> TRK1005
    }
    // Persist the updated orders list to file
    dp.saveOrders();
    log.write(order.orderId, "Status changed to " + nextStatus);
    System.out.print(MINT+"Order " + order.orderId + " status updated to " + nextStatus + ".\n"+RESET);
}


    /** Feature 5 & 8: Reorder a previous order (copy its items into a new order and process it) */
    private void handleReorder(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Enter Order ID to reorder: "+RESET);
        String oldId = console.readLine();
        if (oldId == null) oldId = "";
        oldId = oldId.trim();
        if (oldId.equals("")) {
            System.out.print(ROSE+"Order ID cannot be empty.\n"+RESET);
            return;
        }
        oldId = normalizeOrderId(oldId);
        // Find the original order
        Order original = null;
        for (int i = 0; i < dp.orderCount; i++) {
            if (dp.orders[i] != null && dp.orders[i].orderId.equalsIgnoreCase(oldId)) {
                original = dp.orders[i];
                break;
            }
        }
        if (original == null) {
            System.out.print(ROSE+"Order " + oldId + " not found.\n"+RESET);
            return;
        }
        // Create a new order with the same items (and same address/payment as original, if available)
        Order newOrder = new Order();
        newOrder.orderId = dp.generateOrderId();
        newOrder.date = currentDateString();
        newOrder.address = original.address;
        newOrder.paymentMode = original.paymentMode.equals("") ? "COD" : original.paymentMode;
        // Copy each item from original
        for (int j = 0; j < original.itemCount; j++) {
            Item it = original.items[j];
            if (it == null) continue;
            newOrder.addItem(new Item(it.productId, it.quantity));
        }
        // Process the new order through inventory & payment
        boolean success = processPendingOrder(newOrder, console);
        // Add the new order to system records
        dp.orders[dp.orderCount++] = newOrder;
        if (!success) {
            System.out.print(ROSE+"Reorder created as " + newOrder.orderId + " but failed (" + newOrder.cancelReason + ").\n"+RESET);
        } else {
            System.out.print(MINT+"Reorder successful! New Order ID: " + newOrder.orderId + " (Status: " + newOrder.status + ").\n"+RESET);
            log.write(newOrder.orderId, "Reordered from " + oldId);
        }
    }

    /** Feature 6 (continued): View or filter products by brand or category */
    private void handleAdvancedFilter(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Filter by Brand or Category? (B/C): "+RESET);
        String choice = console.readLine();
        if (choice == null) choice = "";
        choice = choice.trim().toUpperCase();
        if (!choice.equals("B") && !choice.equals("C")) {
            System.out.print(ROSE+"Invalid choice."+RESET+SOFTGRAY+" Enter 'B' for Brand or 'C' for Category.\n"+RESET);
            return;
        }
        System.out.print("Enter " + (choice.equals("B") ? "Brand" : "Category") + " name: ");
        String keyword = console.readLine();
        if (keyword == null) keyword = "";
        keyword = keyword.trim();
        if (keyword.equals("")) {
            System.out.print(ROSE+"Input cannot be empty.\n"+RESET);
            return;
        }
        // Filter products by brand or category (case-insensitive substring match)
        Product[] filtered = new Product[dp.productCount];
        int count = 0;
        for (int i = 0; i < dp.productCount; i++) {
            Product p = dp.products[i];
            if (p == null) continue;
            String field = choice.equals("B") ? p.brand : p.category;
            if (field.toLowerCase().contains(keyword.toLowerCase())) {
                filtered[count++] = p;
            }
        }
        if (count == 0) {
            System.out.print(ROSE+"No products found for \"" + keyword + "\".\n"+RESET);
        } else {
            // Simply list the filtered products (ProductID | Name | Price | Stock)
            System.out.print(SOFTGRAY+"Filtered products (" + (choice.equals("B") ? "Brand" : "Category") + " contains \"" + keyword + "\"):\n"+RESET);
            for (int i = 0; i < count; i++) {
                Product p = filtered[i];
                System.out.print(SOFTGRAY+"- " + p.productId + " | " + p.name + " | BDT " + p.price + " | Stock: " + p.stock + "\n"+RESET);
            }
        }
    }

    /** Feature 13: Display low stock items (stock < 5) highlighted in color */
    private void showLowStockAlerts() {
        boolean anyLow = false;
        System.out.print(ANSI_Yellow+"Low Stock Items (stock < 5):\n"+RESET);
        for (int i = 0; i < dp.productCount; i++) {
            Product p = dp.products[i];
            if (p == null) continue;
            if (p.stock < 5) {
                anyLow = true;
                // Highlight low stock product in yellow
                System.out.print(ANSI_Yellow+ p.productId + " | " + p.name + " | Stock: " + p.stock + RESET + "\n");
            }
        }
        if (!anyLow) {
            System.out.print(ROSE+"None (all products have sufficient stock).\n"+RESET);
        }
    }

    /** Feature 18: Export current stock levels of all products to stock_report.txt */
    private void exportStockReport() throws Exception {
        FileWriter fw = new FileWriter(dp.path("stock_report.txt"), false);
        fw.write("ProductID | Name | Price | Stock\n");
        for (int i = 0; i < dp.productCount; i++) {
            Product p = dp.products[i];
            if (p == null) continue;
            fw.write(p.productId + " | " + p.name + " | " + p.price + " | " + p.stock + "\n");
        }
        fw.close();
        System.out.print(MINT+"Stock report generated in stock_report.txt\n"+RESET);
    }

    /** Feature 10: Bulk import orders from orders_import.txt */
   private void importOrdersFromFile(BufferedReader console) throws Exception{
        BufferedReader br = null;
        int importedCount = 0;
        try {
       br = new BufferedReader(new FileReader(dp.path("orders_import.json")));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;

            // Extract fields manually
            String orderId = extract(line, "\"orderId\":\"", "\"");
            String date = extract(line, "\"date\":\"", "\"");
            String address = extract(line, "\"address\":\"", "\"");
            String paymentMode = extract(line, "\"paymentMode\":\"", "\"");
            String itemList = extract(line, "\"items\":\"", "\"");

            // Create Order object
            Order o = new Order();
            o.orderId = orderId;
            o.date = date;
            o.address = address;
            o.paymentMode = paymentMode;
            o.status = "PENDING";
            dp.parseItemsIntoOrder(o, itemList);

            // Calculate total
            int total = 0;
            for (int i = 0; i < o.itemCount; i++) {
                Product p = dp.findProductById(o.items[i].productId);
                if (p != null) {
                    total += p.price * o.items[i].quantity;
                }
            }
            o.totalAmount = total;

            dp.orders[dp.orderCount++] = o;
            System.out.print(MINT+"Imported: " + o.orderId + "\n"+RESET);
            }
        } catch (Exception e) {
            System.out.print(ROSE+"Error reading orders_import.txt\n"+RESET);
        } finally {
            if (br != null) br.close();
        }
        System.out.print(MINT+importedCount + " orders imported from orders_import.txt.\n"+RESET);
    }
    private String extract(String src, String prefix, String endToken) {
    int start = src.indexOf(prefix);
    if (start == -1) return "";
    start += prefix.length();
    int end = src.indexOf(endToken, start);
    if (end == -1) return src.substring(start);
    return src.substring(start, end);
}

    /** Feature 11: Simulation mode to generate and process orders in various scenarios */
  private void runSimulation(BufferedReader console) throws Exception {
    System.out.print(SOFTGRAY+"Simulation scenarios:\n"+RESET);
    System.out.print(SOFTGRAY+"1. Successful order\n"+RESET);
    System.out.print(SOFTGRAY+"2. Payment failure scenario\n"+RESET);
    System.out.print(SOFTGRAY+"3. Inventory shortage scenario\n"+RESET);
    System.out.print(SOFTGRAY+"4. Random order scenario\n"+RESET);
    System.out.print(SOFTGRAY+"Choose scenario (1-4): "+RESET);
    String opt = console.readLine();
    if (opt == null) opt = "";
    opt = opt.trim();
    if (!opt.matches("[1-4]")) {
        System.out.print(ROSE+"Invalid scenario selection.\n"+RESET);
        return;
    }
    // Create a simulated order
    Order simOrder = new Order();
    simOrder.orderId = dp.generateOrderId();
    simOrder.date = currentDateString();
    simOrder.status = "PENDING";  // Default status

    // Build order based on scenario choice
    if (opt.equals("2")) {
        // Scenario 2: Payment failure – ensure total triggers a decline (simulate by prompting N)
        Product p = dp.products[0];
        if (p == null) {
            System.out.print(ROSE+"No products available for simulation.\n"+RESET);
            return;
        }
        simOrder.addItem(new Item(p.productId, 1));
        simOrder.paymentMode = "MockCard";
        simOrder.status = "CANCELLED"; // Simulate failure
        simOrder.cancelReason = "Payment Failure (MockCard)";
    } else if (opt.equals("3")) {
        // Scenario 3: Inventory shortage – order more than available stock of a product
        Product p = null;
        for (int i = 0; i < dp.productCount; i++) {
            if (dp.products[i] != null && dp.products[i].stock > 0 && dp.products[i].stock < 10) {
                p = dp.products[i];
                break;
            }
        }
        if (p == null) {
            p = dp.products[0];
        }
        int largeQty = (p.stock == 0 ? 5 : p.stock + 5);
        simOrder.addItem(new Item(p.productId, largeQty));
        simOrder.paymentMode = "COD";
        // Mark the order as cancelled due to inventory shortage
        simOrder.status = "CANCELLED"; // Simulate cancellation
        simOrder.cancelReason = "Inventory Shortage";
    } else {
        // Scenario 1 or 4: Successful or Random order – pick 1-2 random items within stock
        if (dp.productCount == 0) {
            System.out.print(ROSE+"No products available to simulate order.\n"+RESET);
            return;
        }
        Product p1 = dp.products[0];
        simOrder.addItem(new Item(p1.productId, 1));
        if (opt.equals("4") && dp.productCount > 1) {
            Product p2 = dp.products[1];
            simOrder.addItem(new Item(p2.productId, 1));
        }
        simOrder.paymentMode = "COD";
        // Successful order – set the status as "DELIVERED"
        simOrder.status = "DELIVERED"; // Mark as delivered for successful order
    }
    simOrder.address = "SimulatedAddress";

    // Process the simulated order
     processPendingOrder(simOrder, console);


    // Add to system records (orders.txt)
    dp.orders[dp.orderCount++] = simOrder;
    // Log to orders.txt
    dp.saveOrders();
    System.out.print(MINT+"Simulation Order " + simOrder.orderId + " created (Status: " + simOrder.status + ").\n"+RESET);
    // Log to log.txt
    log.write(simOrder.orderId, "Simulation order with status: " + simOrder.status);
}

    /** Feature 8: Retry processing a failed (cancelled) order by creating a fresh attempt */
    private void retryCancelledOrder(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Enter Cancelled Order ID to retry: "+RESET);
        String cid = console.readLine();
        if (cid == null) cid = "";
        cid = cid.trim();
        if (cid.equals("")) {
            System.out.print(ROSE+"Order ID cannot be empty.\n"+RESET);
            return;
        }
        cid = normalizeOrderId(cid);
        // Find the cancelled order
        Order original = null;
        for (int i = 0; i < dp.orderCount; i++) {
            Order o = dp.orders[i];
            if (o != null && o.orderId.equalsIgnoreCase(cid) && o.status.equals("CANCELLED")) {
                original = o;
                break;
            }
        }
        if (original == null) {
            System.out.print(ROSE+"Order " + cid + " not found in cancelled list.\n"+RESET);
            return;
        }
        // Use handleReorder logic to attempt the order again (with same items)
        Order retryOrder = new Order();
        retryOrder.orderId = dp.generateOrderId();
        retryOrder.date = currentDateString();
        retryOrder.address = original.address;
        retryOrder.paymentMode = original.paymentMode.equals("") ? "COD" : original.paymentMode;
        for (int j = 0; j < original.itemCount; j++) {
            Item it = original.items[j];
            if (it == null) continue;
            retryOrder.addItem(new Item(it.productId, it.quantity));
        }
        boolean success = processPendingOrder(retryOrder, console);
        dp.orders[dp.orderCount++] = retryOrder;
        if (success) {
            System.out.print(MINT+"Order " + retryOrder.orderId + " reprocessed successfully (Status: " + retryOrder.status + ").\n"+RESET);
            log.write(retryOrder.orderId, "Retry successful for " + cid);
        } else {
            System.out.print(ROSE+"Retry order failed (" + retryOrder.cancelReason + "). New Order ID: " + retryOrder.orderId + "\n"+RESET);
        }
    }

    /** Feature 12: Archive delivered orders older than N days (moves them to archive_orders.txt and removes from active list) */
    private void archiveDeliveredOrders(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Archive delivered orders older than how many days? "+RESET);
        String daysStr = console.readLine();
        if (daysStr == null) daysStr = "";
        daysStr = daysStr.trim();
        int N = DataPersistence.toInt(daysStr);
        if (N <= 0) {
            System.out.print(ROSE+"Invalid number of days.\n"+RESET);
            return;
        }
        String todayStr = currentDateString();
        // Convert date to a simple numeric day count (approximate)
        int todayCount = dateToDayCount(todayStr);
        FileWriter fw = new FileWriter(dp.path("archive_orders.txt"), true);
        int archivedCount = 0;
        // Use a new array to store remaining orders after archiving
        Order[] remaining = new Order[dp.orders.length];
        int remCount = 0;
        for (int i = 0; i < dp.orderCount; i++) {
            Order o = dp.orders[i];
            if (o == null) continue;
            if (o.status.equals("DELIVERED")) {
                // Calculate age in days
                int orderDayCount = dateToDayCount(o.date);
                int age = todayCount - orderDayCount;
                if (age > N) {
                    // Write order record to archive file
                    fw.write(o.toRecord() + "\n");
                    archivedCount++;
                    // Skip adding it to remaining active orders (effectively removing it)
                    log.write(o.orderId, "Archived after delivery (age " + age + " days)");
                    continue;
                }
            }
            // Keep order in the remaining list if not archived
            remaining[remCount++] = o;
        }
        fw.close();
        // Replace the active orders list with the remaining orders
        dp.orders = remaining;
        dp.orderCount = remCount;
        System.out.print(MINT+"Archived " + archivedCount + " delivered orders (older than " + N + " days).\n"+RESET);
    }

    /** Feature 20: Change password for the currently logged-in admin account */
    private void changeAdminPassword(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Enter current password: "+RESET);
        String currentPass = console.readLine();
        if (currentPass == null) currentPass = "";
        currentPass = currentPass.trim();
        Admin admin = dp.admins[dp.currentAdminIndex];
        if (!admin.passHash.equals(Admin.hashPassword(currentPass))) {
            System.out.print(ROSE+"Current password is incorrect.\n"+RESET);
            return;
        }
        System.out.print(SOFTGRAY+"Enter new password: "+RESET);
        String newPass1 = console.readLine();
        if (newPass1 == null) newPass1 = "";
        newPass1 = newPass1.trim();
        System.out.print(SOFTGRAY+"Confirm new password: "+RESET);
        String newPass2 = console.readLine();
        if (newPass2 == null) newPass2 = "";
        newPass2 = newPass2.trim();
        if (!newPass1.equals(newPass2) || newPass1.equals("")) {
            System.out.print(ROSE+"Password mismatch or empty. Password not changed.\n"+RESET);
            return;
        }
        // Update password hash and save to file immediately
        admin.passHash = Admin.hashPassword(newPass1);
        dp.saveAll();
        log.write("ADMIN", "Password changed");
        System.out.print(MINT+"Admin password changed successfully.\n"+RESET);
    }

    /** Feature 14: Clear all logs (logs.txt) after confirmation */
    private void clearLogs(BufferedReader console) throws Exception {
        System.out.print(ANSI_Yellow+"Are you sure you want to clear all logs? (Y/N): "+RESET);
        String confirm = console.readLine();
        if (confirm == null) confirm = "";
        confirm = confirm.trim();
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.print(ROSE+"Log clearance cancelled.\n"+RESET);
            return;
        }
        // Overwrite logs.txt with nothing
        FileWriter fw = new FileWriter(dp.path("logs.txt"), false);
        fw.write("");
        fw.close();
        System.out.print(MINT+"All logs cleared.\n"+RESET);
    }

    /** Feature 16: Generate a receipt text file for a delivered order */
    private void generateReceipt(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Enter Order ID for receipt: "+RESET);
        String rid = console.readLine();
        if (rid == null) rid = "";
        rid = rid.trim();
        if (rid.equals("")) {
            System.out.print(ROSE+"Order ID cannot be empty.\n"+RESET);
            return;
        }
        rid = normalizeOrderId(rid);
        Order order = null;
        for (int i = 0; i < dp.orderCount; i++) {
            if (dp.orders[i] != null && dp.orders[i].orderId.equalsIgnoreCase(rid)) {
                order = dp.orders[i];
                break;
            }
        }
        if (order == null) {
            System.out.print(ROSE+"Order " + rid + " not found.\n"+RESET);
            return;
        }
        if (!order.status.equals("DELIVERED")) {
            System.out.print(SOFTGRAY+"Receipt can only be generated for delivered orders.\n"+RESET);
            return;
        }
        // Create receipt file with order details
        String filename = "receipt_" + order.orderId + ".txt";
        FileWriter fw = new FileWriter(dp.path(filename), false);
        fw.write("Receipt for Order " + order.orderId + "\n");
        fw.write("Address: " + (order.address.equals("") ? "(Not Provided)" : order.address) + "\n");
        fw.write("Status: " + order.status + "\n");
        if (order.trackingId != null && !order.trackingId.equals("")) {
            fw.write("Tracking ID: " + order.trackingId + "\n");
        } else {
            if (order.status.equalsIgnoreCase("SHIPPED") || order.status.equalsIgnoreCase("OUT_FOR_DELIVERY")) {
                fw.write("Tracking ID: (In transit)\n");
            } else {
                fw.write("Tracking ID: (Unavailable)\n");
            }
        }
        fw.write("Items:\n");
        for (int j = 0; j < order.itemCount; j++) {
            Item it = order.items[j];
            if (it == null) continue;
            Product p = dp.findProductById(it.productId);
            String itemName = (p != null ? p.name : it.productId);
            int priceEach = (p != null ? p.price : 0);
            fw.write("- " + itemName + " (x" + it.quantity + " @ BDT " + priceEach + " each)\n");
        }
        fw.write("--------------------------------------\n");
        fw.write("Total Paid: BDT " + order.totalAmount + "\n");
        fw.write(MINT+"Thank you for your purchase!\n"+RESET);
        fw.close();
        System.out.print(SOFTGRAY+"Receipt generated: " + filename + "\n"+RESET);
    }

    /** Feature 14: Increase stock of an existing product (restock) */
    private void handleRestock(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Enter Product ID to restock: "+RESET);
        String pid = console.readLine();
        if (pid == null) pid = "";
        pid = pid.trim();
        if (pid.equals("")) {
            System.out.print(ROSE+"Product ID cannot be empty.\n"+RESET);
            return;
        }
        Product product = dp.findProductById(pid);
        if (product == null) {
            System.out.print(ROSE+"Product " + pid + " not found.\n"+RESET);
            return;
        }
        System.out.print(SOFTGRAY+"Enter quantity to add: "+RESET);
        String qtyStr = console.readLine();
        if (qtyStr == null) qtyStr = "";
        qtyStr = qtyStr.trim();
        int addQty = DataPersistence.toInt(qtyStr);
        if (addQty <= 0) {
            System.out.print(ROSE+"Invalid quantity.\n"+RESET);
            return;
        }
        product.stock += addQty;
        System.out.print(MINT+"Product " + product.productId + " restocked. New stock: " + product.stock + "\n"+RESET);
        dp.saveProducts();
        log.write("ADMIN", "Restocked " + product.productId + " (+" + addQty + ")");
    }

    /** Feature 23: Manage products (Add, Edit, Delete products) */
    private void handleProductManagement(BufferedReader console) throws Exception {
        System.out.print(SOFTGRAY+"Choose action - [A]dd, [E]dit, [D]elete: "+RESET);
        String action = console.readLine();
        if (action == null) action = "";
        action = action.trim().toUpperCase();
        if (action.equals("A")) {
            // Add new product
            if (dp.productCount >= dp.products.length) {
                System.out.print(ROSE+"Product list is full, cannot add more products.\n"+RESET);
                return;
            }
            System.out.print(SOFTGRAY+"Enter new Product ID: "+RESET);
            String newId = console.readLine();
            if (newId == null) newId = "";
            newId = newId.trim();
            if (newId.equals("")) {
                System.out.print(ROSE+"Product ID cannot be empty.\n"+RESET);
                return;
            }
            // Check for uniqueness
            if (dp.findProductById(newId) != null) {
                System.out.print(MINT+"Product ID " + newId + " already exists.\n"+RESET);
                return;
            }
            System.out.print(SOFTGRAY+"Enter Category: "+RESET);
            String category = console.readLine();
            if (category == null) category = "";
            category = category.trim();
            System.out.print(SOFTGRAY+"Enter Brand: "+RESET);
            String brand = console.readLine();
            if (brand == null) brand = "";
            brand = brand.trim();
            System.out.print(SOFTGRAY+"Enter Product Name: "+RESET);
            String name = console.readLine();
            if (name == null) name = "";
            name = name.trim();
            System.out.print(SOFTGRAY+"Enter Price: "+RESET);
            String priceStr = console.readLine();
            if (priceStr == null) priceStr = "";
            priceStr = priceStr.trim();
            System.out.print(SOFTGRAY+"Enter Initial Stock: "+RESET);
            String stockStr = console.readLine();
            if (stockStr == null) stockStr = "";
            stockStr = stockStr.trim();
            if (newId.equals("") || category.equals("") || brand.equals("") || name.equals("")) {
                System.out.print(ROSE+"Fields cannot be empty. Product not added.\n"+RESET);
                return;
            }
            int price = DataPersistence.toInt(priceStr);
            int stock = DataPersistence.toInt(stockStr);
            dp.products[dp.productCount++] = new Product(newId, category, brand, name, price, stock);
            System.out.print(MINT+"Product " + newId + " added successfully.\n"+RESET);
            log.write("ADMIN", "Added product " + newId);
        } else if (action.equals("E")) {
            // Edit existing product
            System.out.print(SOFTGRAY+"Enter Product ID to edit: "+RESET);
            String editId = console.readLine();
            if (editId == null) editId = "";
            editId = editId.trim();
            if (editId.equals("")) {
                System.out.print(ROSE+"Product ID cannot be empty.\n"+RESET);
                return;
            }
            Product product = dp.findProductById(editId);
            if (product == null) {
                System.out.print(ROSE+"Product " + editId + " not found.\n"+RESET);
                return;
            }
            System.out.print(SOFTGRAY+"Edit field - [N]ame, [P]rice, [S]tock: "+RESET);
            String field = console.readLine();
            if (field == null) field = "";
            field = field.trim().toUpperCase();
            if (field.equals("N")) {
                System.out.print(SOFTGRAY+"Enter new Name: "+RESET);
                String newName = console.readLine();
                if (newName == null) newName = "";
                newName = newName.trim();
                if (!newName.equals("")) {
                    product.name = newName;
                    System.out.print(MINT+"Product " + product.productId + " name updated.\n"+RESET);
                    log.write("ADMIN", "Edited product " + product.productId + " (Name changed)");
                }
            } else if (field.equals("P")) {
                System.out.print(SOFTGRAY+"Enter new Price: "+RESET);
                String newPriceStr = console.readLine();
                if (newPriceStr == null) newPriceStr = "";
                newPriceStr = newPriceStr.trim();
                int newPrice = DataPersistence.toInt(newPriceStr);
                if (newPrice > 0) {
                    product.price = newPrice;
                    System.out.print(MINT+"Product " + product.productId + " price updated.\n"+RESET);
                    log.write("ADMIN", "Edited product " + product.productId + " (Price changed)");
                }
            } else if (field.equals("S")) {
                System.out.print(SOFTGRAY+"Enter new Stock value: "+RESET);
                String newStockStr = console.readLine();
                if (newStockStr == null) newStockStr = "";
                newStockStr = newStockStr.trim();
                int newStock = DataPersistence.toInt(newStockStr);
                if (newStock >= 0) {
                    product.stock = newStock;
                    System.out.print(MINT+"Product " + product.productId + " stock updated.\n"+RESET);
                    log.write("ADMIN", "Edited product " + product.productId + " (Stock adjusted)");
                }
            } else {
                System.out.print(ROSE+"Invalid field selection.\n"+RESET);
            }
        } else if (action.equals("D")) {
            // Delete a product
            System.out.print(SOFTGRAY+"Enter Product ID to delete: "+RESET);
            String delId = console.readLine();
            if (delId == null) delId = "";
            delId = delId.trim();
            if (delId.equals("")) {
                System.out.print(ROSE+"Product ID cannot be empty.\n"+RESET);
                return;
            }
            // Find index of product
            int idx = -1;
            for (int i = 0; i < dp.productCount; i++) {
                if (dp.products[i] != null && dp.products[i].productId.equals(delId)) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                System.out.print(ROSE+"Product " + delId + " not found.\n"+RESET);
                return;
            }
            // Confirm deletion
            System.out.print(ANSI_Yellow+"Are you sure you want to delete " + delId + "? (Y/N): "+RESET);
            String conf = console.readLine();
            if (conf == null) conf = "";
            conf = conf.trim().toUpperCase();
            if (!conf.equals("Y") && !conf.equals("YES")) {
                System.out.print(ROSE+"Deletion cancelled.\n"+RESET);
                return;
            }
            // Remove product by shifting array
            for (int j = idx; j < dp.productCount - 1; j++) {
                dp.products[j] = dp.products[j+1];
            }
            dp.products[dp.productCount - 1] = null;
            dp.productCount--;
            System.out.print(MINT+"Product " + delId + " deleted.\n"+RESET);
            log.write("ADMIN", "Deleted product " + delId);
        } else {
            System.out.print(ROSE+"Invalid action.\n"+RESET);
        }
    }

 /** Process a PENDING order through inventory check, reservation, invoice generation, and payment simulation */
private boolean processPendingOrder(Order order, BufferedReader console) throws Exception {
    if (order == null || !order.status.equals("PENDING")) return false;

    boolean inventoryOK = true;

    // Step 1: Pre-check all items without modifying stock
    for (int i = 0; i < order.itemCount; i++) {
        Item it = order.items[i];
        if (it == null) continue;
        Product prod = dp.findProductById(it.productId);
        if (prod == null) {
            order.status = "CANCELLED";
            order.cancelReason = "Invalid product " + it.productId;
            log.write(order.orderId, "Order cancelled - " + order.cancelReason);
            return false;
        }
        if (prod.stock < it.quantity) {
            inventoryOK = false;
            order.cancelReason = "Inventory Shortage: " + it.productId;
            break;
        }
    }

    if (!inventoryOK) {
        order.status = "CANCELLED";
        log.write(order.orderId, "Order cancelled -" + order.cancelReason);
        return false;
    }

    // Step 2: Reserve stock
    for (int i = 0; i < order.itemCount; i++) {
        Item it = order.items[i];
        Product prod = dp.findProductById(it.productId);
        if (prod != null) {
            prod.stock -= it.quantity;
        }
    }
    log.write(order.orderId, "Inventory OK – stock reserved");

    // Step 3: Calculate total price
    int total = 0;
    for (int i = 0; i < order.itemCount; i++) {
        Item it = order.items[i];
        Product prod = dp.findProductById(it.productId);
        int price = (prod != null ? prod.price : 0);
        total += price * it.quantity;
    }
    order.totalAmount = total;

    // Step 4: Simulate payment
    boolean paymentSuccess;
    if (console == null) {
        paymentSuccess = order.paymentMode.equalsIgnoreCase("COD");
        if (order.paymentMode.equalsIgnoreCase("COD")) {
        log.write(order.orderId, "PAYMENT OK (COD)");
       } else {
        log.write(order.orderId, "PAYMENT FAIL (Auto decline for simulation)");
        paymentSuccess = false;
    }
    } else {
        paymentSuccess = paymentService.processPayment(order, console);
    }

    // Step 5: Rollback stock if payment fails
    if (!paymentSuccess) {
        for (int i = 0; i < order.itemCount; i++) {
            Item it = order.items[i];
            Product prod = dp.findProductById(it.productId);
            if (prod != null) {
                prod.stock += it.quantity;
            }
        }
        order.status = "CANCELLED";
        order.cancelReason = "Payment Declined";
        log.write(order.orderId, "Order cancelled - " + order.cancelReason);
        return false;
    }

    // Step 6: Mark as PACKED and generate properly formatted invoice
    order.status = "PACKED";
    log.write(order.orderId, "Status changed to PACKED");

    try {
        // ✅ Format: INV-YYYYMM-####
        String ym = order.date.substring(0, 7).replace("-", ""); // "202602"
        String orderNum = order.orderId.substring(1); // drop 'O' → "1005"
        String invoiceId = "INV-" + ym + "-" + orderNum;

        FileWriter fw = new FileWriter(dp.path("invoices.txt"), true);
        fw.write(invoiceId + "|BDT " + order.totalAmount + "\n");
        fw.close();

        // (Optional) Show invoice ID to admin
        System.out.print("Invoice generated: " + invoiceId + "\n");
    } catch (Exception e) {
        // Ignore invoice errors silently
    }

    return true;
}

    /** View detailed information of an order (internal helper) */
    private void viewOrderDetails(Order order) {
        System.out.print(LAVENDER+"Order ID: " + order.orderId + "\n"+RESET);
        System.out.print(LAVENDER+"Date: " + order.date + "\n"+RESET);
        System.out.print(LAVENDER+"Status: " + order.status + "\n"+RESET);
        if (order.status.equalsIgnoreCase("CANCELLED")) {
            System.out.print(ANSI_DUSTY_ROSE+"Cancel Reason: " + (order.cancelReason.equals("") ? "(None)" : order.cancelReason) + "\n"+RESET);
        }
        if (order.trackingId != null && !order.trackingId.equals("")) {
            System.out.print(LAVENDER+"Tracking ID: " + order.trackingId + "\n"+RESET);
        }
        System.out.print(LAVENDER+"Address: " + (order.address.equals("") ? "(Not provided)" : order.address) + "\n"+RESET);
        System.out.print(LAVENDER+"Payment Mode: " + (order.paymentMode.equals("") ? "(N/A)" : order.paymentMode) + "\n"+RESET);
        System.out.print(LAVENDER+"Total Amount: BDT " + order.totalAmount + "\n"+RESET);
        System.out.print("Items:\n");
        for (int i = 0; i < order.itemCount; i++) {
            Item it = order.items[i];
            if (it == null) continue;
            Product p = dp.findProductById(it.productId);
            String itemName = (p != null ? p.name : it.productId);
            System.out.print(LAVENDER+"- " + itemName + " (x" + it.quantity + ")\n"+RESET);
        }
    }

    /** Feature 17: Generate a report of revenue and cancellations, write to report.txt */
    private void generateReport() throws Exception {
        int totalOrders = dp.orderCount;
        int completedCount = 0;
        int cancelledCount = 0;
        int revenueSum = 0;
        // Count cancellation reasons
        String[] reasons = new String[totalOrders];
        int[] reasonCounts = new int[totalOrders];
        int reasonTypes = 0;
        for (int i = 0; i < dp.orderCount; i++) {
            Order o = dp.orders[i];
            if (o == null) continue;
            if (o.status.equalsIgnoreCase("DELIVERED")) {
                completedCount++;
                revenueSum += o.totalAmount;
            }
            if (o.status.equalsIgnoreCase("CANCELLED")) {
                cancelledCount++;
                String reason = (o.cancelReason == null || o.cancelReason.equals("") ? "Unknown" : o.cancelReason);
                // Increment count for this reason
                boolean found = false;
                for (int r = 0; r < reasonTypes; r++) {
                    if (reasons[r].equalsIgnoreCase(reason)) {
                        reasonCounts[r]++;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    reasons[reasonTypes] = reason;
                    reasonCounts[reasonTypes] = 1;
                    reasonTypes++;
                }
            }
        }
        // Sort cancellation reasons by frequency (descending)
        for (int i = 0; i < reasonTypes - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < reasonTypes; j++) {
                if (reasonCounts[j] > reasonCounts[maxIndex]) {
                    maxIndex = j;
                }
            }
            // swap
            String tempReason = reasons[i];
            reasons[i] = reasons[maxIndex];
            reasons[maxIndex] = tempReason;
            int tempCount = reasonCounts[i];
            reasonCounts[i] = reasonCounts[maxIndex];
            reasonCounts[maxIndex] = tempCount;
        }
        // Build report content
        StringBuilder report = new StringBuilder();
        report.append("Total Orders: ").append(totalOrders).append("\n");
        report.append("Completed Orders: ").append(completedCount).append("\n");
        report.append("Cancelled Orders: ").append(cancelledCount).append("\n");
        report.append("Total Revenue: BDT ").append(revenueSum).append("\n");
        report.append("Top 3 Cancellation Reasons:\n");
        for (int k = 0; k < reasonTypes && k < 3; k++) {
            report.append((k + 1) + ". " + reasons[k] + " – " + reasonCounts[k] + "\n");
        }
        // Write report to file and display summary in console
        FileWriter fw = new FileWriter(dp.path("report.txt"), false);
        fw.write(report.toString());
        fw.close();
        System.out.print("=== Report Summary ===\n");
        System.out.print(report.toString());
        System.out.print(MINT+"(Full report saved to report.txt)\n"+RESET);
    }


    /** Helper: normalize input to full Order ID format (e.g., add 'O' prefix if missing) */
  private String normalizeOrderId(String id) {
    id = id.trim().toUpperCase();
    if (!id.startsWith("O")) {
        // Remove leading zeros from numeric part
        id = id.replaceFirst("^0+(?!$)", "");  // "01006" becomes "1006"
        id = "O" + id;
    }
    return id;
}

    /** Helper: check if a string is numeric */
    private boolean isNumeric(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return s.length() > 0;
    }

    /** Helper: get current date as YYYY-MM-DD */
    private String currentDateString() {
        // Use system date for realism
        LocalDate today = LocalDate.now();
        return today.toString();
    }

    /** Helper: convert a YYYY-MM-DD date string to an approximate day count for comparison */
    private int dateToDayCount(String dateStr) {
        if (dateStr == null || dateStr.length() == 0) return 0;
        String[] parts = dateStr.split("-");
        if (parts.length < 3) return 0;
        int y = DataPersistence.toInt(parts[0]);
        int m = DataPersistence.toInt(parts[1]);
        int d = DataPersistence.toInt(parts[2]);
        // approximate: year*360 + month*30 + day
        return y * 360 + m * 30 + d;
    }

    /** 
 * Accept a new order from the admin by manually inputting order details.
 * This will generate a new Order ID, collect product selections, and process the order.
 */
private void acceptNewOrder(BufferedReader console) throws Exception {
    // 1. Auto-generate Order ID and initialize a new Order
    String newId = dp.generateOrderId();
    Order newOrder = new Order();
    newOrder.orderId = newId;
    newOrder.date = currentDateString();  // set current date (YYYY-MM-DD)
    System.out.print(LAVENDER+"New Order ID: " + newOrder.orderId + "\n"+RESET);

    // 2. Display product catalog (Product ID, Name, Stock)
    printTitle("Product Catalog");
    for (int i = 0; i < dp.productCount; i++) {
        Product prod = dp.products[i];
        if (prod == null) continue;
        System.out.print(LAVENDER+prod.productId + " - " + prod.name + " (Stock: " + prod.stock + ")\n"+RESET);
    }
    printLine();
    // 3. Allow admin to select 1–3 products and specify quantities
    System.out.print(SOFTGRAY+"How many different products in this order? (1-10): "+RESET);
    String countStr = console.readLine();
    if (countStr == null) countStr = "";
    countStr = countStr.trim();
    int itemCount = DataPersistence.toInt(countStr);
    if (itemCount < 1 || itemCount > 10) {
        System.out.print(ROSE+"Invalid number of products. Order cancelled.\n"+RESET);
        return;
    }
    for (int i = 1; i <= itemCount; i++) {
        System.out.print(SOFTGRAY+"Enter Product ID for item " + i + ": "+RESET);
        String pid = console.readLine();
        if (pid == null) pid = "";
        pid = pid.trim();
        if (pid.equals("")) {
            System.out.print(ROSE+"Product ID cannot be empty. Order cancelled.\n"+RESET);
            return;
        }
        Product product = dp.findProductById(pid);
        if (product == null) {
            System.out.print(ROSE+"Product " + pid + " not found. Order cancelled.\n"+RESET);
            return;
        }
        System.out.print(SOFTGRAY+"Enter quantity for " + product.name + ": "+RESET);
        String qtyStr = console.readLine();
        if (qtyStr == null) qtyStr = "";
        qtyStr = qtyStr.trim();
        int qty = DataPersistence.toInt(qtyStr);
        if (qty <= 0) {
            System.out.print(ROSE+"Invalid quantity. Order cancelled.\n"+RESET);
            return;
        }
        // Add the selected item to the order
        if (!newOrder.addItem(new Item(product.productId, qty))) {
            System.out.print(ROSE+"Failed to add item " + product.productId + ". Order cancelled.\n"+RESET);
            return;
        }
    }

    // 4. Ask for shipping address and payment mode
    System.out.print(SOFTGRAY+"Enter shipping address: "+RESET);
    String address = console.readLine();
    if (address == null) address = "";
    address = address.trim();
    if (address.equals("")) {
        System.out.print(ROSE+"Address cannot be empty. Order cancelled.\n"+RESET);
        return;
    }
    newOrder.address = address;
    System.out.print(SOFTGRAY+"Enter payment mode (COD or MockCard): "+RESET);
    String paymentMode = console.readLine();
    if (paymentMode == null) paymentMode = "";
    paymentMode = paymentMode.trim();
    if (paymentMode.equalsIgnoreCase("")) {
        System.out.print(ROSE+"Payment mode cannot be empty. Order cancelled.\n"+RESET);
        return;
    }
    newOrder.paymentMode = paymentMode;  // e.g., "COD" or "MockCard"

    // 5. Log the order creation and process the order through existing workflow
    log.write(newOrder.orderId, "Order created via admin interface (pending)");  // Log creation event
    boolean processed = processPendingOrder(newOrder, console);
    // (processPendingOrder will handle inventory check, payment processing, and update order status)

    // 6. Add the new order to system records
    dp.orders[dp.orderCount++] = newOrder;

    // 7. Output result and log outcome
    if (!processed) {
        // If processing failed, the order status is now "CANCELLED" (cancelReason set by processPendingOrder)
        System.out.print(ROSE+"Order processing failed. Order ID: " + newOrder.orderId  + " is CANCELLED (" + newOrder.cancelReason + ").\n"+RESET);
        // (The cancellation reason and status change have been logged by processPendingOrder)
    } else {
        // If processing succeeded, the order status is now "PACKED"
        System.out.print(MINT+"New order accepted and processed successfully! New Order ID: " + newOrder.orderId + " (Status: " + newOrder.status + ").\n"+RESET);
        // (Inventory reservation and payment confirmation have been logged, and status set to PACKED)
    }
}

   }

