package ee.kjlellep.energyaggregator.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Energy Aggregator API",
        version = "v1",
        description = "Backend for aggregating electricity prices and weather data."
    ),
    servers = { @Server(url = "http://localhost:8080") }
)
public class OpenApiConfig {}
