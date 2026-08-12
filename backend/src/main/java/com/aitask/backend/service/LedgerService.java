package com.aitask.backend.service;

import com.aitask.backend.dto.LedgerHistoryResponse;
import com.aitask.backend.dto.LedgerVerifyResponse;
import com.aitask.backend.model.Action;
import com.aitask.backend.model.Task;
import com.aitask.backend.model.TaskLedger;
import com.aitask.backend.repository.TaskLedgerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final TaskLedgerRepository taskLedgerRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

    public void recordAction(Task task, Action action) {
        String payloadSnapshot = generateCanonicalJson(task);
        
        String prevHash = "0";
        taskLedgerRepository.findTopByTaskIdOrderByTimestampDesc(task.getId())
                .ifPresent(latest -> prevHash.replace("0", latest.getHash()));
        
        // Actually we need to set prevHash correctly
        String finalPrevHash = taskLedgerRepository.findTopByTaskIdOrderByTimestampDesc(task.getId())
                .map(TaskLedger::getHash)
                .orElse("0");

        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        String hash = generateHash(payloadSnapshot, finalPrevHash, timestamp);

        TaskLedger ledger = TaskLedger.builder()
                .task(task)
                .action(action)
                .payloadSnapshot(payloadSnapshot)
                .prevHash(finalPrevHash)
                .hash(hash)
                .timestamp(timestamp)
                .build();

        taskLedgerRepository.save(ledger);
    }

    public List<LedgerHistoryResponse> getHistory(Long taskId) {
        List<TaskLedger> ledgers = taskLedgerRepository.findByTaskIdOrderByTimestampAsc(taskId);
        List<LedgerHistoryResponse> responses = new ArrayList<>();

        for (TaskLedger ledger : ledgers) {
            String computedHash = generateHash(ledger.getPayloadSnapshot(), ledger.getPrevHash(), ledger.getTimestamp());
            boolean isValid = computedHash.equals(ledger.getHash());

            responses.add(LedgerHistoryResponse.builder()
                    .id(ledger.getId())
                    .action(ledger.getAction())
                    .payloadSnapshot(ledger.getPayloadSnapshot())
                    .prevHash(ledger.getPrevHash())
                    .hash(ledger.getHash())
                    .timestamp(ledger.getTimestamp())
                    .valid(isValid)
                    .build());
        }

        return responses;
    }

    public LedgerVerifyResponse verifyLedger(Long taskId) {
        List<TaskLedger> ledgers = taskLedgerRepository.findByTaskIdOrderByTimestampAsc(taskId);
        
        if (ledgers.isEmpty()) {
            return new LedgerVerifyResponse(true, "No history found.");
        }

        String expectedPrevHash = "0";
        for (TaskLedger ledger : ledgers) {
            if (!ledger.getPrevHash().equals(expectedPrevHash)) {
                return new LedgerVerifyResponse(false, "Tampering detected: prevHash mismatch at ledger ID " + ledger.getId());
            }

            String computedHash = generateHash(ledger.getPayloadSnapshot(), ledger.getPrevHash(), ledger.getTimestamp());
            if (!computedHash.equals(ledger.getHash())) {
                return new LedgerVerifyResponse(false, "Tampering detected: hash mismatch at ledger ID " + ledger.getId());
            }

            expectedPrevHash = ledger.getHash();
        }

        return new LedgerVerifyResponse(true, "Ledger verified successfully.");
    }

    private String generateCanonicalJson(Task task) {
        try {
            // Create a Map to ensure custom properties order/filtering if needed, but simple task mapping is enough
            Map<String, Object> map = new TreeMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("description", task.getDescription());
            map.put("priority", task.getPriority());
            map.put("status", task.getStatus());
            map.put("dueDate", task.getDueDate() != null ? task.getDueDate().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() : null);
            map.put("userId", task.getUser().getId());
            map.put("deletedAt", task.getDeletedAt() != null ? task.getDeletedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() : null);

            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating JSON snapshot", e);
        }
    }

    private String generateHash(String payloadSnapshot, String prevHash, LocalDateTime timestamp) {
        try {
            String timestampStr = timestamp.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
            String input = payloadSnapshot + prevHash + timestampStr;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}
