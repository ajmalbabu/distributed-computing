package akka.initializer.support;


import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Enumeration;
import java.util.Properties;

/**
 * A convenient class to start up spring application context and stellar system and provides methods
 * to shut down system gracefully.
 * <p/>
 * This class help to run test cases in parallel by having many stellar system including clustered one
 * running at the same time , each of these system will have its own spring context,configuration &
 * properties values.
 */
public class SpringContextSupport {

    private Properties providedProperties = new Properties();
    private AnnotationConfigApplicationContext applicationContext;

    public SpringContextSupport() {

    }

    public SpringContextSupport(Properties providedProperties) {
        this.providedProperties = providedProperties;
    }

    public AnnotationConfigApplicationContext getApplicationContext() {

        if (applicationContext == null) {
            throw new IllegalStateException("Call build() method first.");
        }

        return applicationContext;
    }


    public void close() {


        applicationContext.close();

    }


    public <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public SpringContextSupport add(String propertyKey, String propertyValue) {
        providedProperties.setProperty(propertyKey, propertyValue);
        return this;
    }


    public SpringContextSupport build(String[] packages) {

        applicationContext = new AnnotationConfigApplicationContext();

        applicationContext.scan(packages);

        applicationContext.addBeanFactoryPostProcessor(propertyPlaceholderConfigurerWith(providedProperties));
        applicationContext.refresh();


        return this;
    }

    private PropertyPlaceholderConfigurer propertyPlaceholderConfigurerWith(Properties userProvidedProperties) {

        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(mergeWithDefault(userProvidedProperties));
        return configurer;

    }

    private Properties mergeWithDefault(Properties userProvidedProperties) {

        Properties defaultProperties = new Properties();

        Enumeration keys = userProvidedProperties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            defaultProperties.put(key, userProvidedProperties.getProperty(key));
        }


        return defaultProperties;

    }

    public static SpringContextSupport instance(Properties properties) {
        return new SpringContextSupport(properties);
    }


    /**
     * This is not a singleton, a convenient method to create a fresh instance.
     *
     * @return
     */
    public static SpringContextSupport instance() {
        return new SpringContextSupport();
    }
}
