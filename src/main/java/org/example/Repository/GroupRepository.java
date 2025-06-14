package org.example.Repository;

import jakarta.transaction.Transactional;
import org.example.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Transactional
    @Query("SELECT g FROM Group g WHERE g.text = :name AND g.user.id = :id")
    Group findByIdAndName(@Param("name") String name, @Param("id") Long id);
}
