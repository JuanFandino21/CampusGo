package CampusGo.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import CampusGo.service.UsuarioService;

/*
 * Configuración del microservicio de AUTENTICACIÓN.
 *
 * Este servicio EMITE tokens, y los firma con la LLAVE PRIVADA
 * del par RSA. Es el único que tiene esa llave.
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


    // ==========================================
    // LLAVES RSA
    // ==========================================

    /*
     * Llave privada. Con ella se FIRMAN los tokens.
     * Solo existe en este microservicio.
     */
    @Bean
    public RSAPrivateKey llavePrivada() throws Exception {

        byte[] bytes = leerPem("keys/private.pem",
                "PRIVATE KEY");

        PKCS8EncodedKeySpec especificacion =
                new PKCS8EncodedKeySpec(bytes);

        return (RSAPrivateKey) KeyFactory
                .getInstance("RSA")
                .generatePrivate(especificacion);
    }

    /*
     * Llave pública. Es la pareja de la privada y sirve
     * únicamente para verificar las firmas.
     */
    @Bean
    public RSAPublicKey llavePublica() throws Exception {

        byte[] bytes = leerPem("keys/public.pem",
                "PUBLIC KEY");

        X509EncodedKeySpec especificacion =
                new X509EncodedKeySpec(bytes);

        return (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(especificacion);
    }

    /*
     * Lee un archivo .pem del classpath, le quita las líneas
     * de encabezado y los saltos de línea, y decodifica
     * el contenido que queda, que está en Base64.
     */
    private byte[] leerPem(String ruta, String etiqueta)
            throws Exception {

        String contenido = new String(
                new ClassPathResource(ruta)
                        .getInputStream()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        String base64 = contenido
                .replace("-----BEGIN " + etiqueta + "-----", "")
                .replace("-----END " + etiqueta + "-----", "")
                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }

    /*
     * Generador de JWT firmados con RS256.
     *
     * El par de llaves se envuelve en un JWK (JSON Web Key),
     * que es el formato estándar que usa Nimbus.
     */
    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey llavePublica,
            RSAPrivateKey llavePrivada) {

        RSAKey jwk = new RSAKey.Builder(llavePublica)
                .privateKey(llavePrivada)
                .build();

        JWKSource<SecurityContext> fuente =
                new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(fuente);
    }
}
