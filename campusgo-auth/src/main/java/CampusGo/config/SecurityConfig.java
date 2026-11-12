package CampusGo.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import CampusGo.service.UsuarioService;

/*
 * Configuración del microservicio de AUTENTICACIÓN.
 *
 * Este servicio EMITE tokens. Por eso tiene JwtEncoder,
 * pero no necesita JwtDecoder: él no valida tokens ajenos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * Codificador utilizado para almacenar y verificar
     * las contraseñas de los usuarios.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Administrador encargado de autenticar usuario y contraseña.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(usuarioService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    /*
     * Permite que la interfaz web, que vive en el microservicio
     * de objetos y por lo tanto en otro origen, pueda llamar
     * a este servicio desde el navegador.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOriginPatterns(List.of("*"));
        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration("/**", configuracion);

        return fuente;
    }

    /*
     * Configuración general de seguridad del servicio.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/api/auth/**",
                                "/h2-console/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .headers(headers ->
                        headers.frameOptions(frame ->
                                frame.sameOrigin())
                );

        return http.build();
    }

    /*
     * Convierte la clave secreta de configuración
     * en una SecretKey utilizada por HS256.
     */
    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}") String secret) {

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 32 bytes.");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /*
     * Generador de JWT.
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {

        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
}
