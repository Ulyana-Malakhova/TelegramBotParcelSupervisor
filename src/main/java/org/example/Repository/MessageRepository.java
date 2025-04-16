package org.example.Repository;

import org.example.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId ORDER BY m.date DESC LIMIT 1")
    Optional<Message> findLatestMessageByUserId(@Param("userId") Long userId);
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND (m.text LIKE '%/track%' OR m.text LIKE '%/history%' OR m.text LIKE '%/add_name%' OR m.text LIKE '%/traceability_track%') AND m.date >= :date ORDER BY m.date DESC")
    List<Message> findLatestMessagesWithTrackingNumbers(@Param("userId") Long userId, @Param("date") Date date);
}
