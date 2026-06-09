package br.com.urbanintel.infrastructure.adapter.out.persistence;

import br.com.urbanintel.domain.model.Alert;
import br.com.urbanintel.domain.port.out.AlertRepositoryPort;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AlertJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String severity;
    @Column(nullable = false, length = 512) private String message;
    private String region;
    private Double lat;
    private Double lon;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean active;
    @Column(nullable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    static AlertJpaEntity fromDomain(Alert a) {
        return AlertJpaEntity.builder()
                .id(a.id()).city(a.city()).type(a.type().name())
                .severity(a.severity().name()).message(a.message())
                .region(a.region()).lat(a.lat()).lon(a.lon())
                .validFrom(a.validFrom()).validUntil(a.validUntil())
                .active(a.active()).build();
    }

    Alert toDomain() {
        return new Alert(id, city,
                Alert.AlertType.valueOf(type), Alert.Severity.valueOf(severity),
                message, region, lat, lon, validFrom, validUntil, active);
    }
}

@Repository
interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, Long> {

    @Query("""
        SELECT a FROM AlertJpaEntity a
        WHERE a.city = :city AND a.active = true
          AND (a.validUntil IS NULL OR a.validUntil > :now)
        ORDER BY
          CASE a.severity
            WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2
            WHEN 'MEDIUM'   THEN 3 ELSE 4
          END
    """)
    List<AlertJpaEntity> findActiveByCityOrderBySeverity(
            @Param("city") String city, @Param("now") LocalDateTime now);

    List<AlertJpaEntity> findByTypeAndActiveTrue(String type);

    @Modifying
    @Query("UPDATE AlertJpaEntity a SET a.active = false WHERE a.validUntil < :now AND a.active = true")
    void deactivateExpired(@Param("now") LocalDateTime now);
}

@Component
@RequiredArgsConstructor
class AlertPersistenceAdapter implements AlertRepositoryPort {

    private final AlertJpaRepository jpaRepository;

    @Override
    public Alert save(Alert alert) {
        return jpaRepository.save(AlertJpaEntity.fromDomain(alert)).toDomain();
    }

    @Override
    public List<Alert> findActiveByCityOrderBySeverity(String city, LocalDateTime now) {
        return jpaRepository.findActiveByCityOrderBySeverity(city, now)
                .stream().map(AlertJpaEntity::toDomain).toList();
    }

    @Override
    public List<Alert> findByTypeAndActive(Alert.AlertType type) {
        return jpaRepository.findByTypeAndActiveTrue(type.name())
                .stream().map(AlertJpaEntity::toDomain).toList();
    }

    @Override
    public void deactivateExpired(LocalDateTime now) {
        jpaRepository.deactivateExpired(now);
    }
}
