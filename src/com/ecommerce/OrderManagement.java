
package com.ecommerce;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String DB_URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";

    // Change this to your MySQL password
    static final String PASSWORD = "password";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    // DATABASE AND TABLE CREATION
    public static void connect() throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        // Connect to MySQL server
        Connection temp = DriverManager.getConnection(
                URL, USER, PASSWORD);

        Statement st = temp.createStatement();

        // Create database
        st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS ecommerce_db");

        temp.close();

        // Connect to ecommerce_db
        con = DriverManager.getConnection(
                DB_URL, USER, PASSWORD);

        // Create Orders table
        String sql = "CREATE TABLE IF NOT EXISTS Orders (" +
                "order_id INT PRIMARY KEY, " +
                "customer_name VARCHAR(100), " +
                "product_name VARCHAR(100), " +
                "quantity INT, " +
                "unit_price DOUBLE, " +
                "status VARCHAR(30))";

        Statement stmt = con.createStatement();
        stmt.executeUpdate(sql);

        System.out.println("Database connected successfully!");
        System.out.println("Orders table created/verified!");
    }

    // A) INSERT THREE ORDERS
    public static void insertOrders() {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter details for order " + i);

                System.out.print("Order ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Customer Name: ");
                String customer = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Unit Price: ");
                double price = sc.nextDouble();
                sc.nextLine();

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, customer);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID! Record not inserted.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Insertion error: " + e.getMessage());
        }
    }

    // B) DISPLAY ORDERS BY STATUS
    public static void searchOrders() {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders " +
                "WHERE status = ? AND quantity > 2 " +
                "ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            System.out.println("\nMatching Orders:");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        rs.getInt("order_id") + " | " +
                        rs.getString("customer_name") + " | " +
                        rs.getString("product_name") + " | " +
                        rs.getInt("quantity") + " | " +
                        rs.getDouble("unit_price") + " | " +
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No matching orders found.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // C) INCREASE ORDER QUANTITY
    public static void increaseQuantity() {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int amount = sc.nextInt();
        sc.nextLine();

        if (amount <= 0) {
            System.out.println("Additional quantity must be positive.");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? " +
                "WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // D) UPDATE ORDER STATUS
    public static void updateStatus() {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new status: ");
        String status = sc.nextLine();

        if (!(status.equalsIgnoreCase("Pending") ||
              status.equalsIgnoreCase("Shipped") ||
              status.equalsIgnoreCase("Delivered"))) {

            System.out.println("Invalid status!");
            System.out.println("Allowed: Pending, Shipped, Delivered");
            return;
        }

        // Normalize status
        if (status.equalsIgnoreCase("Pending"))
            status = "Pending";
        else if (status.equalsIgnoreCase("Shipped"))
            status = "Shipped";
        else
            status = "Delivered";

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Status updated successfully!");

                String query = "SELECT * FROM Orders WHERE order_id = ?";

                try (PreparedStatement ps2 = con.prepareStatement(query)) {

                    ps2.setInt(1, id);

                    ResultSet rs = ps2.executeQuery();

                    if (rs.next()) {
                        System.out.println("Updated Order Details:");
                        System.out.println("ID: " + rs.getInt("order_id"));
                        System.out.println("Customer: " + rs.getString("customer_name"));
                        System.out.println("Product: " + rs.getString("product_name"));
                        System.out.println("Quantity: " + rs.getInt("quantity"));
                        System.out.println("Price: " + rs.getDouble("unit_price"));
                        System.out.println("Status: " + rs.getString("status"));
                    }
                }

            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // E1) DISPLAY TOTAL ORDER VALUE
    public static void totalOrderValue() {

        String sql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Order Value: " +
                        rs.getDouble("total"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // E2) DELETE ONLY CANCELLED ORDERS
    public static void deleteOrder() {

        System.out.print("Enter Order ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        String check = "SELECT status FROM Orders WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(check)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Order does not exist.");
                return;
            }

            String status = rs.getString("status");

            if (!status.equalsIgnoreCase("Cancelled")) {
                System.out.println("Cannot delete! Order is not Cancelled.");
                return;
            }

            String sql = "DELETE FROM Orders WHERE order_id = ? " +
                    "AND status = 'Cancelled'";

            try (PreparedStatement ps2 = con.prepareStatement(sql)) {

                ps2.setInt(1, id);

                int rows = ps2.executeUpdate();

                if (rows > 0) {
                    System.out.println("Cancelled order deleted successfully!");
                } else {
                    System.out.println("Order could not be deleted.");
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // MAIN MENU
    public static void main(String[] args) {

        try {

            connect();

            int choice;

            do {

                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Three Orders");
                System.out.println("2. Search Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Display Total Order Value");
                System.out.println("6. Delete Cancelled Order");
                System.out.println("0. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:
                        insertOrders();
                        break;

                    case 2:
                        searchOrders();
                        break;

                    case 3:
                        increaseQuantity();
                        break;

                    case 4:
                        updateStatus();
                        break;

                    case 5:
                        totalOrderValue();
                        break;

                    case 6:
                        deleteOrder();
                        break;

                    case 0:
                        System.out.println("Exiting application...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 0);

        } catch (Exception e) {
        	  e.printStackTrace();
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            sc.close();
        }
    }
}
