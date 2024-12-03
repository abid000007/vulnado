@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
            .frameOptions().sameOrigin()  // Prevent clickjacking
            .and()
            .xssProtection().block(true) // Enable XSS protection
            .and()
            .contentSecurityPolicy("default-src 'self';"); // Define CSP
    }
}
