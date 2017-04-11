package akka.initializer;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * CustomEnvironmentAware is responsible for traversing through all
 * properties at startup. Sensitive properties such as secrets and passwords
 * should be filtered out by our custom sensitive property filter.
 */
@Component
public class CustomEnvironmentAware implements EnvironmentAware {

    @Autowired
    private Environment env;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEnvironmentAware.class);
    private Map<String, Object> map = new HashMap<>();
    private boolean blnFoundPropertySources = false;

    @Override
    public void setEnvironment(Environment environment) {
        LOGGER.trace("Started setEnvironment");
        env = environment;
        for (PropertySource<?> propertySource1 : ((AbstractEnvironment) env).getPropertySources()) {
            PropertySource propertySource = (PropertySource) propertySource1;
            if (propertySource instanceof MapPropertySource) {
                boolean blnValidSource = propertySource.getName().contains("applicationConfig");
                if (blnValidSource) {
                    LOGGER.info("Found Property Source {}", propertySource.getName());
                    blnFoundPropertySources = true;
                    map.putAll(((MapPropertySource) propertySource).getSource());
                }
            }
        }
        LOGGER.trace("Finished setEnvironment");
        if (blnFoundPropertySources) {
            readPropertySources();
        }
    }

    private void readPropertySources() {
        LOGGER.info("*** listing properties ***");
        SortedSet<String> keys = new TreeSet<String>((Maps.filterKeys(map, sensitivePropertyFilter()).keySet()));
        for (String key : keys) {
            Object value = map.get(key);
            LOGGER.info("{} = {}", key, value);
        }
    }

    static Predicate<String> sensitivePropertyFilter() {
        Set<String> sensitiveProperties = new HashSet<String>();
        sensitiveProperties.add("password");
        sensitiveProperties.add("secret");
        Predicate<String> filter = s -> {
            boolean blnReturnValue = true;
            for (String sensitiveValue : sensitiveProperties) {
                if (s.toLowerCase().contains(sensitiveValue.toLowerCase())) {
                    blnReturnValue = false;
                }
            }
            return blnReturnValue;
        };
        return filter;
    }
}
