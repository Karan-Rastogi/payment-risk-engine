package com.karan.risk.paymentriskengine.service.impl;

import com.karan.risk.paymentriskengine.domain.RuleHit;
import com.karan.risk.paymentriskengine.repository.RuleHitRepository;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RuleHitServiceImplTest {

    private RuleHitRepository repository;
    private RuleHitServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(RuleHitRepository.class);
        service = new RuleHitServiceImpl(repository);
    }

    @Test
    @DisplayName("recordHits persists all rule results")
    void recordHits_persistsAll() {
        UUID paymentId = UUID.randomUUID();
        List<RuleResult> results = List.of(
            RuleResult.hit("VELOCITY", 40, "too fast"),
            RuleResult.pass("AMOUNT_THRESHOLD"));

        service.recordHits(paymentId, results);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RuleHit>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).saveAll(captor.capture());

        List<RuleHit> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getRuleName()).isEqualTo("VELOCITY");
        assertThat(saved.get(0).getScore()).isEqualTo(40);
        assertThat(saved.get(0).getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("recordHits with empty list does nothing")
    void recordHits_emptyList_noOp() {
        service.recordHits(UUID.randomUUID(), List.of());

        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("recordHits with null does nothing")
    void recordHits_null_noOp() {
        service.recordHits(UUID.randomUUID(), null);

        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Long reason is truncated to 512 chars")
    void recordHits_truncatesLongReason() {
        String longReason = "x".repeat(1000);
        List<RuleResult> results = List.of(RuleResult.hit("R", 10, longReason));

        service.recordHits(UUID.randomUUID(), results);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RuleHit>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());

        assertThat(captor.getValue().get(0).getReason()).hasSize(512);
    }
}
