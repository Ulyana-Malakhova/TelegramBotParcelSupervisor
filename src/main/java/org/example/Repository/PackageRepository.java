package org.example.Repository;

import jakarta.transaction.Transactional;
import org.example.Entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для посылок
 */
@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    /**
     * Получение всех сохраненных пользователем посылок
     * @param id id пользователя
     * @return список сущностей посылок пользователя
     */
    @Query("SELECT p FROM Package p WHERE p.userEntity.id = :id")
    List<Package> findByUserIdEntity(@Param("id") Long id);

    /**
     * Удаление имени посылки у пользователя
     * @param id id пользователя
     * @param name имя посылки
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Package p WHERE p.namePackage = :name AND p.userEntity.id = :id")
    void deleteByIdAndName(@Param("id") Long id, @Param("name") String name);

    /**
     * Получение посылки по id пользователя и имени
     * @param userId id пользователя
     * @param name имя посылки
     * @return сущность посылки
     */
    @Query("SELECT p FROM Package p WHERE p.namePackage = :name AND p.userEntity.id = :userId")
    Optional<Package> findByNamePackageAndUserId(@Param("userId") Long userId, @Param("name") String name);

    /**
     * Получение посылки по id пользователя и трек-номеру
     * @param userId id пользователя
     * @param track трек-номер посылки
     * @return сущность посылки
     */
    @Query("SELECT p FROM Package p WHERE p.trackNumber = :track AND p.userEntity.id = :userId")
    Optional<Package> findByTrackNumberAndUserId(@Param("userId") Long userId, @Param("track") String track);

    /**
     * Получение посылок за заданный промежуток времени
     * @param period дата с которой выбираем посылки
     * @return список сущностей посылки
     */
    @Query("SELECT e FROM Package e WHERE e.departureDate >= :period")
    List<Package> findByPeriod(@Param("period") Date period);
}
