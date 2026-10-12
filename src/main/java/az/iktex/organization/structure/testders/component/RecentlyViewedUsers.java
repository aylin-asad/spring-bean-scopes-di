package az.iktex.organization.structure.testders.component;

import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
@Component
@SessionScope
public class RecentlyViewedUsers implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Deque<Long> ids = new ArrayDeque<>();
    public synchronized void add(Long id) {
        ids.remove(id);
        ids.addFirst(id);
        while (ids.size() > 5) ids.removeLast();
    }
    public synchronized List<Long> getIds() { return new ArrayList<>(ids); }
    @PreDestroy public void destroy() { System.out.println("RecentlyViewedUsers @PreDestroy (session ended)"); }
}
