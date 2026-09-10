package CampusGo.service;

import CampusGo.model.Objeto;
import CampusGo.repository.ObjetoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ObjetoService {

    private final ObjetoRepository objetoRepository;

    public ObjetoService(ObjetoRepository objetoRepository) {
        this.objetoRepository = objetoRepository;
    }

    public List<Objeto> listarObjetos() {
        return objetoRepository.findAll();
    }

    public Optional<Objeto> buscarPorId(Long id) {
        return objetoRepository.findById(id);
    }

    public Objeto guardarObjeto(Objeto objeto) {
        return objetoRepository.save(objeto);
    }

    public Optional<Objeto> actualizarObjeto(Long id, Objeto datosObjeto) {

        Optional<Objeto> objetoEncontrado = objetoRepository.findById(id);

        if (objetoEncontrado.isPresent()) {

            Objeto objeto = objetoEncontrado.get();

            objeto.setNombre(datosObjeto.getNombre());
            objeto.setDescripcion(datosObjeto.getDescripcion());
            objeto.setCategoria(datosObjeto.getCategoria());
            objeto.setUbicacion(datosObjeto.getUbicacion());
            objeto.setEstado(datosObjeto.getEstado());

            return Optional.of(objetoRepository.save(objeto));
        }

        return Optional.empty();
    }

    public boolean eliminarObjeto(Long id) {

        if (objetoRepository.existsById(id)) {
            objetoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    public List<Objeto> listarPorEstado(String estado) {
        return objetoRepository.findByEstadoIgnoreCase(estado);
    }
}