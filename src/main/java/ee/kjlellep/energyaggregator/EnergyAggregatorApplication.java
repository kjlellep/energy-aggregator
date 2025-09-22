package ee.kjlellep.energyaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EnergyAggregatorApplication {
	public static void main(String[] args) {
		SpringApplication.run(EnergyAggregatorApplication.class, args);
	}
}
