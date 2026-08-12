package com.aitask.backend.dto;

import com.aitask.backend.model.Action;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LedgerHistoryResponse {
    private Long id;
    private Action action;
    private String payloadSnapshot;
    private String prevHash;
    private String hash;
    private LocalDateTime timestamp;
    private boolean valid;
}
