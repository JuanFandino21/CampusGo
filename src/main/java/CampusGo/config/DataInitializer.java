package CampusGo.config;

import CampusGo.model.Usuario;
import CampusGo.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsuario(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.findByUsername("admin").isEmpty()) {

                Usuario usuario = new Usuario();

                usuario.setUsername("admin");

                usuario.setPassword(
                        passwordEncoder.encode("CampusGo123"));

                usuario.setRol("USER");

                usuarioRepository.save(usuario);
            }
        };
    }
}