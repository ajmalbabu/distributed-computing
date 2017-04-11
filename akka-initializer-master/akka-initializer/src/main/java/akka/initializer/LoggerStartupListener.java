package akka.initializer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

import java.io.File;

/**
 * LoggerStartupListener is responsible for providing a default log directory and log name
 * if one was not supplied during startup.
 */
public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private static final String DEFAULT_LOG_DIRECTORY = System.getProperty("user.home") + File.separator + "logs";
    private static final String DEFAULT_LOG_NAME = "akka-util-log";
    private boolean started = false;

    @Override
    public void start() {
        if (started) {
            return;
        }
        String logDirectory = getValue(System.getProperty("log.dir"), DEFAULT_LOG_DIRECTORY);
        String logName = getValue(System.getProperty("log.name"), DEFAULT_LOG_NAME);

        context.putProperty("LOG_DIRECTORY", logDirectory);
        context.putProperty("LOG_NAME", logName);
        started = true;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {

    }

    @Override
    public void onReset(LoggerContext loggerContext) {

    }

    @Override
    public void onStop(LoggerContext loggerContext) {

    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }

    private String getValue(String value, String defaultValue) {
        String strReturnValue = "";
        if (value != null && !value.isEmpty()) {
            strReturnValue = value;
        } else if (defaultValue != null && !defaultValue.isEmpty()) {
            strReturnValue = defaultValue;
        }
        return strReturnValue;
    }
}
