package com.okta.developer.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * There is a bug in the eureka health check indicator that this class works around. Currently, if there is a noticeable
 * delay in starting the application, the eureka health check returns OUT_OF_SERVICE initially. This is a problem as
 * when a service is in this state it will ignore all 'up' reports. The fix here is to initially report us as down,
 * which will override the out of service state, and allow all subsequent transitions to up to occur successfully.
 */
@Component
public class EurekaFix implements HealthIndicator {
    private static Logger LOG = LoggerFactory.getLogger(EurekaFix.class);

    private boolean applicationIsUp = false;

    /**
     * When we receive notification that the application has started, report that we are now in an up state
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        this.applicationIsUp = true;
        LOG.warn("Application has started, reporting to eureka that application is now available");
    }

    /**
     * Force ourselves into a down state while the application is starting, and transition us to an up state once we
     * have started. Down should override out of service.
     *
     * @return down if we are not yet fully started, otherwise put us in an up state
     */
    @Override
    public Health health() {
        if (!applicationIsUp) {
            LOG.warn("Reporting application as down to eureka as application has not yet started");
            return Health.down().build();
        }
        return Health.up().build();
    }
}
