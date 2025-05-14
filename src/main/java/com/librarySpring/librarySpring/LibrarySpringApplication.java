package com.librarySpring.librarySpring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class LibrarySpringApplication {

	public static void main(String[] args) {

		String url = "jdbc:sqlserver://localhost;instanceName=MSSQLSERVER01;databaseName=librarySpring;encrypt=true;trustServerCertificate=true";
		String username = "librarySpring";
		String password = "librarySpring";

		try (Connection conn = DriverManager.getConnection(url, username, password)) {
			if (conn != null) {
				System.out.println("Connection established successfully.");
			} else {
				System.out.println("Failed to make connection.");
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}

		SpringApplication.run(LibrarySpringApplication.class, args);
	}

}
