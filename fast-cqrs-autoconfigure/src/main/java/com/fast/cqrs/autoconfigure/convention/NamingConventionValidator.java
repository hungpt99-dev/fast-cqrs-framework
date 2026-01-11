package com.fast.cqrs.autoconfigure.convention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates naming conventions at startup.
 * <p>
 * Logs warnings for convention violations.
 */
@Component
public class NamingConventionValidator {

    private static final Logger log = LoggerFactory.getLogger(NamingConventionValidator.class);

    private final List<String> violations = new ArrayList<>();

    /**
     * Validates a controller class.
     */
    public void validateController(Class<?> clazz) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackageName();
        
        if (!Conventions.isValidController(className, packageName)) {
            String msg = String.format(
                "Controller '%s' violates naming conventions: " +
                "should be in '*.controller' package with 'Controller' suffix",
                clazz.getName()
            );
            violations.add(msg);
            log.warn(msg);
        }
    }

    /**
     * Validates a handler class.
     */
    public void validateHandler(Class<?> clazz) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackageName();
        
        if (!Conventions.isValidHandler(className, packageName)) {
            String msg = String.format(
                "Handler '%s' violates naming conventions: " +
                "should be in '*.handler' package with 'Handler' suffix",
                clazz.getName()
            );
            violations.add(msg);
            log.warn(msg);
        }
    }

    /**
     * Validates a repository class.
     */
    public void validateRepository(Class<?> clazz) {
        String className = clazz.getSimpleName();
        String packageName = clazz.getPackageName();
        
        if (!Conventions.isValidRepository(className, packageName)) {
            String msg = String.format(
                "Repository '%s' violates naming conventions: " +
                "should be in '*.repository' package with 'Repository' suffix",
                clazz.getName()
            );
            violations.add(msg);
            log.warn(msg);
        }
    }

    /**
     * Gets all violations.
     */
    public List<String> getViolations() {
        return new ArrayList<>(violations);
    }

    /**
     * Returns true if there are violations.
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    /**
     * Clears all violations.
     */
    public void clear() {
        violations.clear();
    }
}
