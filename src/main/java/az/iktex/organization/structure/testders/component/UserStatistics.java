package az.iktex.organization.structure.testders.component;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;
@Component
@ApplicationScope
public class UserStatistics {
    private final AtomicLong created = new AtomicLong();
    private final AtomicLong viewed = new AtomicLong();
    public void created() { created.incrementAndGet(); }
    public void viewed() { viewed.incrementAndGet(); }
    public long getCreated() { return created.get(); }
    public long getViewed() { return viewed.get(); }
}
