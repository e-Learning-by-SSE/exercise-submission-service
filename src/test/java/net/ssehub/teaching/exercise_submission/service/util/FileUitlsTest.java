package net.ssehub.teaching.exercise_submission.service.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUitlsTest {

    private static final Path TESTDATA = Path.of("src/test/resources/FileUtilsTest");
    
    @TempDir
    private Path tempDir;
    
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
                Files.isDirectory(tempDir), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(tempDir).count(), is(0L));
        
        FileUtils.deleteDirectory(tempDir);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(tempDir), is(false));
    }
    
    @Test
    public void deleteDirectorySingleFileInside() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(tempDir), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(tempDir).count(), is(0L));
        
        Path file = tempDir.resolve("some_file.txt");
        Files.createFile(file);
        
        assertThat("Precondition: directory should not be empty",
                Files.list(tempDir).count(), is(1L));
        
        FileUtils.deleteDirectory(tempDir);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(tempDir), is(false));
        assertThat("Postcondition: file should not exist",
                Files.exists(file), is(false));
    }
    
    @Test
    public void deleteDirectoryMultipleFilesInside() throws IOException {
        assertThat("Precondition: directory should exist",
                Files.isDirectory(tempDir), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(tempDir).count(), is(0L));
        
        for (int i = 0; i < 10; i++) {
            Path file = tempDir.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        assertThat("Precondition: directory should not be empty",
                Files.list(tempDir).count(), is(10L));
        
        FileUtils.deleteDirectory(tempDir);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(tempDir), is(false));
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
                Files.isDirectory(tempDir), is(true));
        assertThat("Precondition: directory should be empty",
                Files.list(tempDir).count(), is(0L));
        
        Path nested = tempDir.resolve("nested");
        Files.createDirectory(nested);
        assertThat("Precondition: nested directory should exist",
                Files.isDirectory(tempDir), is(true));
        
        for (int i = 0; i < 10; i++) {
            Path file = nested.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        for (int i = 0; i < 10; i++) {
            Path file = tempDir.resolve("some_file" + i + ".txt");
            Files.createFile(file);
        }
        
        assertThat("Precondition: directory should not be empty",
                Files.list(tempDir).count(), is(11L));
        
        assertThat("Precondition: nested directory should not be empty",
                Files.list(nested).count(), is(10L));
        
        FileUtils.deleteDirectory(tempDir);
        
        assertThat("Postcondition: directory should not exist",
                Files.exists(tempDir), is(false));
        assertThat("Postcondition: nested directory should not exist",
                Files.exists(nested), is(false));
    }
    
}
