package net.ssehub.teaching.exercise_submission.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The main application class to start this spring boot application.
 * 
 * @author Adam
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Exercise Submitter Server",
        description = "A sever for storing and retrieving exercise submissions.",
        version = "0.0.1"
    ),
    tags = {
        @Tag(name = "submission", description = "Sending and retrieving submission"),
    }
)
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(
        implicit = @OAuthFlow(
                authorizationUrl = "http://localhost:8090/authorize", tokenUrl = "http://localhost:8090/token")))
@SpringBootApplication
public class Application {

    /**
     * Main method of this application.
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
