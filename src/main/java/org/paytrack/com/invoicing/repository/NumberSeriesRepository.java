package org.paytrack.com.invoicing.repository;

import jakarta.persistence.LockModeType;
import org.paytrack.com.invoicing.domain.NumberSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NumberSeriesRepository extends JpaRepository<NumberSeries, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM NumberSeries s WHERE s.organisationId = :orgId AND s.financialYear = :fy AND s.seriesKey = :key")
    Optional<NumberSeries> findForUpdate(
        @Param("orgId") UUID orgId,
        @Param("fy") String financialYear,
        @Param("key") String seriesKey
    );

    Optional<NumberSeries> findByOrganisationIdAndFinancialYearAndSeriesKey(
        UUID organisationId,
        String financialYear,
        String seriesKey
    );
}
