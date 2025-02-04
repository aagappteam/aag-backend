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

/*    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS configuration
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                .authorizeRequests()
                .requestMatchers("/ludo-websocket/**").permitAll()
                .anyRequest().authenticated();

        return http.build();
    }*/


       @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
               .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS configuration
               .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
               .authorizeRequests(auth -> auth
                       .requestMatchers(
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
                               "/account/**",
                               "/test/**",
                               "/ludo-websocket/**"          // Allow WebSocket endpoint

                       ).permitAll() // Allow public access to Swagger UI and some other resources
                       .anyRequest().authenticated() // Require authentication for all other paths
               )
               .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // Add JWT filter

       return http.build();
   }
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

