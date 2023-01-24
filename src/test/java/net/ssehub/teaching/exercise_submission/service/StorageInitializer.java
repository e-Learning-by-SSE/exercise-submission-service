package net.ssehub.teaching.exercise_submission.service;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import net.ssehub.teaching.exercise_submission.service.StorageInitializer.Initializer;

@ContextConfiguration(initializers = Initializer.class)
public class StorageInitializer {

    @TempDir
    private static Path testStorage = Path.of("teststorage"); 
    
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("storage.location=" + testStorage).applyTo(applicationContext);
        }
        
    }
    
}
