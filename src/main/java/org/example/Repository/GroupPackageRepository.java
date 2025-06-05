package org.example.Repository;

import org.example.Entity.GroupPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPackageRepository extends JpaRepository<GroupPackage, Long> {
}
