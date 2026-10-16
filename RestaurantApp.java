import java.util.ArrayList;
import java.util.Scanner;

public class RestaurantApp extends Restaurant {
    // Stores descriptions of the items ordered by the customer.
    private final ArrayList<String> orderList = new ArrayList<>();

    // Accepts menu selections and quantities from the customer.
    public void takeOrder(Scanner scanner) {
        while (true) {
            System.out.print("\nEnter menu item number (0 to finish): ");
            String choiceText = scanner.nextLine().trim();

            int choice;
            try {
                choice = Integer.parseInt(choiceText);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid whole number.");
                continue;
            }

            if (choice == 0) {
                break;
            }

            if (choice < 1 || choice > getMenuSize()) {
                System.out.println("That menu number does not exist. Try again.");
                continue;
            }

            System.out.print("Enter quantity: ");
            String quantityText = scanner.nextLine().trim();

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid whole-number quantity.");
                continue;
            }

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                continue;
            }

            String[] parts = getMenuItem(choice - 1).split(",", 2);
            String itemName = parts[0].trim();

            double price;
            try {
                price = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException exception) {
                System.out.println("The price for this menu item is invalid.");
                continue;
            }

            double amount = price * quantity;

            StringBuffer description = new StringBuffer();
            description.append(itemName)
                       .append(" x ")
                       .append(quantity)
                       .append(" = Rs. ")
                       .append(String.format("%.2f", amount));

            orderList.add(description.toString());
            addToBill(amount);

            System.out.println("Added: " + description);
        }

        if (orderList.isEmpty()) {
            System.out.println("No items were ordered.");
        }
    }

    // Prints the customer's order and calculates any eligible discount.
    public void displayBill() {
        System.out.println("\n========== CUSTOMER BILL ==========");

        if (orderList.isEmpty()) {
            System.out.println("No items ordered.");
            System.out.println("Total: Rs. 0.00");
            return;
        }

        for (String item : orderList) {
            System.out.println(item);
        }

        double subtotal = getTotalBill();
        double discountRate;

        if (subtotal >= 1000) {
            discountRate = 0.10;
        } else if (subtotal >= 500) {
            discountRate = 0.05;
        } else {
            discountRate = 0.0;
        }

        double discount = subtotal * discountRate;
        double finalAmount = subtotal - discount;

        System.out.printf("Subtotal: Rs. %.2f%n", subtotal);
        System.out.printf("Discount: Rs. %.2f%n", discount);
        System.out.printf("Final amount: Rs. %.2f%n", finalAmount);
    }

    // Creates a kitchen ticket using the customer's order list.
    public void sendToKitchen() {
        StringBuffer ticket = new StringBuffer();

        ticket.append("\n========== KITCHEN ORDER TICKET ==========\n");

        if (orderList.isEmpty()) {
            ticket.append("No order to prepare.\n");
        } else {
            for (String item : orderList) {
                ticket.append(item).append('\n');
            }
        }

        System.out.print(ticket);
    }

    // Lets the user update the kitchen/delivery status.
    public String updateKitchenStatus(Scanner scanner) {
        System.out.println("\nSelect order status:");
        System.out.println("1. Preparing");
        System.out.println("2. Order ready");
        System.out.println("3. Delivered");
        System.out.print("Enter status number: ");

        String statusText = scanner.nextLine().trim();

        int statusChoice;
        try {
            statusChoice = Integer.parseInt(statusText);
        } catch (NumberFormatException exception) {
            System.out.println("Invalid selection. Status remains Preparing.");
            return "Preparing";
        }

        switch (statusChoice) {
            case 1:
                System.out.println("Status updated: Preparing");
                return "Preparing";
            case 2:
                System.out.println("Status updated: Order ready");
                return "Order ready";
            case 3:
                System.out.println("Status updated: Delivered");
                return "Delivered";
            default:
                System.out.println("Invalid selection. Status remains Preparing.");
                return "Preparing";
        }
    }

    // Confirms delivery only after the status is marked as Delivered.
    public void confirmDelivery(Scanner scanner, String kitchenStatus) {
        if (!"Delivered".equals(kitchenStatus)) {
            System.out.println("Delivery cannot be confirmed yet. Current status: "
                    + kitchenStatus);
            return;
        }

        System.out.print("Has the customer received the order? (yes/no): ");
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase("yes")) {
            System.out.println("Delivery confirmed. Thank you for ordering!");
        } else {
            System.out.println("Delivery has not been confirmed.");
        }
    }

    public static void main(String[] args) {
        RestaurantApp app = new RestaurantApp();

        try {
            app.loadMenu("menu.txt");
            app.displayMenu();

            try (Scanner scanner = new Scanner(System.in)) {
                app.takeOrder(scanner);
                app.displayBill();
                app.sendToKitchen();

                if (app.getTotalBill() > 0) {
                    String status = app.updateKitchenStatus(scanner);
                    app.confirmDelivery(scanner, status);
                }
            }
        } catch (java.io.IOException exception) {
            System.out.println("Could not load menu.txt: " + exception.getMessage());
            System.out.println("Make sure menu.txt is in the project folder.");
        }
    }
}
