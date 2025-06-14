package org.example.Repository;

import jakarta.transaction.Transactional;
import org.example.Entity.GroupPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPackageRepository extends JpaRepository<GroupPackage, Long> {
    @Transactional
    @Query("SELECT g FROM GroupPackage g WHERE g.id.groupId = :groupId")
    List<GroupPackage> findByGroupId(@Param("groupId") Long groupId);

}
