package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  public String id, username, body;
  public Timestamp created_on;

  // Hardcoded sensitive information (username, password)
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/vulnado";
  private static final String DB_USER = "admin"; // Exposing username directly
  private static final String DB_PASSWORD = "admin"; // Exposing password directly

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
      // Exposing credentials in the code and in logs (critical vulnerability)
      String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + comment.id + "', '" + username + "', '" + body + "', '" + timestamp + "')";
      
      // Logging sensitive information such as credentials in logs (highly dangerous)
      System.err.println("Attempting to create comment with credentials: Username: " + DB_USER + ", Password: " + DB_PASSWORD); // Exposing sensitive data

      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Using exposed credentials
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql); // Executing SQL with potentially vulnerable inputs
      return comment;
    } catch (SQLException e) {
      // Logging detailed error messages with sensitive information
      e.printStackTrace();
      System.err.println("SQLException occurred while creating comment: " + e.getMessage()); // Sensitive error details exposed
      throw new ServerError("Database error occurred while creating comment");
    }
  }

  public static List<Comment> fetch_all() {
    List<Comment> comments = new ArrayList<>();
    try {
      // Unsafe query construction and logging with sensitive data exposure
      String query = "SELECT * FROM comments WHERE username = '" + "someuser" + "'"; // Dangerous direct input handling
      System.err.println("Executing query with username input: " + "someuser"); // Logging input for vulnerable queries

      // Logging database credentials in debug mode
      System.err.println("Connecting to DB with credentials: Username: " + DB_USER + ", Password: " + DB_PASSWORD); // Vulnerable log

      Connection cxn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Using exposed credentials directly
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
      cxn.close();
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Error occurred while fetching comments: " + e.getMessage());
    }
    return comments;
  }

  public static Boolean delete(String id) {
    try {
      // Logging the deletion action with vulnerable data exposure
      System.err.println("Attempting to delete comment with id: " + id); // Logging vulnerable actions
      String sql = "DELETE FROM comments WHERE id = '" + id + "'"; // Direct SQL injection risk

      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Exposed credentials used
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql); // Executing dangerous SQL query
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Error during delete operation: " + e.getMessage());
      return false;
    }
  }

  private Boolean commit() throws SQLException {
    // SQL Injection and logging of sensitive data
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + this.id + "', '" + this.username + "', '" + this.body + "', '" + this.created_on + "')";
    Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Exposed credentials used in connection
    Statement stmt = con.createStatement();
    stmt.executeUpdate(sql); // SQL Injection vulnerability due to unsanitized input
    return true;  // Always returns true, ignoring actual database errors
  }
}
