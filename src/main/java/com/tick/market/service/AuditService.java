package com.tick.market.service;

import com.tick.market.entity.AuditOperation;
import com.tick.market.entity.AuditRecord;
import com.tick.market.entity.AuditStatus;
import java.util.List;

/**
 * Service contract for persisting and retrieving audit trails for market event operations.
 */
public interface AuditService {

    /**
     * Records an audit entry in a separate transaction boundary to guarantee persistence
     * even if the primary ingestion transaction is rolled back.
     *
     * @param eventId   the event identifier
     * @param operation the operation performed
     * @param status    the resulting status
     * @param message   contextual information or failure details
     * @return the persisted AuditRecord
     */
    AuditRecord recordAudit(String eventId, AuditOperation operation, AuditStatus status, String message);

    /**
     * Retrieves all audit records for a given eventId, ordered newest first.
     *
     * @param eventId the event identifier
     * @return list of audit records
     */
    List<AuditRecord> getAuditTrail(String eventId);
}
