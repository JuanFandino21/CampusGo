package CampusGo.controller;

import CampusGo.model.Objeto;
import CampusGo.service.ObjetoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/objetos")
public class ObjetoController {

    private final ObjetoService objetoService;

    public ObjetoController(ObjetoService objetoService) {
        this.objetoService = objetoService;
    }

    @GetMapping
    public ResponseEntity<List<Objeto>> listarObjetos() {
        return ResponseEntity.ok(objetoService.listarObjetos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Objeto> buscarPorId(@PathVariable Long id) {

        Optional<Objeto> objeto = objetoService.buscarPorId(id);

        if (objeto.isPresent()) {
            return ResponseEntity.ok(objeto.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/estado")
    public ResponseEntity<List<Objeto>> listarPorEstado(
            @RequestParam String valor) {

        return ResponseEntity.ok(objetoService.listarPorEstado(valor));
    }

    @PostMapping
    public ResponseEntity<Objeto> guardarObjeto(
            @Valid @RequestBody Objeto objeto) {

        Objeto objetoGuardado = objetoService.guardarObjeto(objeto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(objetoGuardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objeto> actualizarObjeto(
            @PathVariable Long id,
            @Valid @RequestBody Objeto datosObjeto) {

        Optional<Objeto> objetoActualizado =
                objetoService.actualizarObjeto(id, datosObjeto);

        if (objetoActualizado.isPresent()) {
            return ResponseEntity.ok(objetoActualizado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarObjeto(@PathVariable Long id) {

        boolean eliminado = objetoService.eliminarObjeto(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}