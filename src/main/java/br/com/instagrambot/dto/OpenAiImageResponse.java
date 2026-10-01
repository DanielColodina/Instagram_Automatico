package br.com.instagrambot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiImageResponse(List<ImageData> data) {

    public record ImageData(
            @JsonProperty("b64_json") String b64Json
    ) {
    }
}
