package net.ssehub.teaching.exercise_submission.service.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Static utility methods for files and directories.
 * 
 * @author Adam
 */
public class FileUtils {
    
    private static boolean fileOperationsShouldFail;
    
    /**
     * Don't allow any instances.
     */
    private FileUtils() {}
    
    /**
     * Rigs file operations to throw {@link IOException}s. Supported methods:
     * <ul>
     *  <li>{@link #deleteDirectory(Path)} always throws</li>
     * </ul>
     * This method should only be used by test cases.
     * 
     * @param fileReadingShouldFail Whether all newly created file readers should throw {@link IOException}s.
     */
    static void setRigFileOperationsToFail(boolean fileReadingShouldFail) {
        FileUtils.fileOperationsShouldFail = fileReadingShouldFail;
    }
    
    /**
     * Deletes a directory with all content of it.
     * 
     * @param directory The folder to delete.
     * 
     * @throws IOException If deleting the directory fails.
     */
    public static void deleteDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException(directory + " is not a directory");
        }
        
        if (fileOperationsShouldFail) {
            throw new IOException("rigged to fail");
        }
        
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
