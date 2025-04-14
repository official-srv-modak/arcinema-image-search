package com.modakdev.arcinema_image_search.models;

import com.modakdev.arcinema_image_search.models.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrailerRepository extends JpaRepository<Trailer, Long> {

    Optional<Trailer> findByMovieName(String movieName);

    @Query("SELECT t.url FROM Trailer t WHERE t.movieName = :movieName")
    Optional<String> findUrlByMovieName(@Param("movieName") String movieName);

}