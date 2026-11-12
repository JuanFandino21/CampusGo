package CampusGo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import CampusGo.model.Objeto;

public interface ObjetoRepository extends JpaRepository<Objeto, Long> {

    List<Objeto> findByEstadoIgnoreCase(String estado);

    List<Objeto> findByNombreContainingIgnoreCase(String nombre);

    /*
     * Cuenta cuántos objetos hay en un estado.
     * La cuenta la hace la base de datos, no Java.
     */
    long countByEstadoIgnoreCase(String estado);

    /*
     * Objetos registrados antes de una fecha.
     */
    List<Objeto> findByFechaRegistroBeforeOrderByFechaRegistroAsc(
            LocalDateTime fecha);

    /*
     * Agrupa los objetos por categoría y cuenta cada grupo.
     * Agrupar no se puede expresar con el nombre del método,
     * así que la consulta se escribe en JPQL.
     */
    @Query("SELECT o.categoria, COUNT(o) FROM Objeto o "
            + "GROUP BY o.categoria ORDER BY COUNT(o) DESC")
    List<Object[]> contarPorCategoria();
}
