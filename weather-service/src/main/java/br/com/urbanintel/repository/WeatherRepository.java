package br.com.urbanintel.repository;

import br.com.urbanintel.model.WeatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherRecord, Long> {

    Optional<WeatherRecord> findTopByCityOrderByRecordedAtDesc(String city);

    List<WeatherRecord> findByCityAndRecordedAtBetweenOrderByRecordedAtDesc(
            String city, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT w FROM WeatherRecord w
        WHERE w.rainMm >= :threshold
          AND w.recordedAt >= :since
        ORDER BY w.rainMm DESC
    """)
    List<WeatherRecord> findHeavyRainSince(
            @Param("threshold") Double threshold,
            @Param("since") LocalDateTime since);

    @Query("""
        SELECT AVG(w.rainMm) FROM WeatherRecord w
        WHERE w.city = :city
          AND EXTRACT(HOUR FROM w.recordedAt) = :hour
          AND w.recordedAt >= :since
    """)
    Double avgRainForHour(
            @Param("city") String city,
            @Param("hour") int hour,
            @Param("since") LocalDateTime since);

    @Query("""
        SELECT w FROM WeatherRecord w
        WHERE w.city = :city
          AND w.condition IN ('RAIN','STORM')
          AND w.recordedAt >= :since
        ORDER BY w.recordedAt DESC
    """)
    List<WeatherRecord> findRainEventsByCity(
            @Param("city") String city,
            @Param("since") LocalDateTime since);
}
