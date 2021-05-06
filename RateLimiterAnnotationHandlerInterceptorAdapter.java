package marketplace.front.ratelimiter;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.grid.GridBucketState;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import marketplace.front.configuration.cache.HazelcastFrontConfiguration;
import marketplace.front.util.AuthenticationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@AllArgsConstructor
public class RateLimiterAnnotationHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private AuthenticationUtil authenticationUtil;

    private HazelcastInstance hazelcastInstance;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Optional<List<RateLimiter>> rateLimiters = RateLimiterUtils.getRateLimiters(handlerMethod);

            if (rateLimiters.isPresent()) {
                RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
                IMap<RateLimiterKey, GridBucketState> map = hazelcastInstance.getMap(HazelcastFrontConfiguration.RATE_LIMITER_BUCKET);
                RateLimiterKey key = new RateLimiterKey(authenticationUtil.getPersonId(), requestMapping.value());
                List<Bandwidth> bandwidths = RateLimiterUtils.rateLimiterAnnotationsToBandwidths(rateLimiters.get());
                Boolean executedResult = (Boolean) map.executeOnKey(key, new AcquireTokensEntryProcessor(bandwidths));
                if (!executedResult) {
                    response.setStatus(429);
                    return false;
                }
            }
        }
        return true;
    }
}
