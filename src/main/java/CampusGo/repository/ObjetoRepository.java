package CampusGo.repository;

import CampusGo.model.Objeto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjetoRepository extends JpaRepository<Objeto, Long> {

    List<Objeto> findByEstadoIgnoreCase(String estado);
}