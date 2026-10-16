
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Restaurant {

    // Stores the restaurant menu.
    private final ArrayList<String> menuItems = new ArrayList<>();

    // Stores the total bill.
    private double totalBill;

    // Loads menu items from the file.
    public void loadMenu(String fileName) throws IOException {
        menuItems.clear();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(fileName))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    menuItems.add(line.trim());
                }
            }
        }
    }

    // Displays the restaurant menu.
    public void displayMenu() {
        StringBuffer menuDisplay = new StringBuffer();

        menuDisplay.append("\n===== RESTAURANT MENU =====\n");

        for (int i = 0; i < menuItems.size(); i++) {
            String[] parts = menuItems.get(i).split(",", 2);

            menuDisplay.append(i + 1)
                    .append(". ")
                    .append(parts[0].trim())
                    .append(" - Rs. ")
                    .append(parts[1].trim())
                    .append("\n");
        }

        System.out.print(menuDisplay);
    }

    // Returns the number of menu items.
    public int getMenuSize() {
        return menuItems.size();
    }

    // Returns a menu item.
    public String getMenuItem(int index) {
        return menuItems.get(index);
    }

    // Adds an amount to the bill.
    public void addToBill(double amount) {
        totalBill += amount;
    }

    // Returns the total bill.
    public double getTotalBill() {
        return totalBill;
    }

    // Main method: program execution starts here.
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();

        try {
            restaurant.loadMenu("menu.txt");
            restaurant.displayMenu();
        } catch (IOException e) {
            System.out.println("Error loading menu: " + e.getMessage());
        }
    }
}
