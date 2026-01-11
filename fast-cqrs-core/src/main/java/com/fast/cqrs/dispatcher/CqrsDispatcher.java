package com.fast.cqrs.dispatcher;

import com.fast.cqrs.annotation.Command;
import com.fast.cqrs.annotation.Query;
import com.fast.cqrs.bus.CommandBus;
import com.fast.cqrs.bus.QueryBus;
import com.fast.cqrs.context.HttpInvocationContext;
import com.fast.cqrs.handler.CommandHandler;
import com.fast.cqrs.handler.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationContextAware;

/**
 * Central CQRS dispatcher that routes requests to the appropriate bus.
 * <p>
 * Supports routing via:
 * <ul>
 *   <li>@Query/@Command with explicit handler class</li>
 *   <li>@Query/@Command with query/command class</li>
 *   <li>Auto-detection from method parameters</li>
 * </ul>
 */
public class CqrsDispatcher implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(CqrsDispatcher.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private ApplicationContext applicationContext;

    public CqrsDispatcher(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Object dispatch(HttpInvocationContext context) {
        Method method = context.method();
        
        boolean isQuery = method.isAnnotationPresent(Query.class);
        boolean isCommand = method.isAnnotationPresent(Command.class);

        if (!isQuery && !isCommand) {
            throw new CqrsDispatchException(
                "Method '" + method.getName() + "' must be annotated with @Query or @Command"
            );
        }

        if (isQuery && isCommand) {
            throw new CqrsDispatchException(
                "Method '" + method.getName() + "' cannot have both @Query and @Command"
            );
        }

        if (isQuery) {
            return dispatchQuery(context, method.getAnnotation(Query.class));
        } else {
            return dispatchCommand(context, method.getAnnotation(Command.class));
        }
    }

    @SuppressWarnings("unchecked")
    private Object dispatchQuery(HttpInvocationContext context, Query annotation) {
        // Option 1: Explicit handler class
        if (annotation.handler() != Query.DefaultHandler.class) {
            QueryHandler<Object, Object> handler = getHandler(annotation.handler());
            Object query;
            
            if (annotation.query() != Void.class) {
                query = buildObjectFromParams(annotation.query(), context.arguments());
            } else {
                // Infer from handler generic type
                Class<?> queryType = handler.getQueryType();
                if (queryType != null && queryType != Object.class) {
                    query = buildObjectFromParams(queryType, context.arguments());
                } else {
                    // Fallback to simpler auto-detection
                     query = buildQueryFromParams(annotation, context);
                }
            }
            
            return handler.handle(query);
        }

        // Option 2: Explicit query class
        if (annotation.query() != Void.class) {
            Object query = buildObjectFromParams(annotation.query(), context.arguments());
            return queryBus.dispatch(query);
        }

        // Option 3: Auto-detect from parameters
        Object query = extractPayload(context);
        if (query != null) {
            return queryBus.dispatch(query);
        }

        // Option 4: Simple query
        return queryBus.dispatch(new SimpleQuery(context.method(), context.arguments()));
    }

    @SuppressWarnings("unchecked")
    private Object dispatchCommand(HttpInvocationContext context, Command annotation) {
        // Option 1: Explicit handler class
        if (annotation.handler() != Command.DefaultHandler.class) {
            CommandHandler<Object> handler = getHandler(annotation.handler());
            Object command;
            
            if (annotation.command() != Void.class) {
                command = buildObjectFromParams(annotation.command(), context.arguments());
            } else {
                // Infer from handler generic type
                Class<?> commandType = handler.getCommandType();
                if (commandType != null && commandType != Object.class) {
                    command = buildObjectFromParams(commandType, context.arguments());
                } else {
                    command = buildCommandFromParams(annotation, context);
                }
            }
            
            handler.handle(command);
            return null;
        }

        // Option 2: Explicit command class
        if (annotation.command() != Void.class) {
            Object command = buildObjectFromParams(annotation.command(), context.arguments());
            commandBus.dispatch(command);
            return null;
        }

        // Option 3: Auto-detect from parameters
        Object command = extractPayload(context);
        if (command != null) {
            commandBus.dispatch(command);
        } else {
            commandBus.dispatch(new SimpleCommand(context.method(), context.arguments()));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getHandler(Class<?> handlerClass) {
        if (applicationContext != null) {
            return (T) applicationContext.getBean(handlerClass);
        }
        throw new CqrsDispatchException("ApplicationContext not available for handler lookup");
    }

    private Object buildQueryFromParams(Query annotation, HttpInvocationContext context) {
        if (annotation.query() != Void.class) {
            return buildObjectFromParams(annotation.query(), context.arguments());
        }
        // Create simple wrapper
        return new SimpleQuery(context.method(), context.arguments());
    }

    private Object buildCommandFromParams(Command annotation, HttpInvocationContext context) {
        if (annotation.command() != Void.class) {
            return buildObjectFromParams(annotation.command(), context.arguments());
        }
        // Create simple wrapper
        return new SimpleCommand(context.method(), context.arguments());
    }

    private Object buildObjectFromParams(Class<?> targetClass, Object[] args) {
        // If single argument matches target type, return it directly
        if (args.length == 1 && args[0] != null && targetClass.isAssignableFrom(args[0].getClass())) {
            return args[0];
        }

        try {
            // Try constructor matching args
            for (Constructor<?> ctor : targetClass.getConstructors()) {
                if (ctor.getParameterCount() == args.length) {
                    return ctor.newInstance(args);
                }
            }
            // Try no-arg constructor
            return targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CqrsDispatchException("Failed to create " + targetClass.getSimpleName(), e);
        }
    }

    private Object extractPayload(HttpInvocationContext context) {
        for (Object arg : context.arguments()) {
            if (arg != null && isPayloadType(arg.getClass())) {
                return arg;
            }
        }
        return null;
    }

    private boolean isPayloadType(Class<?> type) {
        return !type.isPrimitive() &&
               !type.equals(String.class) &&
               !Number.class.isAssignableFrom(type) &&
               !type.equals(Boolean.class);
    }

    public record SimpleQuery(Method method, Object[] arguments) {}
    public record SimpleCommand(Method method, Object[] arguments) {}
}
