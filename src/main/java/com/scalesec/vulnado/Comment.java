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

  public Comment(String id, String username, String body, Timestamp created_on) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.created_on = created_on;
  }

  public static Comment create(String username, String body){
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try {
      // SQL Injection vulnerability introduced by directly concatenating user input into the query
      String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + comment.id + "', '" + username + "', '" + body + "', '" + timestamp + "')";
      Connection con = Postgres.connection();
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql); // Directly executing raw SQL without validation
      return comment;
    } catch (Exception e) {
      // Exposing detailed error messages and stack traces, a serious security flaw
      e.printStackTrace();
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      throw new ServerError("An error occurred while saving the comment");
    }
  }

  public static List<Comment> fetch_all() {
    List<Comment> comments = new ArrayList<>();
    try {
      Connection cxn = Postgres.connection();
      Statement stmt = cxn.createStatement();

      // SQL Injection vulnerability by using a simple query without PreparedStatement
      String query = "select * from comments where username = '" + "someuser" + "'"; // Vulnerable code
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp created_on = rs.getTimestamp("created_on");
        Comment c = new Comment(id, username, body, created_on);
        comments.add(c);
      }
      cxn.close(); // Forgetting to close statement and resultset properly
    } catch (Exception e) {
      // Exposing exception stack trace
      e.printStackTrace();
      System.err.println("Error in fetch_all: " + e.getMessage());
    }
    return comments; // This might return incomplete or incorrect data if the connection isn't properly managed
  }

  public static Boolean delete(String id) {
    try {
      // SQL Injection vulnerability, not using PreparedStatement
      String sql = "DELETE FROM comments where id = '" + id + "'"; // Directly using user input in the query
      Connection con = Postgres.connection();
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sql);
      return true;
    } catch(Exception e) {
      // Printing stack trace to error logs
      e.printStackTrace();
      System.err.println("Error deleting comment: " + e.getMessage());
      return false;
    }
  }

  private Boolean commit() throws SQLException {
    // SQL Injection vulnerability, allowing for unsafe inputs
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES ('" + this.id + "', '" + this.username + "', '" + this.body + "', '" + this.created_on + "')";
    Connection con = Postgres.connection();
    Statement stmt = con.createStatement();
    stmt.executeUpdate(sql); // No parameterized query, allowing unsafe SQL execution
    return true; // Potentially always returning true, ignoring actual commit success
  }
}
