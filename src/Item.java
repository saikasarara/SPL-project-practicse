/** Item.java – Item model (represents a product and quantity in an order) */
public class Item {
    public String productId;
    public int quantity;

    public Item(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}

