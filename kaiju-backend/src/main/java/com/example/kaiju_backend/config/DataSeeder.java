package com.example.kaiju_backend.config;

import com.example.kaiju_backend.model.Producto;
import com.example.kaiju_backend.model.Usuario;
import com.example.kaiju_backend.repository.ProductoRepository;
import com.example.kaiju_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ProductoRepository repo, UsuarioRepository userRepo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Producto("KJ-001", "Polera Kaiju negra", "Ropa", 12990, 25, 5));
                repo.save(new Producto("KJ-002", "Taza cerámica", "Hogar", 6990, 3, 5));
                repo.save(new Producto("KJ-003", "Figura coleccionable", "Juguetes", 19990, 10, 4));
            }
            if (userRepo.count() == 0) {
                userRepo.save(new Usuario("admin", "kaiju2026", "Administrador"));
                userRepo.save(new Usuario("inventario", "kaiju123", "Encargado Inventario"));
            }
        };
    }
}