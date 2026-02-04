import java.io.BufferedReader;
import java.security.MessageDigest;

/** Admin.java – Admin model and authentication logic (password hashing for secure login) */
public class Admin {
    public String username;
    public String passHash;  // stored password hash (hex string)

    public Admin(String username, String passHash) {
        this.username = username;
        this.passHash = passHash;
    }

    /** Authenticate an admin login (allows 3 attempts). Returns true if successful. */
    public static boolean authenticate(DataPersistence dp, BufferedReader console) throws Exception {
        System.out.print("\n=== Admin Login ===\n");
        int attempts = 0;
        while (attempts < 3) {
            // Prompt for credentials
            System.out.print("Username: ");
            String u = console.readLine();
            if (u == null) u = "";
            u = u.trim();
            System.out.print("Password: ");
            String p = console.readLine();
            if (p == null) p = "";
            p = p.trim();
            // Verify credentials against all admin accounts
            boolean authenticated = false;
            for (int i = 0; i < dp.adminCount; i++) {
                Admin adm = dp.admins[i];
                if (adm != null && adm.username.equals(u) && adm.passHash.equals(hashPassword(p))) {
                    dp.currentAdminIndex = i;  // store which admin logged in
                    authenticated = true;
                    break;
                }
            }
            if (authenticated) {
                System.out.print("Login successful.\n");
                return true;
            } else {
                System.out.print("Invalid credentials. ");
                attempts++;
                if (attempts < 3) {
                    System.out.print("Try again.\n");
                }
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
