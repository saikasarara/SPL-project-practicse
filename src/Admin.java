import java.io.BufferedReader;
import java.security.MessageDigest;

/** Admin.java – Admin model and authentication logic (password hashing for secure login) */
public class Admin {
    public String username;
    public String passHash;  // Hashed password
    public Role role;  // User role (e.g., ADMIN, MANAGER, SUPPORT)

    // Constructor to initialize the Admin object
    public Admin(String username, String passHash, Role role) {
        this.username = username;
        this.passHash = passHash;
        this.role = role;
    }

        // (optional) if you still use old 2-arg constructor anywhere
    public Admin(String username, String passHash) {
        this.username = username;
        this.passHash = passHash;
        this.role = Role.ADMIN; // default
    }
      public boolean hasPermission(Role required) {
        return this.role == required;
    }

public static boolean authenticate(DataPersistence dp, BufferedReader console) throws Exception {
    System.out.print("\n=== Admin Login ===\n");
    int attempts = 0;

    while (attempts < 3) {
        System.out.print("Username: ");
        String u = console.readLine();
        if (u == null) u = "";
        u = u.trim();

        System.out.print("Password: ");
        String p = console.readLine();
        if (p == null) p = "";
        p = p.trim();

        int foundIndex = -1;

        for (int i = 0; i < dp.adminCount; i++) {
            Admin adm = dp.admins[i];
            if (adm != null && adm.username.equals(u) && adm.passHash.equals(hashPassword(p))) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1) {
            dp.currentAdminIndex = foundIndex;
            Admin admin = dp.admins[foundIndex];
            System.out.println("Login successful. Role: " + admin.role);
            return true;
        } else {
            System.out.print("Invalid credentials. ");
            attempts++;
            if (attempts < 3) System.out.print("Try again.\n");
        }
    }

    System.out.print("\nToo many failed attempts.\n");
    return false;
}


    /** Compute SHA-256 hash of a plaintext password and return hex string */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");  // hashing algorithm
            byte[] hashBytes = md.digest(password.getBytes());        // compute hash
            // Convert hash bytes to hexadecimal string
            String hexDigits = "0123456789abcdef";
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                int val = b & 0xFF;
                sb.append(hexDigits.charAt(val >>> 4));
                sb.append(hexDigits.charAt(val & 0x0F));
            }
            return sb.toString();
        } catch (Exception e) {
            // In case of error, fall back to plain password (should not happen in normal use)
            return password;
        }
    }
    
}
