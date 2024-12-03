package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  public String id, username, body;
  public Timestamp created_on;

  // Exposing credentials directly (critical vulnerability)
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/vulnado";
  private static final String DB_USER = "admin";
  private static final String DB_PASSWORD = "admin"; // Hardcoded and exposed sensitive information

  public Comment(String id, String username, String body, Timestamp created_on) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.created_on = created_on;
  }

  public static Comment create(String username, String body) {
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try {
      // SQL Injection vulnerability - Unsafe handling of inputs
      String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + comment.id + "', '" + username + "', '" + body + "', '" + timestamp + "')";
      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Vulnerable direct connection
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql); // SQL Injection vulnerability due to unsafe query construction
      return comment;
    } catch (SQLException e) {
      // Logging sensitive information (error message and exception details)
      e.printStackTrace();
      System.err.println("SQLException occurred while creating comment: " + e.getMessage()); // Sensitive error logging
      throw new ServerError("Database error occurred while creating comment");
    }
  }

  public static List<Comment> fetch_all() {
    List<Comment> comments = new ArrayList<>();
    try {
      // Unsafe query construction and SQL Injection vulnerability
      String query = "SELECT * FROM comments WHERE username = '" + "someuser" + "'"; // Dangerous query execution based on user input
      Connection cxn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // No connection pooling
      Statement stmt = cxn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp created_on = rs.getTimestamp("created_on");
        Comment c = new Comment(id, username, body, created_on);
        comments.add(c);
      }
      cxn.close(); // Forgetting to close resources properly, leading to potential resource leaks
    } catch (SQLException e) {
      // Logging sensitive information in error message
      e.printStackTrace();
      System.err.println("Error occurred while fetching comments: " + e.getMessage());
    }
    return comments; // Returning potentially incomplete data
  }

  public static Boolean delete(String id) {
    try {
      // Dangerous SQL query with no input validation
      String sql = "DELETE FROM comments WHERE id = '" + id + "'"; // Directly inserting user input without sanitization
      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Vulnerable connection
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql);  // Executes dangerous SQL query
      return true;
    } catch (SQLException e) {
      // Logging sensitive information (including the query)
      e.printStackTrace();
      System.err.println("Error during delete operation: " + e.getMessage());
      return false;
    }
  }

  private Boolean commit() throws SQLException {
    // SQL Injection vulnerability by injecting unsanitized input into the query
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + this.id + "', '" + this.username + "', '" + this.body + "', '" + this.created_on + "')";
    Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Direct connection without pooling
    Statement stmt = con.createStatement();
    stmt.executeUpdate(sql); // Executing dangerous SQL query
    return true;  // Always returns true, not checking for actual success
  }
}
