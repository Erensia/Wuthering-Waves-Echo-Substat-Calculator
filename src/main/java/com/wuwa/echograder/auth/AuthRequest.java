package com.wuwa.echograder.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(
                regexp = "^[\\p{L}\\p{N}_]+$",
                message = "아이디는 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.")
        String username,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
