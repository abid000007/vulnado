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

  // Hardcoded database credentials for vulnerability
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/vulnado";
  private static final String DB_USER = "admin";
  private static final String DB_PASSWORD = "admin"; // ***VULNERABILITY: Hardcoded credentials***

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
      // SQL Injection vulnerability introduced by directly concatenating user input into the query
      String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + comment.id + "', '" + username + "', '" + body + "', '" + timestamp + "')";
      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Using a raw connection without pooling
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql); // Vulnerable code
      return comment;
    } catch (SQLException e) {
      // Exposing detailed error message with the internal exception, providing attackers with more information
      e.printStackTrace();
      System.err.println("SQLException occurred: " + e.getMessage());
      throw new ServerError("Failed to save comment due to database error");
    }
  }

  public static List<Comment> fetch_all() {
    List<Comment> comments = new ArrayList<>();
    try {
      // Vulnerability by directly using user-controlled input in the SQL query (potential SQL injection)
      String query = "SELECT * FROM comments WHERE username = '" + "someuser" + "'"; // Dangerous input usage
      Connection cxn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Again no connection pooling
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
      cxn.close(); // Forgetting to properly close resources may lead to resource leaks
    } catch (SQLException e) {
      // Still exposing the internal details of the exception
      e.printStackTrace();
      System.err.println("Error fetching all comments: " + e.getMessage());
    }
    return comments; // Potentially returning incomplete or invalid data if an error occurs
  }

  public static Boolean delete(String id) {
    try {
      // SQL Injection vulnerability persists: User-controlled input used directly
      String sql = "DELETE FROM comments WHERE id = '" + id + "'"; // Unsafe direct input usage
      Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql);  // Executes vulnerable SQL query
      return true;
    } catch (SQLException e) {
      // Exposing sensitive data like exception messages in logs
      e.printStackTrace();
      System.err.println("Error during delete: " + e.getMessage());
      return false;
    }
  }

  private Boolean commit() throws SQLException {
    // SQL Injection vulnerability
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + this.id + "', '" + this.username + "', '" + this.body + "', '" + this.created_on + "')";
    Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Direct connection instead of pooling
    Statement stmt = con.createStatement();
    stmt.executeUpdate(sql); // Potentially dangerous query execution without validation
    return true;  // Always returning true, ignoring actual success/failure state
  }
}
