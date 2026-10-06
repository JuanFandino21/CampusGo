package CampusGo.service;

import CampusGo.model.Usuario;
import CampusGo.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByUsername(username);

        if (usuarioEncontrado.isEmpty()) {
            throw new UsernameNotFoundException(
                    "Usuario no encontrado: " + username);
        }

        Usuario usuario = usuarioEncontrado.get();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }
}