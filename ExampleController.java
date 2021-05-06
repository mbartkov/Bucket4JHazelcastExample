package marketplace.front.ratelimiter;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    @RateLimiters({@RateLimiter(timeUnit = TimeUnit.MINUTES, timeValue = 1, restriction = 2), @RateLimiter(timeUnit = TimeUnit.HOURS, timeValue = 1, restriction = 5)})
    @RequestMapping("/example/{id}")
    public ExampleResponse example(@PathVariable("id") String id) {
        return new ExampleResponse(String.format("Hello user %s from ip %s", id, id));
    }

    public static class ExampleResponse {
        private String message;

        public ExampleResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
