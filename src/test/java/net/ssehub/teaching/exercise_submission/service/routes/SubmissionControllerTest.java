package net.ssehub.teaching.exercise_submission.service.routes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import net.ssehub.teaching.exercise_submission.service.auth.AuthManager;
import net.ssehub.teaching.exercise_submission.service.dto.CheckMessageDto;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.dto.VersionDto;
import net.ssehub.teaching.exercise_submission.service.storage.ISubmissionStorage;
import net.ssehub.teaching.exercise_submission.service.storage.NoSuchTargetException;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.Submission;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionBuilder;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionManager;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.Version;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage.MessageType;

public class SubmissionControllerTest {
    
    private SubmissionTarget target;
    
    private Authentication author1Authentication;
    
    private AuthManager allAllowedAuthManager;
    
    @BeforeEach
    public void setupMocks() {
        target = new SubmissionTarget("java-sose23", "Homework03", "JP042");
        
        author1Authentication = mock(Authentication.class);
        when(author1Authentication.getName()).thenReturn("author1");
        
        allAllowedAuthManager = mock(AuthManager.class);
        when(allAllowedAuthManager.isSubmissionAllowed(any(), any())).thenReturn(true);
        when(allAllowedAuthManager.isReplayAllowed(any(), any())).thenReturn(true);
    }

    @Nested
    class Submit {
        
        private List<FileDto> files;
        
        private SubmissionManager acceptingManager;
        private SubmissionResultDto acceptingResult;
        
        private SubmissionManager rejectingManager;
        private SubmissionResultDto rejectingResult;
        
        @BeforeEach
        public void setupMocks() throws StorageException {
            files = List.of(
                FileDto.fromStringContent("Main.java", "testcontent"),
                FileDto.fromStringContent("util/Util.java", "testcontent")
            );
            
            SubmissionBuilder submissionBuilder = new SubmissionBuilder("author1");
            submissionBuilder.addUtf8File(Path.of("Main.java"), "testcontent");
            submissionBuilder.addUtf8File(Path.of("util/Util.java"), "testcontent");
            Submission submission = submissionBuilder.build();
            
            acceptingManager = mock(SubmissionManager.class);
            acceptingResult = new SubmissionResultDto(true,
                    List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
            when(acceptingManager.submit(target, submission))
                .thenReturn(acceptingResult);
            
            rejectingManager = mock(SubmissionManager.class);
            rejectingResult = new SubmissionResultDto(false,
                    List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
            when(rejectingManager.submit(target, submission))
                .thenReturn(rejectingResult);
        }
        
        @Test
        public void accepted() {
            SubmissionController controller = new SubmissionController(
                    acceptingManager, mock(ISubmissionStorage.class), allAllowedAuthManager);
            
            ResponseEntity<SubmissionResultDto> result = assertDoesNotThrow(() -> controller.submit(
                    target.course(), target.assignmentName(), target.groupName(), files, author1Authentication));
            
            assertAll(
                () -> assertEquals(acceptingResult, result.getBody()),
                () -> assertEquals(HttpStatus.CREATED, result.getStatusCode())
            );
        }
        
        @Test
        public void rejected() {
            SubmissionController controller = new SubmissionController(
                    rejectingManager, mock(ISubmissionStorage.class), allAllowedAuthManager);
            
            ResponseEntity<SubmissionResultDto> result = assertDoesNotThrow(() -> controller.submit(
                    target.course(), target.assignmentName(), target.groupName(), files, author1Authentication));
            
            assertAll(
                () -> assertEquals(rejectingResult, result.getBody()),
                () -> assertEquals(HttpStatus.OK, result.getStatusCode())
            );
        }
        
        @Test
        public void notAllowed() {
            AuthManager authManager = mock(AuthManager.class);
            when(authManager.isSubmissionAllowed(target, "author1")).thenReturn(false);
            
            SubmissionController controller = new SubmissionController(
                    acceptingManager, mock(ISubmissionStorage.class), authManager);
            
            assertThrows(UnauthorizedException.class, () -> controller.submit(
                    target.course(), target.assignmentName(), target.groupName(),
                    files, author1Authentication));
        }
        
        @Test
        public void invalidFilepathBadRequest() {
            SubmissionController controller = new SubmissionController(
                    rejectingManager, mock(ISubmissionStorage.class), allAllowedAuthManager);
            
            List<FileDto> files = List.of(FileDto.fromStringContent("../test.txt", "testcontent"));
            
            ResponseEntity<SubmissionResultDto> result = assertDoesNotThrow(() -> controller.submit(
                    target.course(), target.assignmentName(), target.groupName(), files, author1Authentication));
            
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        }
        
    }
    
    @Nested
    class ListVersions {
        
        private ISubmissionStorage storageWith2Versions;
        
        @BeforeEach
        public void setupMocks() throws StorageException {
            storageWith2Versions = mock(ISubmissionStorage.class);
            when(storageWith2Versions.getVersions(target)).thenReturn(List.of(
                new Version("author2", Instant.ofEpochSecond(123654)),
                new Version("author1", Instant.ofEpochSecond(123456))
            ));
        }
        
        @Test
        public void versionList() {
            SubmissionController controller = new SubmissionController(
                    mock(SubmissionManager.class), storageWith2Versions, allAllowedAuthManager);
            
            List<VersionDto> versions = assertDoesNotThrow(() -> controller.listVersions(
                    target.course(), target.assignmentName(), target.groupName(), author1Authentication));
            
            assertEquals(List.of(new VersionDto("author2", 123654), new VersionDto("author1", 123456)), versions);
        }
        
        @Test
        public void notAllowed() {
            AuthManager authManager = mock(AuthManager.class);
            when(authManager.isReplayAllowed(target, "author1")).thenReturn(false);
            
            SubmissionController controller = new SubmissionController(
                    mock(SubmissionManager.class), mock(ISubmissionStorage.class), authManager);
            
            assertThrows(UnauthorizedException.class, () -> controller.listVersions(
                    target.course(), target.assignmentName(), target.groupName(),
                    author1Authentication));
        }
        
    }
    
    @Nested
    class GetVersion {
        
        @Test
        public void notAllowed() {
            AuthManager authManager = mock(AuthManager.class);
            when(authManager.isReplayAllowed(target, "author1")).thenReturn(false);
            
            SubmissionController controller = new SubmissionController(
                    mock(SubmissionManager.class), mock(ISubmissionStorage.class), authManager);
            
            assertThrows(UnauthorizedException.class, () -> controller.getVersion(
                    target.course(), target.assignmentName(), target.groupName(), 1234L,
                    author1Authentication));
        }
        
        @Test
        public void versionDoesntExist() {
            ISubmissionStorage storage = mock(ISubmissionStorage.class);
            when(assertDoesNotThrow(() -> storage.getVersions(target)))
                .thenReturn(List.of(new Version("someone", Instant.ofEpochSecond(123456))));
            
            SubmissionController controller = new SubmissionController(
                    mock(SubmissionManager.class), storage, allAllowedAuthManager);
            
            assertThrows(NoSuchTargetException.class, () -> controller.getVersion(
                    target.course(), target.assignmentName(), target.groupName(), 654321, author1Authentication));
        }
        
        @Test
        public void versionReturned() {
            Version version = new Version("someone", Instant.ofEpochSecond(123456));
            SubmissionBuilder sb = new SubmissionBuilder("someauthor");
            sb.addUtf8File(Path.of("src/Main.java"), "some content");
            Submission submission = sb.build();
            
            ISubmissionStorage storage = mock(ISubmissionStorage.class);
            when(assertDoesNotThrow(() -> storage.getVersions(target)))
                .thenReturn(List.of(version));
            
            when(assertDoesNotThrow(() -> storage.getSubmission(target, version)))
                .thenReturn(submission);
            
            SubmissionController controller = new SubmissionController(
                    mock(SubmissionManager.class), storage, allAllowedAuthManager);
            
            List<FileDto> files = assertDoesNotThrow(() -> controller.getVersion(
                    target.course(), target.assignmentName(), target.groupName(), 123456, author1Authentication));
            
            assertEquals(List.of(FileDto.fromStringContent("src/Main.java", "some content")), files);
        }
        
    }
    
    
}
