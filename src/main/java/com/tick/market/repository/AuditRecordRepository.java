package com.tick.market.repository;

import com.tick.market.entity.AuditRecord;
import com.tick.market.entity.AuditStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for persisting and querying ingestion audit records.
 */
@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    /**
     * Retrieve all audit entries recorded for a specific event identifier, ordered newest first.
     *
     * @param eventId the event identifier
     * @return list of audit records
     */
    List<AuditRecord> findByEventIdOrderByRecordedAtDesc(String eventId);

    /**
     * Paginated retrieval of audit records filtered by audit status.
     *
     * @param status   the status to filter by
     * @param pageable pagination parameters
     * @return page of audit records
     */
    Page<AuditRecord> findByStatus(AuditStatus status, Pageable pageable);
}
