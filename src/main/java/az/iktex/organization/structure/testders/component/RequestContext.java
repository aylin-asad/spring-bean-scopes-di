package az.iktex.organization.structure.testders.component;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
@Component
@RequestScope // TARGET_CLASS proxy, injected safely into singleton beans
public class RequestContext {
    private final String requestId = UUID.randomUUID().toString().substring(0, 8);
    private final long startTime = System.currentTimeMillis();
    public String getRequestId() { return requestId; }
    public long getStartTime() { return startTime; }
}
