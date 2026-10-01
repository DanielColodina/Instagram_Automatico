package br.com.instagrambot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InstagramContainerStatusResponse(
        String id,
        @JsonProperty("status_code") String statusCode,
        String status
) {
}
