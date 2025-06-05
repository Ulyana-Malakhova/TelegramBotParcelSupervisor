package org.example.Repository;

import org.example.Entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    /**
     * Получение последнего отправленного сообщения
     * @param userId id пользователя
     * @return optional-объект последнего сообщения
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId ORDER BY m.date DESC LIMIT 1")
    Optional<Message> findLatestMessageByUserId(@Param("userId") Long userId);

    /**
     * Получение списка сообщений, в которых есть команды, связанные с трекингом
     * @param userId id пользователя
     * @param date дата, с которой начинаем поиск
     * @return список сообщений
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND (m.text LIKE '%/track%' OR m.text LIKE '%/history%' OR m.text LIKE '%/add_name%' OR m.text LIKE '%/traceability_track%') AND m.date >= :date ORDER BY m.date DESC")
    List<Message> findLatestMessagesWithTrackingNumbers(@Param("userId") Long userId, @Param("date") Date date);

    /**
     * Получение списка самых активных пользователей
     * @param pageable количество пользователей
     * @return список пользователей
     */
    @Query("SELECT m.user.id, COUNT(m) as cnt FROM Message m GROUP BY m.user.id ORDER BY cnt DESC")
    List<Object[]> findTopUsers(Pageable pageable);

    /**
     * Получение всех дат из таблицы
     * @return список дат
     */
    @Query("SELECT m.date FROM Message m")
    List<Timestamp> findAllDate();

    /**
     * Получение количества сообщений начинающихся с символа слэш
     * @param userId идентификатор проверяемого пользователя
     * @return количество сообщений
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.text LIKE '/%' and m.user.id = :userId")
    long countMessagesStartingWithSlash(@Param("userId") Long userId);

    /**
     * Получение количества сообщений не начинающихся с символа слэш
     * @param userId идентификатор проверяемого пользователя
     * @return количество сообщений
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.text NOT LIKE '/%' and m.user.id = :userId")
    long countMessagesNotStartingWithSlash(@Param("userId") Long userId);
}
