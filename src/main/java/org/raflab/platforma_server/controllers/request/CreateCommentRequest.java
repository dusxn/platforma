package org.raflab.platforma_server.controllers.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank @Size(max = 255) String filePath,
        @NotNull @Min(1) Integer lineNumber,
        @NotBlank @Size(max = 1000) String text
) {
}
