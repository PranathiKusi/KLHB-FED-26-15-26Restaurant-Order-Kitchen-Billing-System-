
import java.util.*;
import java.nio.file.*;
import java.io.IOException;

class Order {
    String customerName;
    String itemName;
    int quantity;
    double total;

    // Constructor
    Order(String customerName, String itemName, int quantity, double total) {
        this.customerName = customerName;
        this.itemName = itemName;
        this.quantity = quantity;
        this.total = total;
    }

    // Display order details
    void display() {
        System.out.println("Customer: " + customerName);
        System.out.println("Item: " + itemName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Total: Rs. " + total);
    }
}

public class DineSyncMain {

    static Scanner sc = new Scanner(System.in);

    static String[] items = {"Burger", "Pizza", "Sandwich"};
    static double[] prices = {100, 200, 80};

    static ArrayList<Order> orders = new ArrayList<>();
    static HashSet<String> orderedItems = new HashSet<>();
    static HashMap<String, Integer> sales = new HashMap<>();

    // CO3: Method to display the menu
    static void showMenu() {
        System.out.println("\n----- DINESYNC MENU -----");

        for (int i = 0; i < items.length; i++) {
            System.out.println((i + 1) + ". " + items[i]
                    + " - Rs. " + prices[i]);
        }
    }

    public static void main(String[] args) {

        int choice = 0;

        System.out.println("WELCOME TO DINESYNC!");

        do {
            System.out.println("\n1. Show Menu");
            System.out.println("2. Place Order");
            System.out.println("3. View Bill");
            System.out.println("4. View Sales Report");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {

                    case 1:
                        showMenu();
                        break;

                    case 2:
                        placeOrder();
                        break;

                    case 3:
                        viewBill();
                        break;

                    case 4:
                        viewSales();
                        break;

                    case 5:
                        System.out.println("Thank you for visiting DineSync!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }

        } while (choice != 5);

        sc.close();
    }

    // CO3: Method to place an order
    static void placeOrder() {

        showMenu();

        System.out.print("Enter customer name: ");
        String name = sc.nextLine().trim();

        System.out.print("Choose item number: ");
        int itemChoice;

        try {
            itemChoice = Integer.parseInt(sc.nextLine());

            if (itemChoice < 1 || itemChoice > items.length) {
                System.out.println("Invalid item number!");
                return;
            }

            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(sc.nextLine());

            if (quantity <= 0) {
                System.out.println("Quantity must be positive!");
                return;
            }

            int index = itemChoice - 1;
            double total = prices[index] * quantity;

            // CO1: Arithmetic operators and variables
            // CO4: Creating an object using a constructor
            Order order = new Order(
                    name, items[index], quantity, total);

            orders.add(order);

            // CO6: Set stores unique item names
            orderedItems.add(items[index]);

            // CO6: Map stores total quantity sold per item
            int oldQuantity = sales.getOrDefault(items[index], 0);
            sales.put(items[index], oldQuantity + quantity);

            System.out.println("\nOrder placed successfully!");
            order.display();

            // CO5: Save the order to a text file
            String receipt = name + " | " + items[index]
                    + " | Quantity: " + quantity
                    + " | Total: Rs. " + total + "\n";

            try {
                Files.writeString(
                        Path.of("orders.txt"),
                        receipt,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                System.out.println("Order saved to file.");

            } catch (IOException e) {
                System.out.println("Could not save the order.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers!");
        }
    }

    // CO3: Method to display bills
    static void viewBill() {

        if (orders.isEmpty()) {
            System.out.println("No orders available.");
            return;
        }

        double grandTotal = 0;

        System.out.println("\n----- BILL DETAILS -----");

        for (Order order : orders) {
            order.display();
            System.out.println("------------------------");
            grandTotal += order.total;
        }

        System.out.println("Grand Total: Rs. " + grandTotal);
    }

    // CO6: Collections, lambda expression and Stream API
    static void viewSales() {

        System.out.println("\n----- SALES REPORT -----");

        System.out.println("Unique items ordered: " + orderedItems);

        sales.forEach((item, quantity) ->
                System.out.println(item + ": " + quantity + " sold"));

        System.out.println("\nOrders costing more than Rs. 100:");

        orders.stream()
                .filter(order -> order.total > 100)
                .forEach(order ->
                        System.out.println(order.customerName
                                + " - Rs. " + order.total));
    }
}
