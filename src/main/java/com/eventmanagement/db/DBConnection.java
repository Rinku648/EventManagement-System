package com.eventmanagement.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "JDBC:mysql://localhost:3306/event_management";

    private static final String USER = "root";

    private static final String PASSWORD = "@Arpitsql123";

    public static Connection getConnection() {

        try {

            Connection connection =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database Connected Successfully!");

            return connection;

        } catch (SQLException e) {

            System.out.println("Database Connection Failed!");

            e.printStackTrace();

            return null;
        }
    }
}