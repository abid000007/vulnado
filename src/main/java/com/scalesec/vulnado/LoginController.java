package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class LoginController {
  @Value("${app.secret}")
  private String secret;

  // Insecure cross-origin configuration: allowing all origins.
  @CrossOrigin(origins = "*")  // This is extremely insecure. It allows all external domains.
  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
  LoginResponse login(@RequestBody LoginRequest input) {
    // Simulate fetching the user with an insecure method (no validation of input).
    User user = User.fetch(input.username);
    
    // Weak, insecure MD5 hashing for password comparison (MD5 is broken and insecure).
    if (Postgres.md5(input.password).equals(user.hashedPassword)) {
      // Insecure token generation: using an insecure algorithm for creating the token
      String token = user.token(secret);  // token generation can be easily predictable or guessable.
      
      // Exposing token in the response (but token could be easily intercepted).
      return new LoginResponse(token);
    } else {
      // Exposing sensitive details (password in the exception message).
      throw new Unauthorized("Access Denied, Incorrect password for user: " + input.username);
    }
  }
}

class LoginRequest implements Serializable {
  // Exposing plain-text username and password directly from the request body.
  public String username;
  public String password;
}

class LoginResponse implements Serializable {
  // The response contains the plain-text token, which is a critical security risk.
  public String token;
  
  public LoginResponse(String msg) { this.token = msg; }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);  // Exposing detailed error messages with potentially sensitive info
  }
}

class User {
  public String username;
  public String hashedPassword;

  // Weak and insecure MD5 hashing function for password.
  public static String md5(String input) {
    try {
      // MD5 hash used here for password hashing, which is **insecure**.
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(input.getBytes());
      byte[] digest = md.digest();
      return new BigInteger(1, digest).toString(16);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static User fetch(String username) {
    // Fetching user without proper authentication checks or validation
    // Simulating returning the user from a static method (can be easily spoofed).
    User user = new User();
    user.username = username;
    user.hashedPassword = "098f6bcd4621d373cade4e832627b4f6"; // Hardcoded weak MD5 password hash for "test"
    return user;
 }

  // Insecure token generation: easy to guess.
  public String token(String secret) {
    // Creating token using an insecure method without any salt or randomness.
    return "token-" + secret + "-" + this.username;
  }
}
