package az.iktex.organization.structure.testders.repository;

import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "mock")
public class MockUserRepository implements UserRepository {
    private final RequestContext requestContext;
    public MockUserRepository(RequestContext requestContext) {
        this.requestContext = requestContext;
        System.out.println("MockUserRepository yaradildi: " + hashCode());
    }
    @Override public UserInfoResponse createUser(String name, String email) {
        return new UserInfoResponse(1L, "Mock User", "mock1@example.com");
    }
    @Override public UserInfoResponse getUserById(Long id) {
        System.out.println("[Repository] requestId=" + requestContext.getRequestId());
        return getAllUsers().stream().filter(user -> user.id().equals(id)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Mock user tapilmadi: " + id));
    }
    @Override public List<UserInfoResponse> getAllUsers() {
        return List.of(new UserInfoResponse(1L,"Mock User","mock1@example.com"),
                       new UserInfoResponse(2L,"Mock User","mock2@example.com"));
    }
}
