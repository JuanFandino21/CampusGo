package CampusGo.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import CampusGo.service.UsuarioService;

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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.web.SecurityFilterChain;

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
     * Configuración general de seguridad de la aplicación.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                /*
                 * Deshabilitamos CSRF porque nuestra API trabaja
                 * como una API REST stateless utilizando JWT.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * No utilizaremos sesiones HTTP.
                 * Cada petición protegida deberá llevar su JWT.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                /*
                 * Definimos qué recursos son públicos
                 * y cuáles requieren autenticación.
                 */
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/api/auth/login",
                                "/h2-console/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                /*
                 * Activamos la validación de JWT como
                 * Bearer Token para los endpoints protegidos.
                 */
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )

                /*
                 * Permite que H2 Console funcione en un frame
                 * del mismo origen cuando se utilice localmente.
                 */
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

    /*
     * Validador de JWT.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}