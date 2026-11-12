package CampusGo.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;

/*
 * Configuración del microservicio de OBJETOS.
 *
 * Este servicio NO emite tokens y NO conoce usuarios ni contraseñas.
 * Solo VALIDA los tokens que emitió el microservicio de autenticación,
 * comprobando la firma con la misma clave secreta.
 *
 * Por eso aquí hay JwtDecoder pero no JwtEncoder, y no existen
 * ni BCrypt ni AuthenticationManager: este servicio no autentica a nadie.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/h2-console/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )

                .headers(headers ->
                        headers.frameOptions(frame ->
                                frame.sameOrigin())
                );

        return http.build();
    }

    /*
     * La misma clave secreta que usa el servicio de autenticación.
     * Se comparte por variable de entorno, nunca en el código.
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
     * Validador de JWT. Comprueba la firma y la expiración
     * sin necesidad de llamar al servicio de autenticación.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
