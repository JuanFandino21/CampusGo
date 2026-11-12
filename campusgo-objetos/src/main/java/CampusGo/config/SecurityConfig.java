package CampusGo.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;

/*
 * Configuración del microservicio de OBJETOS.
 *
 * Este servicio NO emite tokens y NO conoce usuarios ni contraseñas.
 * Solo VERIFICA los tokens que emitió el microservicio de autenticación,
 * usando la LLAVE PÚBLICA del par RSA.
 *
 * La llave privada no existe aquí, así que este servicio es incapaz
 * de firmar un token aunque fuera vulnerado. Esa es la ventaja
 * del cifrado asimétrico frente a una clave secreta compartida.
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


    // ==========================================
    // LLAVE RSA PÚBLICA
    // ==========================================

    /*
     * Única llave que necesita este servicio.
     * Es la pareja pública de la privada que firma en auth.
     */
    @Bean
    public RSAPublicKey llavePublica() throws Exception {

        String contenido = new String(
                new ClassPathResource("keys/public.pem")
                        .getInputStream()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        String base64 = contenido
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] bytes = Base64.getDecoder().decode(base64);

        X509EncodedKeySpec especificacion =
                new X509EncodedKeySpec(bytes);

        return (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(especificacion);
    }

    /*
     * Validador de JWT. Comprueba la firma con la llave pública
     * y revisa la expiración, sin llamar al servicio de
     * autenticación en ningún momento.
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey llavePublica) {

        return NimbusJwtDecoder
                .withPublicKey(llavePublica)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
}
