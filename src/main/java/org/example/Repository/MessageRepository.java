package org.example.Repository;

import org.example.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId ORDER BY m.date DESC LIMIT 1")
    Optional<Message> findLatestMessageByUserId(@Param("userId") Long userId);
}
