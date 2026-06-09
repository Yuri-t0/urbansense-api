package br.com.urbanintel.infrastructure.adapter.out.persistence;

import br.com.urbanintel.domain.model.TransitLine;
import br.com.urbanintel.domain.port.out.TransitLineRepositoryPort;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transit_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class TransitLineJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String operator;
    private String color;
    private Double rainVulnerability;
    private Double rainThresholdMm;
    private String vulnerableSection;
    private Integer avgDelayPctOnRain;
}

@Entity
@Table(name = "transit_impacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class TransitImpactJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private TransitLineJpaEntity line;
    private String impactType;
    private Double rainMmAtEvent;
    private LocalDateTime occurredAt;
    private boolean active;
}

@Repository
interface TransitLineJpaRepository extends JpaRepository<TransitLineJpaEntity, Long> {
    @Query("SELECT t FROM TransitLineJpaEntity t WHERE t.rainThresholdMm <= :rainMm ORDER BY t.rainVulnerability DESC")
    List<TransitLineJpaEntity> findLinesAtRisk(@Param("rainMm") Double rainMm);
}

@Repository
interface TransitImpactJpaRepository extends JpaRepository<TransitImpactJpaEntity, Long> {
    @Query("""
        SELECT t FROM TransitImpactJpaEntity t
        WHERE t.line.code = :lineCode
          AND t.rainMmAtEvent >= :minRain
          AND t.occurredAt >= :since
    """)
    List<TransitImpactJpaEntity> findRainImpacts(
            @Param("lineCode") String lineCode,
            @Param("minRain") Double minRain,
            @Param("since") LocalDateTime since);
}

@Component
@RequiredArgsConstructor
public class TransitLinePersistenceAdapter implements TransitLineRepositoryPort {

    private final TransitLineJpaRepository lineRepo;
    private final TransitImpactJpaRepository impactRepo;

    @Override
    public List<TransitLine> findLinesAtRisk(double rainMm) {
        return lineRepo.findLinesAtRisk(rainMm).stream().map(this::toDomain).toList();
    }

    @Override
    public List<TransitLine> findAll() {
        return lineRepo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public int countRainIncidentsByLine(String lineCode, int days) {
        return impactRepo.findRainImpacts(lineCode, 5.0, LocalDateTime.now().minusDays(days)).size();
    }

    private TransitLine toDomain(TransitLineJpaEntity e) {
        return new TransitLine(e.getId(), e.getCode(), e.getName(), e.getOperator(),
                e.getColor(), e.getRainVulnerability(), e.getRainThresholdMm(),
                e.getVulnerableSection(), e.getAvgDelayPctOnRain());
    }
}
