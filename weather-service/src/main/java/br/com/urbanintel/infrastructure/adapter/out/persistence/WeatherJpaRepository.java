package br.com.urbanintel.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface WeatherJpaRepository extends JpaRepository<WeatherJpaEntity, Long> {

    Optional<WeatherJpaEntity> findTopByCityOrderByRecordedAtDesc(String city);

    @Query("""
        SELECT w FROM WeatherJpaEntity w
        WHERE w.rainMm >= :threshold
          AND w.recordedAt >= :since
        ORDER BY w.rainMm DESC
    """)
    List<WeatherJpaEntity> findHeavyRainSince(
            @Param("threshold") Double threshold,
            @Param("since") LocalDateTime since);

    @Query("""
        SELECT AVG(w.rainMm) FROM WeatherJpaEntity w
        WHERE w.city = :city
          AND EXTRACT(HOUR FROM w.recordedAt) = :hour
          AND w.recordedAt >= :since
    """)
    Double avgRainForHour(@Param("city") String city,
                          @Param("hour") int hour,
                          @Param("since") LocalDateTime since);

    @Query("""
        SELECT w FROM WeatherJpaEntity w
        WHERE w.city = :city
          AND w.condition IN ('RAIN','STORM')
          AND w.recordedAt >= :since
        ORDER BY w.recordedAt DESC
    """)
    List<WeatherJpaEntity> findRainEventsByCity(@Param("city") String city,
                                                @Param("since") LocalDateTime since);
}
