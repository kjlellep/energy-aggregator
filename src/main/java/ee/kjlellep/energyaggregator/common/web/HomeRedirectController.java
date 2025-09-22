package ee.kjlellep.energyaggregator.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeRedirectController {
    @GetMapping("/")
    public String home() {
        return "forward:/actuator";
    }
}
