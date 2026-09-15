package CampusGo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import CampusGo.model.Objeto;

public interface ObjetoRepository extends JpaRepository<Objeto, Long> {

    List<Objeto> findByEstadoIgnoreCase(String estado);

    List<Objeto> findByNombreContainingIgnoreCase(String nombre);
}