import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RateLimiter {

    TimeUnit timeUnit() default TimeUnit.MINUTES;

    long timeValue();

    long restriction();

}
