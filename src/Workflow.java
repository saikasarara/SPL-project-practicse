import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

/** Workflow.java – Orchestrates order processing and provides the Admin Dashboard menu */
public class Workflow {
    // ANSI color codes for CLI output
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET  = "\u001B[0m";

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

    /** Admin Dashboard menu loop handling all features */
    public void adminDashboard(BufferedReader console) throws Exception {
        System.out.print("\n===== Admin Dashboard =====\n");
        while (true) {
            // Display menu options
            System.out.print("\nMenu:\n");
            System.out.print("1.Accept New Order\n");
            System.out.print("2.Update Order Status\n");
            System.out.print("3.View Order Logs\n");
            System.out.print("4.Search/Filter Orders\n");
            System.out.print("5.Generate Receipt\n");
           
           
            System.out.print("6.Advanced Product Filter\n");
            System.out.print("7.Manage Products (Add/Edit/Delete)\n");
            System.out.print("8.Low Stock Alerts\n");
            System.out.print("9.Restock Product\n");
            System.out.print("10.Export Stock Report\n");
            System.out.print("11.Bulk Import Orders\n");
            

            System.out.print("12.Archive Delivered Orders\n");
            System.out.print("13.Reorder Previous Order\n");
            System.out.print("14.Retry Failed Order\n");
            
            System.out.print("15.Clear Logs\n");
            System.out.print("16.Change Admin Password\n");
            
            
            System.out.print("17.Generate Report\n");
            System.out.print("18.Simulation Mode\n");
            System.out.print("19.Load Test Data\n");
            
            System.out.print("0. Exit\n");
            System.out.print("Choose an option: ");
            String choice = console.readLine();
            if (choice == null) choice = "";
            choice = choice.trim();
            System.out.print("\n");
            switch (choice) {
                case "1":  // New case for Accept New Order
                acceptNewOrder(console);
                break;
                case "2": handleStatusUpdate(console); break;
                case "3":
             System.out.println("==== Available Orders (Sorted by Date) ====");
    
             // Sort orders by Date (ascending)
             Order[] sortedOrders = Arrays.copyOf(dp.orders, dp.orderCount);
             Arrays.sort(sortedOrders, Comparator.comparing(o -> o.date));  // Sort by Date

             // Display the orders with Order ID and Date
             for (Order order : sortedOrders) {
             if (order != null) {
             System.out.println(order.orderId + " | Date: " + order.date + " | Status: " + order.status);
             }
         }
    
             // Ask the admin to enter an Order ID to view logs
              System.out.print("Enter Order ID to view logs: ");
              String logId = console.readLine();
             if (logId != null && !logId.trim().equals("")) {
             logId = normalizeOrderId(logId.trim());
             log.viewLogsByOrder(logId);  // View logs for the selected Order ID
         }
            break;

                case "4": handleOrderSearch(console); break;
                case "5": generateReceipt(console); break;
                case "6": handleAdvancedFilter(console); break;
                case "7": handleProductManagement(console); break;
                case "8": showLowStockAlerts(); break;
                case "9": handleRestock(console); break;
                case "10": exportStockReport(); break;
                case "11": importOrdersFromFile(console); break;
                case "12": archiveDeliveredOrders(console); break;
                case "13": handleReorder(console); break;
                case "14": retryCancelledOrder(console);break;
                case "15": clearLogs(console); break;
                case "16": changeAdminPassword(console); break;
                case "17": generateReport(); break;
                case "18": runSimulation(console); break;
                case "19":
                    System.out.print("Enter test data filename (e.g. testdata.txt): ");
                    String file = console.readLine();
                    if (file == null) file = "";
                    file = file.trim();
                    if (!file.equals("")) {
                        dp.loadTestDataFromFile(file);
                        dp.saveAll(); 
                        System.out.print("-> " + dp.productCount + " products loaded.\n");
                        System.out.print("-> " + dp.orderCount + " orders loaded.\n");
                        System.out.print("-> " + dp.adminCount + " admins loaded.\n");

                    }
                    break;
                
                case "0":
                    System.out.print("Exiting Admin Dashboard...\n");
                    return;
                default:
                    System.out.print("Invalid option. Please try again.\n");
                    break;
            }
            System.out.print("\n--------------------------------\n");
        }
    }

private void handleOrderSearch(BufferedReader console) throws Exception {
    System.out.print("Enter Order ID or Status to search (or press Enter for advanced filter): ");
    String query = console.readLine();
    if (query == null) query = "";
    query = query.trim();
    if (query.equals("")) {
        // Advanced filtering by multiple criteria
        System.out.print("Enter Status to filter (or press Enter for any): ");
        String statusFilter = console.readLine();
        if (statusFilter == null) statusFilter = "";
        statusFilter = statusFilter.trim();
        System.out.print("Enter Payment Mode to filter (or press Enter for any): ");
        String paymentFilter = console.readLine();
        if (paymentFilter == null) paymentFilter = "";
        paymentFilter = paymentFilter.trim();
        System.out.print("Enter Date to filter (YYYY-MM-DD, or press Enter for any): ");
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
            System.out.print("No orders found matching the given criteria.\n");
        } else {
            // Display summary of orders that matched all filters
            String statusCrit = statusFilter.equals("") ? "Any" : statusFilter;
            String payCrit = paymentFilter.equals("") ? "Any" : paymentFilter;
            String dateCrit = dateFilter.equals("") ? "Any" : dateFilter;
            System.out.print("Orders matching filters - Status: " + statusCrit 
                               + ", Payment: " + payCrit + ", Date: " + dateCrit + ":\n");
            for (int i = 0; i < count; i++) {
                Order o = results[i];
                // Prepare status string (with color coding for output if available)
                String statusStr = o.status;
                if (statusStr.equals("DELIVERED")) {
                    statusStr = ANSI_GREEN + statusStr + ANSI_RESET;
                } else if (statusStr.equals("CANCELLED")) {
                    statusStr = ANSI_RED + statusStr + ANSI_RESET;
                }
                // Print order summary line with relevant details
                System.out.print("- " + o.orderId + " | Date: " + o.date 
                                 + " | Payment: " + o.paymentMode 
                                 + " | Status: " + statusStr 
                                 + " | Total: BDT " + o.totalAmount);
                if (o.status.equals("CANCELLED") && o.cancelReason != null && !o.cancelReason.equals("")) {
                    System.out.print(" | CancelReason: " + o.cancelReason);
                }
                System.out.print("\n");
            }
            // Optionally allow viewing details of one order from the results
            System.out.print("Enter Order ID to view details (or press Enter to skip): ");
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
                    System.out.print("Order " + selId + " not found in results.\n");
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
            System.out.print("No orders found matching \"" + query + "\".\n");
        } else {
            System.out.print("Orders with status containing \"" + query + "\":\n");
            for (int i = 0; i < count; i++) {
                Order o = results[i];
                String statusStr = o.status;
                if (statusStr.equals("DELIVERED")) {
                    statusStr = ANSI_GREEN + statusStr + ANSI_RESET;
                } else if (statusStr.equals("CANCELLED")) {
                    statusStr = ANSI_RED + statusStr + ANSI_RESET;
                }
                System.out.print("- " + o.orderId + " | Status: " + statusStr 
                                 + " | Total: BDT " + o.totalAmount);
                if (o.cancelReason != null && !o.cancelReason.equals("")) {
                    System.out.print(" | CancelReason: " + o.cancelReason);
                }
                System.out.print("\n");
            }
            // Allow viewing details of a selected order from the list
            System.out.print("Enter Order ID to view details (or press Enter to skip): ");
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
                    System.out.print("Order " + selId + " not found in results.\n");
                }
            }
        }
    }
}

    /** Feature 6: Manually progress an order status through the workflow (PENDING -> PACKED -> SHIPPED -> OUT_FOR_DELIVERY -> DELIVERED) */
    private void handleStatusUpdate(BufferedReader console) throws Exception {
    System.out.print("Enter Order ID to update status: ");
    String id = console.readLine();
    if (id == null) id = "";
    id = id.trim();
    if (id.equals("")) {
        System.out.print("Order ID cannot be empty.\n");
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
        System.out.print("Order " + id + " not found.\n");
        return;
    }
    String currentStatus = order.status;
    // If order already delivered or cancelled, no further updates allowed
    if (currentStatus.equals("DELIVERED") || currentStatus.equals("CANCELLED")) {
        System.out.print("Order " + id + " is " + currentStatus + "; status cannot be changed.\n");
        return;
    }
    // If order is PENDING, attempt to process it (inventory check & payment)
    if (currentStatus.equals("PENDING")) {
        boolean processed = processPendingOrder(order, console);
        if (!processed) {
            // If processing failed, order status is now CANCELLED (reason set in processPendingOrder)
            System.out.print("Order processing failed. Status updated to CANCELLED (" 
                             + order.cancelReason + ").\n");
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
    System.out.print("Order " + order.orderId + " status updated to " + nextStatus + ".\n");
}


    /** Feature 5 & 8: Reorder a previous order (copy its items into a new order and process it) */
    private void handleReorder(BufferedReader console) throws Exception {
        System.out.print("Enter Order ID to reorder: ");
        String oldId = console.readLine();
        if (oldId == null) oldId = "";
        oldId = oldId.trim();
        if (oldId.equals("")) {
            System.out.print("Order ID cannot be empty.\n");
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
            System.out.print("Order " + oldId + " not found.\n");
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
            System.out.print("Reorder created as " + newOrder.orderId + " but failed (" + newOrder.cancelReason + ").\n");
        } else {
            System.out.print("Reorder successful! New Order ID: " + newOrder.orderId + " (Status: " + newOrder.status + ").\n");
            log.write(newOrder.orderId, "Reordered from " + oldId);
        }
    }

    /** Feature 6 (continued): View or filter products by brand or category */
    private void handleAdvancedFilter(BufferedReader console) throws Exception {
        System.out.print("Filter by Brand or Category? (B/C): ");
        String choice = console.readLine();
        if (choice == null) choice = "";
        choice = choice.trim().toUpperCase();
        if (!choice.equals("B") && !choice.equals("C")) {
            System.out.print("Invalid choice. Enter 'B' for Brand or 'C' for Category.\n");
            return;
        }
        System.out.print("Enter " + (choice.equals("B") ? "Brand" : "Category") + " name: ");
        String keyword = console.readLine();
        if (keyword == null) keyword = "";
        keyword = keyword.trim();
        if (keyword.equals("")) {
            System.out.print("Input cannot be empty.\n");
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
            System.out.print("No products found for \"" + keyword + "\".\n");
        } else {
            // Simply list the filtered products (ProductID | Name | Price | Stock)
            System.out.print("Filtered products (" + (choice.equals("B") ? "Brand" : "Category") + " contains \"" + keyword + "\"):\n");
            for (int i = 0; i < count; i++) {
                Product p = filtered[i];
                System.out.print("- " + p.productId + " | " + p.name + " | BDT " + p.price + " | Stock: " + p.stock + "\n");
            }
        }
    }

    /** Feature 13: Display low stock items (stock < 5) highlighted in color */
    private void showLowStockAlerts() {
        boolean anyLow = false;
        System.out.print("Low Stock Items (stock < 5):\n");
        for (int i = 0; i < dp.productCount; i++) {
            Product p = dp.products[i];
            if (p == null) continue;
            if (p.stock < 5) {
                anyLow = true;
                // Highlight low stock product in yellow
                System.out.print(ANSI_YELLOW + p.productId + " | " + p.name + " | Stock: " + p.stock + ANSI_RESET + "\n");
            }
        }
        if (!anyLow) {
            System.out.print("None (all products have sufficient stock).\n");
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
        System.out.print("Stock report generated in stock_report.txt\n");
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
            System.out.print("Imported: " + o.orderId + "\n");
        }
        } catch (Exception e) {
            System.out.print("Error reading orders_import.txt\n");
        } finally {
            if (br != null) br.close();
        }
        System.out.print(importedCount + " orders imported from orders_import.txt.\n");
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
    System.out.print("Simulation scenarios:\n");
    System.out.print("1. Successful order\n");
    System.out.print("2. Payment failure scenario\n");
    System.out.print("3. Inventory shortage scenario\n");
    System.out.print("4. Random order scenario\n");
    System.out.print("Choose scenario (1-4): ");
    String opt = console.readLine();
    if (opt == null) opt = "";
    opt = opt.trim();
    if (!opt.matches("[1-4]")) {
        System.out.print("Invalid scenario selection.\n");
        return;
    }

    // Create a simulated order
    Order simOrder = new Order();
    simOrder.orderId = dp.generateOrderId();
    simOrder.date = currentDateString();

    // Build order based on scenario choice
    if (opt.equals("2")) {
        // Scenario 2: Payment failure – simulate failure (order will be cancelled)
        Product p = dp.products[0];
        if (p == null) {
            System.out.print("No products available for simulation.\n");
            return;
        }
        simOrder.addItem(new Item(p.productId, 1)); // Add one item
        simOrder.paymentMode = "MockCard"; // Simulate payment failure

        // Mark the order as cancelled due to payment failure
        simOrder.status = "CANCELLED"; // Simulate failure
        simOrder.cancelReason = "Payment Failure (MockCard)";
        // Log the creation of the order and cancellation reason
        log.write(simOrder.orderId, "Simulated order created (Payment Failure)");

    } else if (opt.equals("3")) {
        // Scenario 3: Inventory shortage – order more than available stock
        Product p = null;
        // Find a product with limited stock
        for (int i = 0; i < dp.productCount; i++) {
            if (dp.products[i] != null && dp.products[i].stock > 0 && dp.products[i].stock < 10) {
                p = dp.products[i];
                break;
            }
        }
        if (p == null) {
            p = dp.products[0]; // fallback to the first product if none with low stock is found
        }
        int largeQty = (p.stock == 0 ? 5 : p.stock + 5); // Add more quantity than available
        simOrder.addItem(new Item(p.productId, largeQty));
        simOrder.paymentMode = "COD"; // Cash on delivery

        // Mark the order as cancelled due to inventory shortage
        simOrder.status = "CANCELLED"; // Simulate cancellation
        simOrder.cancelReason = "Inventory Shortage";
        log.write(simOrder.orderId, "Simulated order created (Inventory Shortage)");

    } else {
        // Scenario 1 or 4: Successful or Random order – pick 1-2 random items
        if (dp.productCount == 0) {
            System.out.print("No products available to simulate order.\n");
            return;
        }
        Product p1 = dp.products[0];
        simOrder.addItem(new Item(p1.productId, 1)); // Add one item

        if (opt.equals("4") && dp.productCount > 1) {
            Product p2 = dp.products[1];  // Add another item if it's the random scenario
            simOrder.addItem(new Item(p2.productId, 1));
        }

        simOrder.paymentMode = "COD"; // Simulate COD payment

        // Successful order – set the status as "DELIVERED"
        simOrder.status = "DELIVERED"; // Mark as delivered for successful order
        // Log the creation of the order and status change
        log.write(simOrder.orderId, "Simulated successful order created (DELIVERED)");
    }

    simOrder.address = "SimulatedAddress"; // Assign a sample address

    // Process the simulated order (this handles the order processing flow, including payment and inventory)
    processPendingOrder(simOrder, console);

    // Add the simulated order to system records
    dp.orders[dp.orderCount++] = simOrder;

    // Show the status of the simulated order
    System.out.print("Simulation Order " + simOrder.orderId + " created (Status: " + simOrder.status + ").\n");
    // Log the order creation
    log.write(simOrder.orderId, "Simulation completed for Order: " + simOrder.orderId);
}

    /** Feature 8: Retry processing a failed (cancelled) order by creating a fresh attempt */
    private void retryCancelledOrder(BufferedReader console) throws Exception {
        System.out.print("Enter Cancelled Order ID to retry: ");
        String cid = console.readLine();
        if (cid == null) cid = "";
        cid = cid.trim();
        if (cid.equals("")) {
            System.out.print("Order ID cannot be empty.\n");
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
            System.out.print("Order " + cid + " not found in cancelled list.\n");
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
            System.out.print("Order " + retryOrder.orderId + " reprocessed successfully (Status: " + retryOrder.status + ").\n");
            log.write(retryOrder.orderId, "Retry successful for " + cid);
        } else {
            System.out.print("Retry order failed (" + retryOrder.cancelReason + "). New Order ID: " + retryOrder.orderId + "\n");
        }
    }

    /** Feature 12: Archive delivered orders older than N days (moves them to archive_orders.txt and removes from active list) */
    private void archiveDeliveredOrders(BufferedReader console) throws Exception {
        System.out.print("Archive delivered orders older than how many days? ");
        String daysStr = console.readLine();
        if (daysStr == null) daysStr = "";
        daysStr = daysStr.trim();
        int N = DataPersistence.toInt(daysStr);
        if (N <= 0) {
            System.out.print("Invalid number of days.\n");
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
        System.out.print("Archived " + archivedCount + " delivered orders (older than " + N + " days).\n");
    }

    /** Feature 20: Change password for the currently logged-in admin account */
    private void changeAdminPassword(BufferedReader console) throws Exception {
        System.out.print("Enter current password: ");
        String currentPass = console.readLine();
        if (currentPass == null) currentPass = "";
        currentPass = currentPass.trim();
        Admin admin = dp.admins[dp.currentAdminIndex];
        if (!admin.passHash.equals(Admin.hashPassword(currentPass))) {
            System.out.print("Current password is incorrect.\n");
            return;
        }
        System.out.print("Enter new password: ");
        String newPass1 = console.readLine();
        if (newPass1 == null) newPass1 = "";
        newPass1 = newPass1.trim();
        System.out.print("Confirm new password: ");
        String newPass2 = console.readLine();
        if (newPass2 == null) newPass2 = "";
        newPass2 = newPass2.trim();
        if (!newPass1.equals(newPass2) || newPass1.equals("")) {
            System.out.print("Password mismatch or empty. Password not changed.\n");
            return;
        }
        // Update password hash and save to file immediately
        admin.passHash = Admin.hashPassword(newPass1);
        dp.saveAll();
        log.write("ADMIN", "Password changed");
        System.out.print("Admin password changed successfully.\n");
    }

    /** Feature 14: Clear all logs (logs.txt) after confirmation */
    private void clearLogs(BufferedReader console) throws Exception {
        System.out.print("Are you sure you want to clear all logs? (Y/N): ");
        String confirm = console.readLine();
        if (confirm == null) confirm = "";
        confirm = confirm.trim();
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.print("Log clearance cancelled.\n");
            return;
        }
        // Overwrite logs.txt with nothing
        FileWriter fw = new FileWriter(dp.path("logs.txt"), false);
        fw.write("");
        fw.close();
        System.out.print("All logs cleared.\n");
    }

    /** Feature 16: Generate a receipt text file for a delivered order */
    private void generateReceipt(BufferedReader console) throws Exception {
        System.out.print("Enter Order ID for receipt: ");
        String rid = console.readLine();
        if (rid == null) rid = "";
        rid = rid.trim();
        if (rid.equals("")) {
            System.out.print("Order ID cannot be empty.\n");
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
            System.out.print("Order " + rid + " not found.\n");
            return;
        }
        if (!order.status.equals("DELIVERED")) {
            System.out.print("Receipt can only be generated for delivered orders.\n");
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
        fw.write("Thank you for your purchase!\n");
        fw.close();
        System.out.print("Receipt generated: " + filename + "\n");
    }

    /** Feature 14: Increase stock of an existing product (restock) */
    private void handleRestock(BufferedReader console) throws Exception {
        System.out.print("Enter Product ID to restock: ");
        String pid = console.readLine();
        if (pid == null) pid = "";
        pid = pid.trim();
        if (pid.equals("")) {
            System.out.print("Product ID cannot be empty.\n");
            return;
        }
        Product product = dp.findProductById(pid);
        if (product == null) {
            System.out.print("Product " + pid + " not found.\n");
            return;
        }
        System.out.print("Enter quantity to add: ");
        String qtyStr = console.readLine();
        if (qtyStr == null) qtyStr = "";
        qtyStr = qtyStr.trim();
        int addQty = DataPersistence.toInt(qtyStr);
        if (addQty <= 0) {
            System.out.print("Invalid quantity.\n");
            return;
        }
        product.stock += addQty;
        System.out.print("Product " + product.productId + " restocked. New stock: " + product.stock + "\n");
        log.write("ADMIN", "Restocked " + product.productId + " (+" + addQty + ")");
    }

    /** Feature 23: Manage products (Add, Edit, Delete products) */
    private void handleProductManagement(BufferedReader console) throws Exception {
        System.out.print("Choose action - [A]dd, [E]dit, [D]elete: ");
        String action = console.readLine();
        if (action == null) action = "";
        action = action.trim().toUpperCase();
        if (action.equals("A")) {
            // Add new product
            if (dp.productCount >= dp.products.length) {
                System.out.print("Product list is full, cannot add more products.\n");
                return;
            }
            System.out.print("Enter new Product ID: ");
            String newId = console.readLine();
            if (newId == null) newId = "";
            newId = newId.trim();
            if (newId.equals("")) {
                System.out.print("Product ID cannot be empty.\n");
                return;
            }
            // Check for uniqueness
            if (dp.findProductById(newId) != null) {
                System.out.print("Product ID " + newId + " already exists.\n");
                return;
            }
            System.out.print("Enter Category: ");
            String category = console.readLine();
            if (category == null) category = "";
            category = category.trim();
            System.out.print("Enter Brand: ");
            String brand = console.readLine();
            if (brand == null) brand = "";
            brand = brand.trim();
            System.out.print("Enter Product Name: ");
            String name = console.readLine();
            if (name == null) name = "";
            name = name.trim();
            System.out.print("Enter Price: ");
            String priceStr = console.readLine();
            if (priceStr == null) priceStr = "";
            priceStr = priceStr.trim();
            System.out.print("Enter Initial Stock: ");
            String stockStr = console.readLine();
            if (stockStr == null) stockStr = "";
            stockStr = stockStr.trim();
            if (newId.equals("") || category.equals("") || brand.equals("") || name.equals("")) {
                System.out.print("Fields cannot be empty. Product not added.\n");
                return;
            }
            int price = DataPersistence.toInt(priceStr);
            int stock = DataPersistence.toInt(stockStr);
            dp.products[dp.productCount++] = new Product(newId, category, brand, name, price, stock);
            System.out.print("Product " + newId + " added successfully.\n");
            log.write("ADMIN", "Added product " + newId);
        } else if (action.equals("E")) {
            // Edit existing product
            System.out.print("Enter Product ID to edit: ");
            String editId = console.readLine();
            if (editId == null) editId = "";
            editId = editId.trim();
            if (editId.equals("")) {
                System.out.print("Product ID cannot be empty.\n");
                return;
            }
            Product product = dp.findProductById(editId);
            if (product == null) {
                System.out.print("Product " + editId + " not found.\n");
                return;
            }
            System.out.print("Edit field - [N]ame, [P]rice, [S]tock: ");
            String field = console.readLine();
            if (field == null) field = "";
            field = field.trim().toUpperCase();
            if (field.equals("N")) {
                System.out.print("Enter new Name: ");
                String newName = console.readLine();
                if (newName == null) newName = "";
                newName = newName.trim();
                if (!newName.equals("")) {
                    product.name = newName;
                    System.out.print("Product " + product.productId + " name updated.\n");
                    log.write("ADMIN", "Edited product " + product.productId + " (Name changed)");
                }
            } else if (field.equals("P")) {
                System.out.print("Enter new Price: ");
                String newPriceStr = console.readLine();
                if (newPriceStr == null) newPriceStr = "";
                newPriceStr = newPriceStr.trim();
                int newPrice = DataPersistence.toInt(newPriceStr);
                if (newPrice > 0) {
                    product.price = newPrice;
                    System.out.print("Product " + product.productId + " price updated.\n");
                    log.write("ADMIN", "Edited product " + product.productId + " (Price changed)");
                }
            } else if (field.equals("S")) {
                System.out.print("Enter new Stock value: ");
                String newStockStr = console.readLine();
                if (newStockStr == null) newStockStr = "";
                newStockStr = newStockStr.trim();
                int newStock = DataPersistence.toInt(newStockStr);
                if (newStock >= 0) {
                    product.stock = newStock;
                    System.out.print("Product " + product.productId + " stock updated.\n");
                    log.write("ADMIN", "Edited product " + product.productId + " (Stock adjusted)");
                }
            } else {
                System.out.print("Invalid field selection.\n");
            }
        } else if (action.equals("D")) {
            // Delete a product
            System.out.print("Enter Product ID to delete: ");
            String delId = console.readLine();
            if (delId == null) delId = "";
            delId = delId.trim();
            if (delId.equals("")) {
                System.out.print("Product ID cannot be empty.\n");
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
                System.out.print("Product " + delId + " not found.\n");
                return;
            }
            // Confirm deletion
            System.out.print("Are you sure you want to delete " + delId + "? (Y/N): ");
            String conf = console.readLine();
            if (conf == null) conf = "";
            conf = conf.trim().toUpperCase();
            if (!conf.equals("Y") && !conf.equals("YES")) {
                System.out.print("Deletion cancelled.\n");
                return;
            }
            // Remove product by shifting array
            for (int j = idx; j < dp.productCount - 1; j++) {
                dp.products[j] = dp.products[j+1];
            }
            dp.products[dp.productCount - 1] = null;
            dp.productCount--;
            System.out.print("Product " + delId + " deleted.\n");
            log.write("ADMIN", "Deleted product " + delId);
        } else {
            System.out.print("Invalid action.\n");
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
        System.out.print("Order ID: " + order.orderId + "\n");
        System.out.print("Date: " + order.date + "\n");
        System.out.print("Status: " + order.status + "\n");
        if (order.status.equalsIgnoreCase("CANCELLED")) {
            System.out.print("Cancel Reason: " + (order.cancelReason.equals("") ? "(None)" : order.cancelReason) + "\n");
        }
        if (order.trackingId != null && !order.trackingId.equals("")) {
            System.out.print("Tracking ID: " + order.trackingId + "\n");
        }
        System.out.print("Address: " + (order.address.equals("") ? "(Not provided)" : order.address) + "\n");
        System.out.print("Payment Mode: " + (order.paymentMode.equals("") ? "(N/A)" : order.paymentMode) + "\n");
        System.out.print("Total Amount: BDT " + order.totalAmount + "\n");
        System.out.print("Items:\n");
        for (int i = 0; i < order.itemCount; i++) {
            Item it = order.items[i];
            if (it == null) continue;
            Product p = dp.findProductById(it.productId);
            String itemName = (p != null ? p.name : it.productId);
            System.out.print("- " + itemName + " (x" + it.quantity + ")\n");
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
        System.out.print("(Full report saved to report.txt)\n");
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
    System.out.print("New Order ID: " + newOrder.orderId + "\n");

    // 2. Display product catalog (Product ID, Name, Stock)
    System.out.print("\n--- Product Catalog ---\n");
    for (int i = 0; i < dp.productCount; i++) {
        Product prod = dp.products[i];
        if (prod == null) continue;
        System.out.print(prod.productId + " - " + prod.name + " (Stock: " + prod.stock + ")\n");
    }
    System.out.print("-----------------------\n");

    // 3. Allow admin to select 1–3 products and specify quantities
    System.out.print("How many different products in this order? (1-10): ");
    String countStr = console.readLine();
    if (countStr == null) countStr = "";
    countStr = countStr.trim();
    int itemCount = DataPersistence.toInt(countStr);
    if (itemCount < 1 || itemCount > 10) {
        System.out.print("Invalid number of products. Order cancelled.\n");
        return;
    }
    for (int i = 1; i <= itemCount; i++) {
        System.out.print("Enter Product ID for item " + i + ": ");
        String pid = console.readLine();
        if (pid == null) pid = "";
        pid = pid.trim();
        if (pid.equals("")) {
            System.out.print("Product ID cannot be empty. Order cancelled.\n");
            return;
        }
        Product product = dp.findProductById(pid);
        if (product == null) {
            System.out.print("Product " + pid + " not found. Order cancelled.\n");
            return;
        }
        System.out.print("Enter quantity for " + product.name + ": ");
        String qtyStr = console.readLine();
        if (qtyStr == null) qtyStr = "";
        qtyStr = qtyStr.trim();
        int qty = DataPersistence.toInt(qtyStr);
        if (qty <= 0) {
            System.out.print("Invalid quantity. Order cancelled.\n");
            return;
        }
        // Add the selected item to the order
        if (!newOrder.addItem(new Item(product.productId, qty))) {
            System.out.print("Failed to add item " + product.productId + ". Order cancelled.\n");
            return;
        }
    }

    // 4. Ask for shipping address and payment mode
    System.out.print("Enter shipping address: ");
    String address = console.readLine();
    if (address == null) address = "";
    address = address.trim();
    if (address.equals("")) {
        System.out.print("Address cannot be empty. Order cancelled.\n");
        return;
    }
    newOrder.address = address;
    System.out.print("Enter payment mode (COD or MockCard): ");
    String paymentMode = console.readLine();
    if (paymentMode == null) paymentMode = "";
    paymentMode = paymentMode.trim();
    if (paymentMode.equalsIgnoreCase("")) {
        System.out.print("Payment mode cannot be empty. Order cancelled.\n");
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
        System.out.print("Order processing failed. Order ID: " + newOrder.orderId 
                         + " is CANCELLED (" + newOrder.cancelReason + ").\n");
        // (The cancellation reason and status change have been logged by processPendingOrder)
    } else {
        // If processing succeeded, the order status is now "PACKED"
        System.out.print("New order accepted and processed successfully! New Order ID: " 
                         + newOrder.orderId + " (Status: " + newOrder.status + ").\n");
        // (Inventory reservation and payment confirmation have been logged, and status set to PACKED)
    }
}

}

