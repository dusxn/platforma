package org.raflab.platforma_server.controllers.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetSolutionPointsRequest(
        @NotNull @DecimalMin("0.0") Double points
) {
}
