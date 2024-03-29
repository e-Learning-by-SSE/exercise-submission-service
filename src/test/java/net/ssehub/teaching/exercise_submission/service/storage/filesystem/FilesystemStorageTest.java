package net.ssehub.teaching.exercise_submission.service.storage.filesystem;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.ssehub.teaching.exercise_submission.service.storage.NoSuchTargetException;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.Submission;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionBuilder;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.Version;

public class FilesystemStorageTest {

    @TempDir
    private Path storageDir;
    
    @Test
    public void constructorNonExistingDirectoryThrows()  {
        IOException e = assertThrows(IOException.class, () -> new FilesystemStorage(Path.of("doesnt_exist")));
        assertEquals("doesnt_exist is not a directory", e.getMessage());
    }
    
    @Test
    public void createOrUpdateAssignmentCreatesDirectories() throws IOException {
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertDoesNotThrow(() ->
                storage.createOrUpdateAssignment("somecourse-wise2122", "FirstAssignment", "Group01", "Group02"));
        
        assertAll(
            () -> assertTrue(Files.isDirectory(storageDir.resolve(
                    "somecourse-wise2122"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve(
                    "somecourse-wise2122/FirstAssignment"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve(
                    "somecourse-wise2122/FirstAssignment/Group01"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve(
                    "somecourse-wise2122/FirstAssignment/Group02")))
        );
    }
    
    @Test
    public void createOrUpdateAssignmentExistingDirectoryAddsNewGroups() throws IOException {
        Files.createDirectories(storageDir.resolve("course-wise2122/Homework01/Group02"));
        Files.createDirectories(storageDir.resolve("course-wise2122/Homework01/Group03"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertDoesNotThrow(() ->
                storage.createOrUpdateAssignment("course-wise2122", "Homework01", "Group01", "Group02"));
        
        assertAll(
            () -> assertTrue(Files.isDirectory(storageDir.resolve("course-wise2122"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve("course-wise2122/Homework01"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve("course-wise2122/Homework01/Group01"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve("course-wise2122/Homework01/Group02"))),
            () -> assertTrue(Files.isDirectory(storageDir.resolve("course-wise2122/Homework01/Group03")))
        );
    }
    
    @Test
    public void getVersionsEmptyForEmptyGroup() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        List<Version> versions = assertDoesNotThrow(
            () -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01")));
        
        assertEquals(Collections.emptyList(), versions);
    }
    
    @Test
    public void getVersionsNonExistingCourseThrows() throws IOException {
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class,
                () -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01")));
    }
    
    @Test
    public void getVersionsNonExistingAssignmentThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class,
            () -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01")));
    }
    
    @Test
    public void getVersionsNonExistingGroupThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class,
            () -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01")));
    }
    
    @Test
    public void getVersionsReturnsPreExistingDirectory() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student1"));
        
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertEquals(Arrays.asList(new Version("student1", timestamp)),
                assertDoesNotThrow(() -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01"))));
    }
    
    @Test
    public void getVersionsInvalidDirectoryNameThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/invalid"));
        
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        StorageException e = assertThrows(StorageException.class,
            () -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01")));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }
    
    @Test
    public void getMultipleVersionsSorted() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student1"));
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/32400_student1"));
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738611_student1"));
        
        Instant t1 = Instant.ofEpochSecond(1634738601L);
        Instant t2 = Instant.ofEpochSecond(32400L);
        Instant t3 = Instant.ofEpochSecond(1634738611L);
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertEquals(Arrays.asList(
                new Version("student1", t3),
                new Version("student1", t1),
                new Version("student1", t2)
                ),
                assertDoesNotThrow(() -> storage.getVersions(new SubmissionTarget("course", "Homework01", "Group01"))));
    }
    
    @Test
    public void filenameToVersionInvalidFormats() {
        assertAll(
            // no underscore
            () -> assertThrows(IllegalArgumentException.class, () -> FilesystemStorage.filenameToVersion("abc")),
            // invalid timestamp
            () -> assertThrows(IllegalArgumentException.class, () -> FilesystemStorage.filenameToVersion("abc_author")),
            // no author
            () -> assertThrows(IllegalArgumentException.class, () -> FilesystemStorage.filenameToVersion("123_"))
        );
    }
    
    @Test
    public void filenameToVersionValidFormat() {
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertEquals(new Version("some_author", timestamp),
                FilesystemStorage.filenameToVersion("1634738601_some_author"));
    }
    
    @Test
    public void versiontoFilename() {
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertEquals("1634738601_author-name",
                FilesystemStorage.versionToFilename(new Version("author-name", timestamp)));
    }
    
    @Test
    public void getSubmissionNonExistingCourseThrows() throws IOException {
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertThrows(NoSuchTargetException.class, () -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
    }
    
    @Test
    public void getSubmissionNonExistingAssignmentThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertThrows(NoSuchTargetException.class, () -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
    }
    
    @Test
    public void getSubmissionNonExistingGroupThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertThrows(NoSuchTargetException.class, () -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
    }
    
    @Test
    public void getSubmissionNonExistingVersionThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        assertThrows(NoSuchTargetException.class, () -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
    }
    
    @Test
    public void getSubmissionEmpty() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        
        Submission submission = assertDoesNotThrow(() -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
        
        assertAll(
            () -> assertEquals(0, submission.getNumFiles()),
            () -> assertEquals(Collections.emptySet(), submission.getFilepaths())
        );
    }
    
    @Test
    public void getSubmissionSingleFile() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student"));
        Files.writeString(storageDir.resolve("course/Homework01/Group01/1634738601_student/test.txt"),
                "some content\n", StandardCharsets.UTF_8);
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        
        Submission submission = assertDoesNotThrow(() -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
        
        assertAll(
            () -> assertEquals(1, submission.getNumFiles()),
            () -> assertArrayEquals("some content\n".getBytes(StandardCharsets.UTF_8),
                    submission.getFileContent(Path.of("test.txt")))
        );
    }
    
    @Test
    public void getSubmissionMultipleFilesAndDirectories() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student/dir1"));
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student/dir2/subdir"));
        Files.writeString(storageDir.resolve("course/Homework01/Group01/1634738601_student/dir1/test.txt"),
                "first content\n", StandardCharsets.UTF_8);
        Files.writeString(storageDir.resolve("course/Homework01/Group01/1634738601_student/dir1/other.txt"),
                "second content\n", StandardCharsets.UTF_8);
        Files.writeString(storageDir.resolve(
                "course/Homework01/Group01/1634738601_student/dir2/subdir/another.txt"),
                "third cöntent\n", StandardCharsets.UTF_8);
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant timestamp = Instant.ofEpochSecond(1634738601L);
        
        Submission submission = assertDoesNotThrow(() -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", timestamp)));
        
        assertAll(
            () -> assertEquals(3, submission.getNumFiles()),
            () -> assertArrayEquals("first content\n".getBytes(StandardCharsets.UTF_8),
                    submission.getFileContent(Path.of("dir1/test.txt"))),
            () -> assertArrayEquals("second content\n".getBytes(StandardCharsets.UTF_8),
                    submission.getFileContent(Path.of("dir1/other.txt"))),
            () -> assertArrayEquals("third cöntent\n".getBytes(StandardCharsets.UTF_8),
                    submission.getFileContent(Path.of("dir2/subdir/another.txt")))
        );
    }
    
    @Test
    public void getSubmissionTwoVersions() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634738601_student"));
        Files.writeString(storageDir.resolve("course/Homework01/Group01/1634738601_student/test.txt"),
                "some content\n", StandardCharsets.UTF_8);
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01/1634801393_friend"));
        Files.writeString(storageDir.resolve("course/Homework01/Group01/1634801393_friend/other.txt"),
                "other content\n", StandardCharsets.UTF_8);
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        Instant t1 = Instant.ofEpochSecond(1634738601L);
        Instant t2 = Instant.ofEpochSecond(1634801393L);
        
        Submission s1 = assertDoesNotThrow(() -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("student", t1)));
        Submission s2 = assertDoesNotThrow(() -> storage.getSubmission(
                new SubmissionTarget("course", "Homework01", "Group01"), new Version("friend", t2)));
        
        assertAll(
            () -> assertEquals(1, s1.getNumFiles()),
            () -> assertArrayEquals("some content\n".getBytes(StandardCharsets.UTF_8),
                    s1.getFileContent(Path.of("test.txt"))),
            
            () -> assertEquals(1, s2.getNumFiles()),
            () -> assertArrayEquals("other content\n".getBytes(StandardCharsets.UTF_8),
                    s2.getFileContent(Path.of("other.txt")))
        );
    }
    
    @Test
    public void submitNewVersionNonExistingCourseThrows() throws IOException {
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class, () -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("student").build()));
    }
    
    @Test
    public void submitNewVersionNonExistingAssignmentThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class, () -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("student").build()));
    }
    
    @Test
    public void submitNewVersionNonExistingGroupThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertThrows(NoSuchTargetException.class, () -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("student").build()));
    }
    
    @Test
    public void submitNewVersionVersionAlreadyExistsThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        // create folders for versions covering the next 100 seconds
        long now = Instant.now().getEpochSecond();
        for (int i = 0; i < 100; i++) {
            Files.createDirectory(storageDir.resolve("course/Homework01/Group01/" + (now + i) + "_student"));
        }
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        StorageException e = assertThrows(StorageException.class, () -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("student").build()));
        assertEquals("Version already exists", e.getMessage());
    }
    
    @Test
    public void submitNewVersionVersionWithDifferentAuthorAlreadyExistsThrows() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        // create folders for versions covering the next 100 seconds
        long now = Instant.now().getEpochSecond();
        for (int i = 0; i < 100; i++) {
            Files.createDirectory(storageDir.resolve("course/Homework01/Group01/" + (now + i) + "_author1"));
        }
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        StorageException e = assertThrows(StorageException.class, () -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("author2").build()));
        assertEquals("Version already exists", e.getMessage());
    }
    
    @Test
    public void submitNewVersionEmptySubmissionCreatesDirectory() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        assertDoesNotThrow(() -> storage.submitNewVersion(
                new SubmissionTarget("course", "Homework01", "Group01"), new SubmissionBuilder("student").build()));
        
        Optional<Path> versionDir = Files.list(storageDir.resolve("course/Homework01/Group01")).findFirst();
        
        assertAll(
            () -> assertTrue(versionDir.isPresent()),
            () -> assertTrue(versionDir.get().getFileName().toString().endsWith("_student")),
            () -> assertTrue(Files.list(versionDir.get()).findAny().isEmpty())
        );
    }
    
    @Test
    public void submitNewVersionWritesContent() throws IOException {
        Files.createDirectories(storageDir.resolve("course/Homework01/Group01"));
        
        FilesystemStorage storage = new FilesystemStorage(storageDir);
        
        SubmissionBuilder builder = new SubmissionBuilder("random-author");
        builder.addUtf8File(Path.of("test.txt"), "some content\n");
        builder.addUtf8File(Path.of("dir/test.txt"), "other content\n");
        
        assertDoesNotThrow(
            () -> storage.submitNewVersion(new SubmissionTarget("course", "Homework01", "Group01"), builder.build()));
        
        Optional<Path> versionDir = Files.list(storageDir.resolve("course/Homework01/Group01")).findFirst();
        
        assertAll(
            () -> assertTrue(versionDir.isPresent()),
            () -> assertTrue(versionDir.get().getFileName().toString().endsWith("_random-author")),
            () -> assertEquals("some content\n",
                    Files.readString(versionDir.get().resolve("test.txt"), StandardCharsets.UTF_8)),
            () -> assertEquals("other content\n",
                    Files.readString(versionDir.get().resolve("dir/test.txt"), StandardCharsets.UTF_8))
        );
    }
    
}
