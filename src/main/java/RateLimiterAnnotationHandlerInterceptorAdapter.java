import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import marketplace.front.configuration.cache.HazelcastFrontConfiguration;
import marketplace.front.util.AuthenticationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;


public class RateLimiterAnnotationHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private AuthenticationUtil authenticationUtil;
    private final ProxyManager<RateLimiterKey> proxyManager;

    @Autowired
    public RateLimiterAnnotationHandlerInterceptorAdapter(AuthenticationUtil authenticationUtil, HazelcastInstance hazelcastInstance) {
        this.authenticationUtil = authenticationUtil;
        IMap<RateLimiterKey, GridBucketState> bucketsMap = hazelcastInstance.getMap(HazelcastFrontConfiguration.RATE_LIMITER_BUCKET);
        proxyManager = Bucket4j.extension(Hazelcast.class).proxyManagerForMap(bucketsMap);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Optional<List<RateLimiter>> rateLimiters = RateLimiterUtils.getRateLimiters(handlerMethod);

            if (rateLimiters.isPresent()) {
                RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
                RateLimiterKey key = new RateLimiterKey(authenticationUtil.getPersonId(), requestMapping.value());
                Bucket bucket = proxyManager.getProxy(key, () -> RateLimiterUtils.rateLimiterAnnotationsToBucketConfiguration(rateLimiters.get()));
                if (!bucket.tryConsume(1)) {
                    response.setStatus(429);
                    return false;
                }
            }
        }
        return true;
    }
}
