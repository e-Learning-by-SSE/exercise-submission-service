package net.ssehub.teaching.exercise_submission.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTest {
    
    @BeforeAll
    public static void createEmptyTeststorage() throws IOException {
        Files.createDirectory(Path.of("teststorage"));
    }
    
    @Test
    public void contextLoads() {
        
    }
    
}
