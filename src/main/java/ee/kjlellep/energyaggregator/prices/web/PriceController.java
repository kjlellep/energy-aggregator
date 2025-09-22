package ee.kjlellep.energyaggregator.prices.web;

import ee.kjlellep.energyaggregator.prices.app.PriceImportService;
import ee.kjlellep.energyaggregator.prices.repo.ElectricityPriceHourlyRepository;
import ee.kjlellep.energyaggregator.prices.web.dto.PriceUploadResponse;
import ee.kjlellep.energyaggregator.common.web.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/price", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Prices", description = "Price CSV upload & listing")
public class PriceController {
    private final PriceImportService importer;
    private final ElectricityPriceHourlyRepository repo;

    public PriceController(PriceImportService importer, ElectricityPriceHourlyRepository repo) {
        this.importer = importer;
        this.repo = repo;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload electricity prices CSV",
        description = """
            Upload a semicolon-delimited CSV (UTF-8). Required headers (quoted):
            - "Ajatempel (UTC)" (epoch seconds, UTC)
            - "Kuupäev (Eesti aeg)" (dd.MM.yyyy HH:mm)
            - "NPS Eesti" (decimal; comma separators allowed)

            Only the "NPS Eesti" column is used for this project.

            **Sample CSV**:
            ```csv
            "Ajatempel (UTC)";"Kuupäev (Eesti aeg)";"NPS Läti";"NPS Leedu";"NPS Soome";"NPS Eesti"
            "1704060000";"01.01.2024 00:00";"40,01";"40,01";"40,01";"40,01"
            "1704063600";"01.01.2024 01:00";"38,37";"38,37";"38,37";"38,37"
            "1704067200";"01.01.2024 02:00";"28,46";"28,46";"28,46";"28,46"
            ```
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Upload result",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PriceUploadResponse.class),
            examples = @ExampleObject(
                name = "Success",
                value = """
                    {"inserted":8500,"updated":200,"unchanged":60,"skipped":0,"totalSeen":8760}
                """
            )
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = {
                @ExampleObject(
                    name = "Empty file",
                    value = """
                        {"message":"Uploaded file is empty."}
                    """
                ),
                @ExampleObject(
                    name = "Missing headers",
                    value = """
                        {"message":"CSV must contain 'NPS Eesti' and either 'Ajatempel (UTC)' or 'Kuupäev (Eesti aeg)'."}
                    """
                )
            }
        )
    )
    public PriceUploadResponse upload(
        @Parameter(
            description = "CSV file to upload",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "string", format = "binary")
            )
        )
        @RequestPart("file")
        MultipartFile file
    ) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        var res = this.importer.upsertCsv(file.getInputStream());
        return new PriceUploadResponse(
            res.inserted(),
            res.updated(),
            res.unchanged(),
            res.skipped(),
            res.inserted() + res.updated() + res.unchanged() + res.skipped()
        );
    }

    @GetMapping("/count")
    @Operation(summary = "Count data rows in electricity price table")
    @ApiResponse(
        responseCode = "200",
        description = "Row count",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(type = "object", example = "{\"rows\": 8760}")
        )
    )
    public Map<String, Object> count() {
        return Map.of("rows", this.repo.count());
    }
}
