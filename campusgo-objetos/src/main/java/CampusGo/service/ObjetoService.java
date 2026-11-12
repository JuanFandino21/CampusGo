package CampusGo.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import CampusGo.model.Objeto;
import CampusGo.repository.ObjetoRepository;

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

    public List<Objeto> buscarPorNombre(String nombre) {
        return objetoRepository.findByNombreContainingIgnoreCase(nombre);
    }


    // ==========================================
    // ENTREGA DEL OBJETO
    // ==========================================

    public Optional<Objeto> entregarObjeto(Long id, String persona) {

        Optional<Objeto> objetoEncontrado = objetoRepository.findById(id);

        if (objetoEncontrado.isPresent()) {

            Objeto objeto = objetoEncontrado.get();

            objeto.setEstado("ENTREGADO");
            objeto.setEntregadoA(persona);
            objeto.setFechaEntrega(LocalDateTime.now());

            return Optional.of(objetoRepository.save(objeto));
        }

        return Optional.empty();
    }


    // ==========================================
    // OBJETOS ANTIGUOS
    // ==========================================

    public List<Objeto> buscarAntiguos(int dias) {

        LocalDateTime limite = LocalDateTime.now().minusDays(dias);

        return objetoRepository
                .findByFechaRegistroBeforeOrderByFechaRegistroAsc(limite);
    }


    // ==========================================
    // RESUMEN DEL INVENTARIO
    // ==========================================

    public Map<String, Object> obtenerResumen() {

        Map<String, Long> porCategoria = new LinkedHashMap<>();

        for (Object[] fila : objetoRepository.contarPorCategoria()) {

            String categoria = (String) fila[0];
            Long cantidad = (Long) fila[1];

            porCategoria.put(categoria, cantidad);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();

        resumen.put("total", objetoRepository.count());
        resumen.put("perdidos",
                objetoRepository.countByEstadoIgnoreCase("PERDIDO"));
        resumen.put("encontrados",
                objetoRepository.countByEstadoIgnoreCase("ENCONTRADO"));
        resumen.put("entregados",
                objetoRepository.countByEstadoIgnoreCase("ENTREGADO"));
        resumen.put("porCategoria", porCategoria);

        return resumen;
    }
}
