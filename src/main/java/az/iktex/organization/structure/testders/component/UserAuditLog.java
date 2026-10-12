package az.iktex.organization.structure.testders.component;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserAuditLog {
    private final UUID auditId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.now();
    private String action;
    public UserAuditLog() { System.out.println("UserAuditLog yaradildi: " + auditId); }
    public UUID getAuditId() { return auditId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    @PreDestroy public void destroy() { System.out.println("UserAuditLog @PreDestroy: " + auditId); }
    // Spring prototype beans are not automatically destroyed by the container.
    @PreDestroy
    public void onDestroy() {
        System.out.println("UserAuditLog @PreDestroy: " + auditId);
    }
}
