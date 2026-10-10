package com.tick.market.service;

import com.tick.market.entity.AuditOperation;
import com.tick.market.entity.AuditRecord;
import com.tick.market.entity.AuditStatus;
import com.tick.market.repository.AuditRecordRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuditService utilizing independent transaction boundaries
 * (Propagation.REQUIRES_NEW) to preserve audit trails during downstream failures.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRecordRepository auditRecordRepository;

    public AuditServiceImpl(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditRecord recordAudit(String eventId, AuditOperation operation, AuditStatus status, String message) {
        log.info("Recording audit: eventId={}, operation={}, status={}, message={}", eventId, operation, status, message);
        AuditRecord auditRecord = AuditRecord.of(eventId, operation, status, message);
        return auditRecordRepository.save(auditRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecord> getAuditTrail(String eventId) {
        return auditRecordRepository.findByEventIdOrderByRecordedAtDesc(eventId);
    }
}
