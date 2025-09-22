package ee.kjlellep.energyaggregator.prices.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PriceUploadResponse", description = "Result summary of the price CSV import.")
@JsonPropertyOrder({"inserted","updated","unchanged","skipped","totalSeen"})
public record PriceUploadResponse(
    @Schema(description = "Number of rows inserted", example = "8500")
    int inserted,

    @Schema(description = "Number of rows updated", example = "200")
    int updated,

    @Schema(description = "Rows already up-to-date", example = "60")
    int unchanged,

    @Schema(description = "Rows skipped due to validation or duplicates", example = "0")
    int skipped,

    @Schema(description = "Total rows seen in the file", example = "8760")
    int totalSeen
) {}

