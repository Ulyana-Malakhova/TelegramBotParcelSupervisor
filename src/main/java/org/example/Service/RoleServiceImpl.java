package org.example.Service;

import org.example.Entity.Role;
import org.example.Entity.Status;
import org.example.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements NameServiceInterface<Role> {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Поиск роли по названию
     *
     * @param name название роли
     * @return сущность роли
     */
    @Override
    public Role findByName(String name) {
        Optional<Role> roleOptional = roleRepository.findByNameRole(name);
        return roleOptional.orElse(null);
    }
}
