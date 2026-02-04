import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

/** DataPersistence.java – Handles loading and saving of data from text files */
public class DataPersistence {
    private String baseDir;
    // Data stores in memory
    public Product[] products = new Product[200];
    public int productCount = 0;
    public Order[] orders = new Order[200];
    public int orderCount = 0;
    public Admin[] admins = new Admin[50];
    public int adminCount = 0;
    public int currentAdminIndex = -1;        // index of the currently logged-in admin
    private int nextOrderNumber = 1001;       // next numeric ID for new orders (starting from O1001)

    public DataPersistence(String baseDir) {
        this.baseDir = (baseDir == null ? "" : baseDir);
    }

    /** Utility to get full file path with base directory (if any) */
    public String path(String filename) {
        if (baseDir.equals("")) {
            return filename;
        }
        return baseDir + "/" + filename;
    }

    /** Load all data from files: products, orders, admins */
    public void loadAll() throws Exception {
        loadProducts();
        loadOrders();
        loadAdmins();
        // Compute initial next order number based on loaded orders
        computeNextOrderNumber();
    }

    private void loadProducts() throws Exception {
        productCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path("products.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                // Format: ProductID|Category|Brand|Name|Price|Stock
                String[] parts = line.split("\\|");
                if (parts.length < 6) continue;
                String pid = parts[0].trim();
                String category = parts[1].trim();
                String brand = parts[2].trim();
                String name = parts[3].trim();
                String priceStr = parts[4].trim().replace(",", "");
                String stockStr = parts[5].trim();
                int price = toInt(priceStr);
                int stock = toInt(stockStr);
                products[productCount++] = new Product(pid, category, brand, name, price, stock);
            }
        } catch (Exception e) {
            // If file not found or format error, skip (will use defaults if any)
        } finally {
            if (br != null) br.close();
        }
    }

    private void loadOrders() throws Exception {
        orderCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path("orders.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                // Format: OrderID|Date|Address|PaymentMode|Status|ItemList|Total|CancelReason
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;
                Order o = new Order();
                o.orderId    = parts[0].trim();
                o.date       = (parts.length > 1 ? parts[1].trim() : "");
                o.address    = (parts.length > 2 ? parts[2].trim() : "");
                o.paymentMode= (parts.length > 3 ? parts[3].trim() : "");
                o.status     = (parts.length > 4 ? parts[4].trim() : "PENDING");
                // Parse items list
                if (parts.length > 5) {
                    String itemsPart = parts[5].trim();
                    parseItemsIntoOrder(o, itemsPart);
                }
                // Total amount
                if (parts.length > 6) {
                    o.totalAmount = toInt(parts[6].trim());
                }
                // Cancel reason (if any)
                if (parts.length > 7) {
                    o.cancelReason = parts[7].trim();
                }
                // Tracking ID will be assigned when order is marked SHIPPED in workflow
                orders[orderCount++] = o;
            }
        } catch (Exception e) {
            // If orders file not found, skip (no initial orders)
        } finally {
            if (br != null) br.close();
        }
    }

    private void loadAdmins() throws Exception {
        adminCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path("admins.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                // Format: username|hashedPassword
                String[] parts = line.split("\\|");
                String user = parts[0].trim();
                String hash = (parts.length > 1 ? parts[1].trim() : "");
                if (!user.equals("") && !hash.equals("")) {
                    admins[adminCount++] = new Admin(user, hash);
                }
            }
        } catch (Exception e) {
            // If admins file not found, create default admin later
        } finally {
            if (br != null) br.close();
        }
        if (adminCount == 0) {
            // If no admin loaded, create a default admin account (admin/admin123)
            String defaultUser = "admin";
            String defaultPassHash = Admin.hashPassword("admin123");
            admins[adminCount++] = new Admin(defaultUser, defaultPassHash);
        }
    }

    /** Save all data back to text files */
    public void saveAll() throws Exception {
        saveProducts();
        saveOrders();
        saveAdmins();
    }

    private void saveProducts() throws Exception {
        FileWriter fw = new FileWriter(path("products.txt"), false);  // overwrite file
        for (int i = 0; i < productCount; i++) {
            Product p = products[i];
            if (p == null) continue;
            // Format: ProductID|Category|Brand|Name|Price|Stock
            fw.write(p.productId + "|" + p.category + "|" + p.brand + "|" + p.name + "|" + p.price + "|" + p.stock + "\n");
        }
        fw.close();
    }

    private void saveOrders() throws Exception {
        FileWriter fw = new FileWriter(path("orders.txt"), false);
        for (int i = 0; i < orderCount; i++) {
            Order o = orders[i];
            if (o == null) continue;
            // Do not save orders that have been archived (they would have been removed from array)
            fw.write(o.toRecord() + "\n");
        }
        fw.close();
    }

    private void saveAdmins() throws Exception {
        FileWriter fw = new FileWriter(path("admins.txt"), false);
        for (int i = 0; i < adminCount; i++) {
            Admin a = admins[i];
            if (a == null) continue;
            fw.write(a.username + "|" + a.passHash + "\n");
        }
        fw.close();
    }

    /** Convert a string to integer without using library parse methods */
    public static int toInt(String s) {
        if (s == null) return 0;
        s = s.trim();
        if (s.length() == 0) return 0;
        boolean neg = false;
        if (s.charAt(0) == '-') {
            neg = true;
            s = s.substring(1);
        }
        int value = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
            } else {
                // ignore any non-digit characters
            }
        }
        return neg ? -value : value;
    }

    /** Determine nextOrderNumber by finding the max numeric part of loaded order IDs */
    private void computeNextOrderNumber() {
        int maxNum = 1000;
        for (int i = 0; i < orderCount; i++) {
            Order o = orders[i];
            if (o == null) continue;
            String id = o.orderId;
            // Assuming OrderID starts with a letter followed by number (e.g., "O1001")
            String numPart = "";
            for (int k = 0; k < id.length(); k++) {
                char ch = id.charAt(k);
                if (ch >= '0' && ch <= '9') {
                    numPart += ch;
                }
            }
            int num = toInt(numPart);
            if (num > maxNum) {
                maxNum = num;
            }
        }
        // Next order number continues from max found
        if (maxNum >= 1000) {
            nextOrderNumber = maxNum + 1;
        }
    }

    /** Generate a new unique Order ID (e.g., "O1001", "O1002", ...) */
    public String generateOrderId() {
        String newId = "O" + nextOrderNumber;
        nextOrderNumber++;
        return newId;
    }

    /** Parse an "ItemList" string (format "ProductIDxQty, ...") into Item objects added to Order */
    public void parseItemsIntoOrder(Order o, String itemsPart) {
        if (o == null || itemsPart == null) return;
        String part = itemsPart.trim();
        if (part.length() == 0) return;
        String[] itemTokens = part.split(",");
        for (int i = 0; i < itemTokens.length; i++) {
            String token = itemTokens[i].trim();
            if (token.length() == 0) continue;
            // Each token format: ProductIDxQuantity (e.g., "M101x2")
            String[] kv = token.split("x");
            if (kv.length == 2) {
                String pid = kv[0].trim();
                int qty = toInt(kv[1].trim());
                Item item = new Item(pid, qty);
                o.addItem(item);  // addItem will ignore if qty <= 0 or capacity exceeded
            }
        }
    }

    /** Find a Product by its ID (case-sensitive match). Returns null if not found. */
    public Product findProductById(String productId) {
        for (int i = 0; i < productCount; i++) {
            Product p = products[i];
            if (p != null && p.productId.equals(productId)) {
                return p;
            }
        }
        return null;
    }
}

