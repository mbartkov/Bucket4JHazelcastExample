package marketplace.front.ratelimiter;

import io.github.bucket4j.*;
import io.github.bucket4j.grid.GridBucketState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class RateLimiterUtils {

    public static final List<Bandwidth> rateLimiterAnnotationsToBandwidths(List<RateLimiter> rateLimiters) {
        return collectBandwidths(rateLimiters);
    }

    public static final GridBucketState buildBucket(List<Bandwidth> bandwidths) {
        long low = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        BucketConfiguration configuration = new BucketConfiguration(bandwidths);
        BucketState bucketState = BucketState.createInitialState(configuration, low);
        return new GridBucketState(configuration, bucketState);
    }

    public static Optional<List<RateLimiter>> getRateLimiters(HandlerMethod handlerMethod) {
        RateLimiters rateLimitersAnnotation = handlerMethod.getMethodAnnotation(RateLimiters.class);
        if(rateLimitersAnnotation != null) {
            return Optional.of(Arrays.asList(rateLimitersAnnotation.value()));
        }
        RateLimiter rateLimiterAnnotation = handlerMethod.getMethodAnnotation(RateLimiter.class);
        if(rateLimiterAnnotation != null) {
            return Optional.of(Arrays.asList(rateLimiterAnnotation));
        }
        return Optional.empty();
    }

    private static final List<Bandwidth> collectBandwidths(List<RateLimiter> rateLimiters) {
        return rateLimiters.stream().map(RateLimiterUtils::buildBandwidth).collect(Collectors.toList());
    }

    private static final Bandwidth buildBandwidth(RateLimiter rateLimiter) {
        TimeUnit timeUnit = rateLimiter.timeUnit();
        long timeValue = rateLimiter.timeValue();
        long restriction = rateLimiter.restriction();
        if (TimeUnit.MINUTES.equals(timeUnit)) {
            return Bandwidth.simple(restriction, Duration.ofMinutes(timeValue));
        } else if (TimeUnit.HOURS.equals(timeUnit)) {
            return Bandwidth.simple(restriction, Duration.ofHours(timeValue));
        } else {
            return Bandwidth.simple(5000, Duration.ofHours(1));
        }
    }
}
