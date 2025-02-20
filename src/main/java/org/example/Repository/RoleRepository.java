package org.example.Repository;

import org.example.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для роли
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Получение сущности роли по названию
     * @param role название роли
     * @return сущность роли
     */
    Optional<Role> findByNameRole(String role);
}
