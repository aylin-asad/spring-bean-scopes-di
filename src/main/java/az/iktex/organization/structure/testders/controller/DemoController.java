package az.iktex.organization.structure.testders.controller;

import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.component.UserAuditLog;
import az.iktex.organization.structure.testders.component.UserStatistics;
import az.iktex.organization.structure.testders.repository.UserRepository;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class DemoController {
    private final UserRepository repository;
    private final ObjectProvider<UserAuditLog> audits;
    private final RequestContext requestContext;
    private final UserStatistics statistics;
    public DemoController(UserRepository repository, ObjectProvider<UserAuditLog> audits,
                          RequestContext requestContext, UserStatistics statistics) {
        this.repository = repository; this.audits = audits;
        this.requestContext = requestContext; this.statistics = statistics;
    }
    @GetMapping("/api/v1/scope-demo") public Map<String, Object> demo() {
        return Map.of("singletonHashCode", System.identityHashCode(repository),
                "prototypeAuditId1", audits.getObject().getAuditId().toString(),
                "prototypeAuditId2", audits.getObject().getAuditId().toString(),
                "requestId", requestContext.getRequestId());
    }
    @GetMapping("/api/v1/stats") public Map<String, Long> stats() {
        return Map.of("createdUsers", statistics.getCreated(), "getUserByIdCalls", statistics.getViewed());
    }
}
