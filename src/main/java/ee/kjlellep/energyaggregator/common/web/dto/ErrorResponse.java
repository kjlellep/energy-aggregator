package ee.kjlellep.energyaggregator.common.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Standard error wrapper")
public record ErrorResponse(
    @Schema(description = "Human-readable error message")
    String message
) {}
