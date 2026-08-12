package com.aitask.backend;

import com.aitask.backend.dto.LedgerVerifyResponse;
import com.aitask.backend.model.Action;
import com.aitask.backend.model.TaskLedger;
import com.aitask.backend.repository.TaskLedgerRepository;
import com.aitask.backend.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerServiceTest {

    @Mock
    private TaskLedgerRepository taskLedgerRepository;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(taskLedgerRepository);
    }

    @Test
    void testVerifyLedger_Empty() {
        when(taskLedgerRepository.findByTaskIdOrderByTimestampAsc(1L)).thenReturn(Collections.emptyList());

        LedgerVerifyResponse response = ledgerService.verifyLedger(1L);
        assertTrue(response.isValid());
        assertEquals("No history found.", response.getMessage());
    }

    @Test
    void testVerifyLedger_TamperedPrevHash() {
        TaskLedger genesis = new TaskLedger(1L, null, Action.CREATED, "{}", "0", "hash1", LocalDateTime.now());
        TaskLedger tampered = new TaskLedger(2L, null, Action.UPDATED, "{}", "wrongHash", "hash2", LocalDateTime.now());

        when(taskLedgerRepository.findByTaskIdOrderByTimestampAsc(1L)).thenReturn(Arrays.asList(genesis, tampered));

        LedgerVerifyResponse response = ledgerService.verifyLedger(1L);
        assertFalse(response.isValid());
    }
}
