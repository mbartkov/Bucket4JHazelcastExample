package marketplace.front.ratelimiter;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.grid.GridBucketState;

import java.util.List;
import java.util.Map;

public class AcquireTokensEntryProcessor implements EntryProcessor<RateLimiterKey, GridBucketState> {

    private List<Bandwidth> bandwidths;

    private long now;

    private static final long TOKEN_COST = 1;

    public AcquireTokensEntryProcessor(List<Bandwidth> bandwidths) {
        this.bandwidths = bandwidths;
        now = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }

    @Override
    public Object process(Map.Entry<RateLimiterKey, GridBucketState> entry) {
        GridBucketState localBucket = (entry != null && entry.getValue() != null) ? entry.getValue() : RateLimiterUtils.buildBucket(bandwidths);
        localBucket.refillAllBandwidth(now);
        long availableTokens = localBucket.getAvailableTokens();
        if(availableTokens > TOKEN_COST) {
            localBucket.consume(TOKEN_COST);
            entry.setValue(localBucket);
            return true;
        }
        return false;
    }

    @Override
    public EntryBackupProcessor<RateLimiterKey, GridBucketState> getBackupProcessor() {
        return null;
    }
}
