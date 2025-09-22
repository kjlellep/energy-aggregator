package ee.kjlellep.energyaggregator.query.web;

import ee.kjlellep.energyaggregator.common.web.dto.ErrorResponse;
import ee.kjlellep.energyaggregator.query.web.dto.DailyMetricsDto;
import ee.kjlellep.energyaggregator.query.service.AggregatorQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Aggregate", description = "Aggregated metrics over date ranges")
public class AggregatorQueryController {
    private final AggregatorQueryService service;

    public AggregatorQueryController(AggregatorQueryService service) {
        this.service = service;
    }

    @GetMapping("/daily")
    @Operation(
        summary = "Get daily aggregated metrics",
        description = """
            Returns per-day averages for temperature and electricity price within the inclusive date range.
            Only days where both datasets exist are returned.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Daily metrics",
        content = @Content(mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = DailyMetricsDto.class)))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Invalid range",
                value = """
                  {"message":"endDate must be on or after startDate."}
                """
            )
        )
    )
    public ResponseEntity<List<DailyMetricsDto>> getDailyAggregates(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date (inclusive)", required = true, example = "2024-01-01",
                       schema = @Schema(type = "string", format = "date"))
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date (inclusive)", required = true, example = "2024-01-31",
                       schema = @Schema(type = "string", format = "date"))
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }
        return ResponseEntity.ok(this.service.query(startDate, endDate));
    }
}
