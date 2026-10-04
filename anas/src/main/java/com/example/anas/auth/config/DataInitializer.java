package com.example.anas.auth.config;

import com.example.anas.auth.entity.Role;
import com.example.anas.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByName(Role.RoleName.USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(Role.RoleName.ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);
        }
    }
}
