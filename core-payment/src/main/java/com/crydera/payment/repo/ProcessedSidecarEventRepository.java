package com.crydera.payment.repo;

import com.crydera.payment.domain.ProcessedSidecarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedSidecarEventRepository extends JpaRepository<ProcessedSidecarEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
