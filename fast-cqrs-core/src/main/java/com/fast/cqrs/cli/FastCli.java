package com.fast.cqrs.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CLI Code Generator for Fast Framework.
 * <p>
 * Usage:
 * <pre>
 * java -jar fast-cli.jar generate controller Order
 * java -jar fast-cli.jar generate handler CreateOrder
 * java -jar fast-cli.jar generate entity Order
 * java -jar fast-cli.jar generate repository Order
 * java -jar fast-cli.jar generate event OrderCreated
 * java -jar fast-cli.jar generate aggregate Order
 * java -jar fast-cli.jar generate all Order
 * </pre>
 */
public class FastCli {

    private static final String BASE_PACKAGE = "com.example";
    private static final String SRC_PATH = "src/main/java";

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        String command = args[0];
        String type = args[1];
        String name = args.length > 2 ? args[2] : type;

        if ("generate".equals(command) || "g".equals(command)) {
            generate(type, name);
        } else {
            printUsage();
        }
    }

    private static void generate(String type, String name) {
        try {
            switch (type) {
                case "controller" -> generateController(name);
                case "handler" -> generateHandler(name);
                case "entity" -> generateEntity(name);
                case "repository" -> generateRepository(name);
                case "event" -> generateEvent(name);
                case "aggregate" -> generateAggregate(name);
                case "all" -> generateAll(name);
                default -> System.out.println("Unknown type: " + type);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void generateController(String name) throws IOException {
        String className = name + "Controller";
        String content = """
            package %s.controller;
            
            import com.fast.cqrs.annotation.HttpController;
            import com.fast.cqrs.annotation.Query;
            import com.fast.cqrs.annotation.Command;
            import org.springframework.web.bind.annotation.*;
            
            @HttpController
            @RequestMapping("/api/%s")
            public interface %s {
            
                @Query
                @GetMapping("/{id}")
                %sDto get%s(@PathVariable String id);
            
                @Command
                @PostMapping
                void create%s(@RequestBody Create%sCmd cmd);
            }
            """.formatted(
                BASE_PACKAGE,
                name.toLowerCase() + "s",
                className,
                name, name,
                name, name
            );
        
        writeFile("controller", className, content);
    }

    private static void generateHandler(String name) throws IOException {
        String className = name + "Handler";
        String content = """
            package %s.handler;
            
            import com.fast.cqrs.handler.CommandHandler;
            import org.springframework.stereotype.Component;
            
            @Component
            public class %s implements CommandHandler<%sCmd> {
            
                @Override
                public void handle(%sCmd cmd) {
                    // TODO: Implement
                }
            }
            """.formatted(BASE_PACKAGE, className, name, name);
        
        writeFile("handler", className, content);
    }

    private static void generateEntity(String name) throws IOException {
        String content = """
            package %s.entity;
            
            import com.fast.cqrs.sql.repository.Id;
            import com.fast.cqrs.sql.repository.Table;
            import com.fast.cqrs.sql.repository.Column;
            
            @Table("%ss")
            public class %s {
            
                @Id
                private String id;
            
                // TODO: Add fields
            
                public %s() {}
            
                public String getId() { return id; }
                public void setId(String id) { this.id = id; }
            }
            """.formatted(BASE_PACKAGE, name.toLowerCase(), name, name);
        
        writeFile("entity", name, content);
    }

    private static void generateRepository(String name) throws IOException {
        String className = name + "Repository";
        String content = """
            package %s.repository;
            
            import %s.entity.%s;
            import com.fast.cqrs.sql.annotation.SqlRepository;
            import com.fast.cqrs.sql.repository.FastRepository;
            
            @SqlRepository
            public interface %s extends FastRepository<%s, String> {
                // Custom queries here
            }
            """.formatted(BASE_PACKAGE, BASE_PACKAGE, name, className, name);
        
        writeFile("repository", className, content);
    }

    private static void generateEvent(String name) throws IOException {
        String className = name + "Event";
        String content = """
            package %s.event;
            
            import com.fast.cqrs.event.DomainEvent;
            
            public class %s extends DomainEvent {
            
                public %s(String aggregateId) {
                    super(aggregateId);
                }
            }
            """.formatted(BASE_PACKAGE, className, className);
        
        writeFile("event", className, content);
    }

    private static void generateAggregate(String name) throws IOException {
        String className = name + "Aggregate";
        String content = """
            package %s.aggregate;
            
            import com.fast.cqrs.eventsourcing.Aggregate;
            import com.fast.cqrs.eventsourcing.ApplyEvent;
            import com.fast.cqrs.eventsourcing.EventSourced;
            
            @EventSourced
            public class %s extends Aggregate {
            
                private String status;
            
                public %s() {
                    super();
                }
            
                // Commands
            
                // Event handlers
            }
            """.formatted(BASE_PACKAGE, className, className);
        
        writeFile("aggregate", className, content);
    }

    private static void generateAll(String name) throws IOException {
        generateEntity(name);
        generateRepository(name);
        generateController(name);
        generateHandler("Create" + name);
        generateEvent(name + "Created");
        generateAggregate(name);
        System.out.println("Generated all components for: " + name);
    }

    private static void writeFile(String packageSuffix, String className, String content) throws IOException {
        String packagePath = BASE_PACKAGE.replace('.', '/') + "/" + packageSuffix;
        Path dir = Paths.get(SRC_PATH, packagePath);
        Files.createDirectories(dir);
        
        Path file = dir.resolve(className + ".java");
        Files.writeString(file, content);
        System.out.println("Created: " + file);
    }

    private static void printUsage() {
        System.out.println("""
            Fast Framework CLI
            
            Usage: fast-cli generate <type> <name>
            
            Types:
              controller   Generate a CQRS controller interface
              handler      Generate a command/query handler
              entity       Generate an entity class
              repository   Generate a SQL repository
              event        Generate a domain event
              aggregate    Generate an event-sourced aggregate
              all          Generate all components for a domain
            
            Examples:
              fast-cli generate controller Order
              fast-cli generate all Product
            """);
    }
}
