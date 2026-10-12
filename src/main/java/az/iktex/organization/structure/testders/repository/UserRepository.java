package az.iktex.organization.structure.testders.repository;

import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import java.util.List;
public interface UserRepository {
    UserInfoResponse createUser(String name, String email);
    UserInfoResponse getUserById(Long id);
    List<UserInfoResponse> getAllUsers();
}
