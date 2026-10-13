package az.iktex.organization.structure.testders.service;
import az.iktex.organization.structure.testders.component.*;
import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import az.iktex.organization.structure.testders.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UserInfoServiceTest {
    @Test void getsAllUsersWithoutSpring() {
        UserRepository repository = mock(UserRepository.class);
        @SuppressWarnings("unchecked") ObjectProvider<UserAuditLog> audits = mock(ObjectProvider.class);
        RequestContext request = mock(RequestContext.class);
        RecentlyViewedUsers recent = mock(RecentlyViewedUsers.class);
        UserStatistics stats = mock(UserStatistics.class);
        var expected = List.of(new UserInfoResponse(1L, "Test User", "test@example.com"));
        when(repository.getAllUsers()).thenReturn(expected);
        var service = new UserInfoService(repository, List.of(repository), audits, request, recent, stats);
        assertEquals(expected, service.getAllUsers());
        verify(repository).getAllUsers();
    }
}
