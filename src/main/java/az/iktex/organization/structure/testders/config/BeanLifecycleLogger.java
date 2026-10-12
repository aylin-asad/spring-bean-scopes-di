package az.iktex.organization.structure.testders.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
@Component
public class BeanLifecycleLogger implements BeanPostProcessor {
    private boolean relevant(String beanName, Object bean) {
        return bean.getClass().getName().startsWith("az.iktex.");
    }
    @Override public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        if (relevant(name, bean)) System.out.println("[BeforeInit] " + name);
        return bean;
    }
    @Override public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        if (relevant(name, bean)) System.out.println("[AfterInit] " + name);
        return bean;
    }
}
