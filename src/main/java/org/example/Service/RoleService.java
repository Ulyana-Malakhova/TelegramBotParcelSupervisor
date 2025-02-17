package org.example.Service;

import org.example.Entity.Role;
import org.example.Entity.Status;
import org.example.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    /**
     * Поиск роли по названию
     * @param role название роли
     * @return сущность роли
     */
    public Role findByNameRole(String role){
        Optional<Role> roleOptional = roleRepository.findByNameRole(role);
        return roleOptional.orElse(null);
    }
}
