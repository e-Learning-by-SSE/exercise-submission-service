package net.ssehub.teaching.exercise_submission.service.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileUitlsTest {

    private static final Path TESTDATA = Path.of("src/test/resources/FileUtilsTest");
    
    private static final Path TEMP_DIRECTORY = TESTDATA.resolve("temp_directory");

    @BeforeEach
    public void createTempDirectory() {
        if (!Files.isDirectory(TEMP_DIRECTORY)) {
            try {
                Files.createDirectory(TEMP_DIRECTORY);
            } catch (IOException e) {
                e.printStackTrace();
                fail("Setup: Could not create empty temporary test directory " + TEMP_DIRECTORY);
            }
        }
    }
    
    @AfterEach
    public void clearTempDirectory() throws IOException {
        if (Files.isDirectory(TEMP_DIRECTORY)) {
            Files.walkFileTree(TEMP_DIRECTORY, new SimpleFileVisitor<Path>() {
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
            });
        }
    }
    
    @Test
    public void deleteNonExistingDirecotry() throws IOException {
        Path file = TESTDATA.resolve("doesnt_exist");
        assertThat("Precondition: test file should not exist",
                Files.exists(file), is(false));
        
        assertThrows(IOException.class, () -> {
            FileUtils.deleteDirectory(file);
        });
    }
    
    @Test
    public void deleteDirectoryOnFile() throws IOException {
        Path file = TESTDATA.resolve("some_file.txt");
        assertThat("Precondition: test file should exist",
                Files.isRegularFile(file), is(true));
        
        assertThrows(IOException.class, () -> {
            FileUtils.deleteDirectory(file);
        });
    }
    
    @Test
    public void deleteDirectoryEmpty() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(TEMP_DIRECTORY), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(TEMP_DIRECTORY).count(), is(0L));
        
        FileUtils.deleteDirectory(TEMP_DIRECTORY);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(TEMP_DIRECTORY), is(false));
    }
    
    @Test
    public void deleteDirectorySingleFileInside() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(TEMP_DIRECTORY), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(TEMP_DIRECTORY).count(), is(0L));
        
        Path file = TEMP_DIRECTORY.resolve("some_file.txt");
        Files.createFile(file);
        
        assertThat("Precondition: directory should not be empty",
                Files.list(TEMP_DIRECTORY).count(), is(1L));
        
        FileUtils.deleteDirectory(TEMP_DIRECTORY);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(TEMP_DIRECTORY), is(false));
        assertThat("Postcondition: file should not exist",
                Files.exists(file), is(false));
    }
    
    @Test
    public void deleteDirectoryMultipleFilesInside() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(TEMP_DIRECTORY), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(TEMP_DIRECTORY).count(), is(0L));
        
        for (int i = 0; i < 10; i++) {
            Path file = TEMP_DIRECTORY.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        assertThat("Precondition: directory should not be empty",
                Files.list(TEMP_DIRECTORY).count(), is(10L));
        
        FileUtils.deleteDirectory(TEMP_DIRECTORY);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(TEMP_DIRECTORY), is(false));
    }
    
    @Test
    public void deleteDirectoryCantDeleteFile() throws IOException {
        Path directory = TESTDATA.resolve("singleFile");
        assertThat("Precondition: directory should exist",
                Files.isDirectory(directory), is(true));
        assertThat("Precondition: directory should not be empty",
                Files.list(directory).count(), is(1L));
        
        Path file = directory.resolve("some_file.txt");
        assertThat("Precondition: test file should exist",
                Files.isRegularFile(file), is(true));
        
        FileUtils.setRigFileOperationsToFail(true);
        
        try {
            assertThrows(IOException.class, () -> {
                FileUtils.deleteDirectory(directory);
            });
        } finally {
            FileUtils.setRigFileOperationsToFail(false);
        }
    }
    
    @Test
    public void deleteDirectoryNestedDirectories() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(TEMP_DIRECTORY), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(TEMP_DIRECTORY).count(), is(0L));
        
        Path nested = TEMP_DIRECTORY.resolve("nested");
        Files.createDirectory(nested);
        assertThat("Precondition: nested directory should exist",
                Files.isDirectory(TEMP_DIRECTORY), is(true));
        
        for (int i = 0; i < 10; i++) {
            Path file = nested.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        for (int i = 0; i < 10; i++) {
            Path file = TEMP_DIRECTORY.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        assertThat("Precondition: directory should not be empty",
                Files.list(TEMP_DIRECTORY).count(), is(11L));
        
        assertThat("Precondition: nested directory should not be empty",
                Files.list(nested).count(), is(10L));
        
        FileUtils.deleteDirectory(TEMP_DIRECTORY);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(TEMP_DIRECTORY), is(false));
        assertThat("Postcondition: nested directory should not exist",
                Files.exists(nested), is(false));
    }
    
}
