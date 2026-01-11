package com.fast.cqrs.autoconfigure.convention;

/**
 * Defines naming conventions for the framework.
 */
public final class Conventions {

    private Conventions() {}

    // Package conventions
    public static final String CONTROLLER_PACKAGE = "controller";
    public static final String HANDLER_PACKAGE = "handler";
    public static final String REPOSITORY_PACKAGE = "repository";
    public static final String ENTITY_PACKAGE = "entity";
    public static final String EVENT_PACKAGE = "event";
    public static final String AGGREGATE_PACKAGE = "aggregate";
    public static final String DTO_PACKAGE = "dto";

    // Suffix conventions
    public static final String CONTROLLER_SUFFIX = "Controller";
    public static final String HANDLER_SUFFIX = "Handler";
    public static final String REPOSITORY_SUFFIX = "Repository";
    public static final String EVENT_SUFFIX = "Event";
    public static final String AGGREGATE_SUFFIX = "Aggregate";
    public static final String COMMAND_SUFFIX = "Cmd";
    public static final String QUERY_SUFFIX = "Query";

    /**
     * Validates controller naming convention.
     */
    public static boolean isValidController(String className, String packageName) {
        return className.endsWith(CONTROLLER_SUFFIX) && 
               packageName.contains("." + CONTROLLER_PACKAGE);
    }

    /**
     * Validates handler naming convention.
     */
    public static boolean isValidHandler(String className, String packageName) {
        return className.endsWith(HANDLER_SUFFIX) && 
               packageName.contains("." + HANDLER_PACKAGE);
    }

    /**
     * Validates repository naming convention.
     */
    public static boolean isValidRepository(String className, String packageName) {
        return className.endsWith(REPOSITORY_SUFFIX) && 
               packageName.contains("." + REPOSITORY_PACKAGE);
    }

    /**
     * Validates event naming convention.
     */
    public static boolean isValidEvent(String className, String packageName) {
        return className.endsWith(EVENT_SUFFIX) && 
               packageName.contains("." + EVENT_PACKAGE);
    }

    /**
     * Validates aggregate naming convention.
     */
    public static boolean isValidAggregate(String className, String packageName) {
        return className.endsWith(AGGREGATE_SUFFIX) && 
               packageName.contains("." + AGGREGATE_PACKAGE);
    }

    /**
     * Gets expected package for a component type.
     */
    public static String getExpectedPackage(String basePackage, String componentType) {
        return basePackage + "." + componentType;
    }
}
