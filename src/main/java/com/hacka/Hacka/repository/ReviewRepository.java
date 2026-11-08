package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndHackathonId(Long userId, Long hackathonId);
    List<Review> findByHackathonIdOrderByCreatedAtDesc(Long hackathonId);

    @Query("select avg(r.rating) from Review r where r.hackathon.id = :hid")
    Double averageRating(@Param("hid") Long hackathonId);
}