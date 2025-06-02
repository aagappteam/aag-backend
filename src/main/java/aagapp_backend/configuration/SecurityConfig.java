package aagapp_backend.configuration;

import aagapp_backend.components.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.Filter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/swagger-ui.html",               // Swagger UI path
                "/swagger-ui/**",                 // Swagger UI static resources
                "/swagger-resources/**",          // Swagger resource URL
                "/v3/api-docs/**",                // OpenAPI docs path
                "/webjars/**",                    // Webjar resources for Swagger
                "/swagger-resources/**" ,          // Swagger resources
                "/api/**/aagdocument/**",
                "/api/**/files/**",
                "/initate-payment",
                "/.well-known/**",
                "/response",
                "/resp",
                "/enq",
                "/MerchantAcknowledgement",
                "/Bank",
                "/.well-known/**",
                "/vendor/**"
        );
    }
    @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
               .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS configuration
               .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
               .authorizeRequests(auth -> auth
                       .requestMatchers(
                               "/",
                               "/swagger-ui.html",           // Swagger UI path
                               "/swagger-ui/**",             // Swagger UI resources
                               "/swagger-resources/**",      // Swagger resources
                               "/v3/api-docs/**",            // OpenAPI documentation
                               "/webjars/**",                // Webjars for Swagger UI
                               "/files/**",
                               "/images/**",
                               "/aagdocument/**",
                               "/api/aagdocument/**",
                               "/otp/**",
                               "/health/**",
                               "/winning/**",
                               "/account/**",
                               "/test/**",
                               "/ludo-websocket/**" ,         // Allow WebSocket endpoint
                               // ✅ Add your SabPaisa-related endpoints
                               "/initate-payment",
                               "/api/v1/initate-payment",
                               "/response",
                               "/resp",
                               "/enq",
                               "/MerchantAcknowledgement",
                               "/subPaisa/**",
                               "/Bank",
                               "/.well-known/**",
                               "/vendor/**"

                       ).permitAll() // Allow public access to Swagger UI and some other resources
                       .anyRequest().authenticated() // Require authentication for all other paths
               )
               .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // Add JWT filter

       return http.build();
   }
/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
    /**
     * Provides a {@link CorsConfigurationSource} that allows CORS requests from any origin.
     * <p>
     * This configuration allows GET, POST, PUT, DELETE, PATCH, and OPTIONS requests from any origin,
     * with any headers, and allows credentials (cookies, etc.) to be sent.
     * This is useful for testing and development purposes, but should not be used in production.
     * <p>
     * To customize the CORS configuration, you can modify this method or create your own
     * {@link CorsConfigurationSource} bean.
     *
     * @return a {@link CorsConfigurationSource} that allows CORS requests from any origin
     */
/* <<<<<<<<<<  3b5f11de-9a34-4307-a7fa-add4816ab48a  >>>>>>>>>>> */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

   @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

