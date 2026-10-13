package az.iktex.organization.structure.testders.controller;

import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.dto.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.UserInfoResponse;
import az.iktex.organization.structure.testders.service.UserInfoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/user-info")
public class UserInfoController {
    private final UserInfoService service;
    private final RequestContext requestContext;
    public UserInfoController(UserInfoService service, RequestContext requestContext) {
        this.service = service;
        this.requestContext = requestContext;
        System.out.println("UserInfoController yaradildi: " + hashCode());
    }
    @PostMapping("/create") public UserInfoResponse create(@Valid @RequestBody CreateUserRequest request) {
        return service.createUser(request.name(),request.email());
    }
    @GetMapping("/{id}") public UserInfoResponse get(@PathVariable Long id) {
        System.out.println("[Controller] requestId=" + requestContext.getRequestId());
        System.out.println("RequestContext class=" + requestContext.getClass().getName());
        UserInfoResponse result = service.getUserById(id);
        System.out.println("Sorğu muddeti: " + (System.currentTimeMillis() - requestContext.getStartTime()) + " ms");
        return result;
    }
    @GetMapping("/all") public List<UserInfoResponse> all() { return service.getAllUsers(); }
    @GetMapping("/recent") public List<UserInfoResponse> recent() { return service.getRecentlyViewedUsers(); }
}
