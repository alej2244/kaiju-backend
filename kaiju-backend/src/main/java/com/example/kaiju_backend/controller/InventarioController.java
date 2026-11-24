package com.example.kaiju_backend.controller;

import com.example.kaiju_backend.model.Movimiento;
import com.example.kaiju_backend.model.Producto;
import com.example.kaiju_backend.model.Usuario;
import com.example.kaiju_backend.repository.MovimientoRepository;
import com.example.kaiju_backend.repository.ProductoRepository;
import com.example.kaiju_backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    private final ProductoRepository productoRepo;
    private final MovimientoRepository movimientoRepo;
    private final UsuarioRepository usuarioRepo;

    public InventarioController(ProductoRepository productoRepo,
                                MovimientoRepository movimientoRepo,
                                UsuarioRepository usuarioRepo) {
        this.productoRepo = productoRepo;
        this.movimientoRepo = movimientoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // --- ENDPOINT DE AUTENTICACIÓN (LOGIN) ---

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe ingresar usuario y contraseña"));
        }

        return usuarioRepo.findByUsernameAndPassword(username, password)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "rol", user.getRol(),
                        "mensaje", "Inicio de sesión exitoso"
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Usuario o contraseña incorrectos")));
    }

    // --- ENDPOINTS DE PRODUCTOS ---

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoRepo.findAll();
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        return productoRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        Producto guardado = productoRepo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto datos) {
        return productoRepo.findById(id).map(prod -> {
            prod.setCodigo(datos.getCodigo());
            prod.setNombre(datos.getNombre());
            prod.setCategoria(datos.getCategoria());
            prod.setPrecio(datos.getPrecio());
            prod.setStock(datos.getStock());
            prod.setStockMinimo(datos.getStockMinimo());
            return ResponseEntity.ok(productoRepo.save(prod));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!productoRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS DE MOVIMIENTOS ---

    @GetMapping("/productos/{id}/movimientos")
    public List<Movimiento> listarMovimientos(@PathVariable Long id) {
        return movimientoRepo.findByProductoIdOrderByIdDesc(id);
    }

    @PostMapping("/movimientos")
    public ResponseEntity<?> registrarMovimiento(@Valid @RequestBody Movimiento movimiento) {
        return productoRepo.findById(movimiento.getProductoId()).map(prod -> {
            int nuevoStock = movimiento.getTipo().equalsIgnoreCase("ENTRADA")
                    ? prod.getStock() + movimiento.getCantidad()
                    : prod.getStock() - movimiento.getCantidad();

            if (nuevoStock < 0) {
                return ResponseEntity.badRequest().body("Stock insuficiente para realizar la salida");
            }

            prod.setStock(nuevoStock);
            productoRepo.save(prod);

            if (movimiento.getFecha() == null || movimiento.getFecha().isBlank()) {
                movimiento.setFecha(LocalDateTime.now().toString());
            }
            Movimiento guardado = movimientoRepo.save(movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        }).orElse(ResponseEntity.notFound().build());
    }
}