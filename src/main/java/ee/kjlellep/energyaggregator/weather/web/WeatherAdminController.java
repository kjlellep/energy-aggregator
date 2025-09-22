package ee.kjlellep.energyaggregator.weather.web;

import ee.kjlellep.energyaggregator.weather.app.WeatherDailyRangeFiller;
import ee.kjlellep.energyaggregator.weather.web.dto.WeatherFillResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/weather", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin - Weather", description = "Maintenance endpoint for weather backfill")
public class WeatherAdminController {
    private final WeatherDailyRangeFiller filler;

    public WeatherAdminController(WeatherDailyRangeFiller filler) {
        this.filler = filler;
    }

    @PostMapping("/fill-once")
    @Operation(
        summary = "Trigger one weather fill pass",
        description = "Runs a single backfill iteration and returns the number of rows processed."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Run completed",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = WeatherFillResponse.class),
            examples = @ExampleObject(
                value = """
                    {"processed": 123}
                """
            )
        )
    )
    public WeatherFillResponse fillOnce() {
        int n = this.filler.fillOnce();
        return new WeatherFillResponse(n);
    }
}
