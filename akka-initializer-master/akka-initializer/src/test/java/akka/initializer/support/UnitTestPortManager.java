package akka.initializer.support;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helps to create unique akka port for each unit test cases and helps avoid conflicts  while
 * testing multiple unit test cases that starts akka (clusters or non-clusters) concurrently
 * in the same JVM.
 */
public class UnitTestPortManager {

    private AtomicInteger port = new AtomicInteger(ThreadLocalRandom.current().nextInt(2000, 65000));

    private static UnitTestPortManager portSingleton = new UnitTestPortManager();

    public static UnitTestPortManager instance() {
        return portSingleton;
    }

    public int getNextPort() {
        return port.incrementAndGet();
    }

    public String getNextPortAsString() {
        return getNextPort() + "";
    }

}
