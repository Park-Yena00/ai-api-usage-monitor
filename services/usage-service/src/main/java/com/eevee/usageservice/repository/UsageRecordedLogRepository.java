package com.eevee.usageservice.repository;

import com.eevee.usageservice.domain.UsageRecordedLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsageRecordedLogRepository extends JpaRepository<UsageRecordedLogEntity, UUID> {

    boolean existsByEventId(UUID eventId);
}
