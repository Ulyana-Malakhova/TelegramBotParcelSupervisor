package org.example.Repository;

import jakarta.transaction.Transactional;
import org.example.Entity.Package;
import org.example.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    @Query("SELECT p FROM Package p WHERE p.userEntity.id = :id")
    List<Package> findByUserIdEntity(@Param("id") Long id);
    @Modifying
    @Transactional
    @Query("DELETE FROM Package p WHERE p.namePackage = :name AND p.userEntity.id = :id")
    void deleteByIdAndName(@Param("id") Long id, @Param("name") String name);
    @Query("SELECT p FROM Package p WHERE p.namePackage = :name AND p.userEntity.id = :userId")
    Optional<Package> findByNamePackageAndUserId(@Param("userId") Long userId, @Param("name") String name);
}
