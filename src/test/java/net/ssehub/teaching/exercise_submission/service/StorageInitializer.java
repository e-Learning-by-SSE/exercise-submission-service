package net.ssehub.teaching.exercise_submission.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import net.ssehub.teaching.exercise_submission.service.StorageInitializer.Initializer;
import net.ssehub.teaching.exercise_submission.service.util.FileUtils;

@ContextConfiguration(initializers = Initializer.class)
public class StorageInitializer {

    @TempDir
    protected static Path testStorage = Path.of("teststorage"); 
    
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("storage.location=" + testStorage).applyTo(applicationContext);
        }
        
    }
    
    @AfterEach
    public void clearTestStorage() throws IOException {
        FileUtils.deleteDirectory(testStorage);
        Files.createDirectory(testStorage);
    }
    
}
