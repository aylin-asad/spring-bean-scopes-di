package az.iktex.organization.structure.testders.service;
import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.component.UserAuditLog;
import az.iktex.organization.structure.testders.component.UserStatistics;
import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import az.iktex.organization.structure.testders.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class UserInfoService {
    private final UserRepository repository;
    private final List<UserRepository> repositories;
    private final ObjectProvider<UserAuditLog> auditProvider;
    private final RequestContext requestContext;
    private final RecentlyViewedUsers recentlyViewed;
    private final UserStatistics statistics;
    private NotificationService notificationService; // optional setter injection: question 30 exception
    public UserInfoService(
            UserRepository repository,
            List<UserRepository> repositories,
            ObjectProvider<UserAuditLog> auditProvider,
            RequestContext requestContext,
            RecentlyViewedUsers recentlyViewed,
            UserStatistics statistics) {

        this.repository = Objects.requireNonNull(repository);
        this.repositories = List.copyOf(repositories);
        this.auditProvider = auditProvider;
        this.requestContext = requestContext;
        this.recentlyViewed = recentlyViewed;
        this.statistics = statistics;

        System.out.println("UserInfoService yaradildi: " + hashCode());
        System.out.println("Inject olunan UserRepository sayi: " + repositories.size());
    }
    // Deliberate optional setter injection (task 30). No field injection.
    @Autowired(required = false)
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    public UserInfoResponse createUser(String name, String email) {
        UserInfoResponse user = repository.createUser(name, email);
        UserAuditLog audit = auditProvider.getObject();
        audit.setAction("CREATE_USER");
        System.out.println("AUDIT id=" + audit.getAuditId() + " action=" + audit.getAction());
        statistics.created();
        if (notificationService != null) notificationService.sendWelcome(email);
        return user;
    }
    public UserInfoResponse getUserById(Long id) {
        System.out.println("[Service] requestId=" + requestContext.getRequestId());
        UserInfoResponse user = repository.getUserById(id);
        recentlyViewed.add(id);
        statistics.viewed();
        return user;
    }
    public List<UserInfoResponse> getAllUsers() { return repository.getAllUsers(); }
    public List<UserInfoResponse> getRecentlyViewedUsers() {
        return recentlyViewed.getIds().stream()
                .map(id -> repository.getAllUsers().stream().filter(user -> user.id().equals(id)).findFirst())
                .flatMap(java.util.Optional::stream).toList();
    }
    public int repositoryCount() { return repositories.size(); }
}
