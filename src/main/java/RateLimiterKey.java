import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Data
@AllArgsConstructor
public class RateLimiterKey implements Serializable {

    private String userId;
    private String[] uri;

}
