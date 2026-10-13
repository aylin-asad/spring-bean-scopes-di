package az.iktex.organization.structure.testders.repository;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "memory", matchIfMissing = true)

public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, UserInfoResponse> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final RequestContext requestContext;
    public InMemoryUserRepository(RequestContext requestContext) {
        this.requestContext = requestContext;
        System.out.println("InMemoryUserRepository yaradildi: " + hashCode());
    }
    @PostConstruct public void init() {
        createUser("Ali Aliyev", "ali@example.com");
        createUser("Leyla Mammadova", "leyla@example.com");
        createUser("Nigar Hasanli", "nigar@example.com");
        System.out.println("UserRepository hazirdir, 3 user yuklendi");
    }
    @PreDestroy public void close() {
        storage.clear();
        System.out.println("InMemoryUserRepository baglandi, storage temizlendi");
    }
    @Override public UserInfoResponse createUser(String name, String email) {
        long id = sequence.incrementAndGet();
        var user = new UserInfoResponse(id, name, email);
        storage.put(id, user);
        return user;
    }
    @Override public UserInfoResponse getUserById(Long id) {
        System.out.println("[Repository] requestId=" + requestContext.getRequestId());
        var user = storage.get(id);
        if (user == null) throw new NoSuchElementException("User tapilmadi: " + id);
        return user;
    }
    @Override public List<UserInfoResponse> getAllUsers() {
        return storage.values().stream().sorted(Comparator.comparing(UserInfoResponse::id)).toList();
    }
}
