package com.aitask.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LedgerVerifyResponse {
    private boolean valid;
    private String message;
}
