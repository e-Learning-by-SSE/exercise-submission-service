package net.ssehub.teaching.exercise_submission.service.routes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import net.ssehub.teaching.exercise_submission.service.dto.CheckMessageDto;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.Submission;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionBuilder;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionManager;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage.MessageType;

public class SubmissionControllerTest {

    private SubmissionTarget target;
    
    private List<FileDto> files;
    
    private SubmissionManager acceptingManager;
    private SubmissionResultDto acceptingResult;
    
    private SubmissionManager rejectingManager;
    private SubmissionResultDto rejectingResult;
    
    @BeforeEach
    public void initSubmission() throws StorageException {
        target = new SubmissionTarget("java-sose23", "Homework03", "JP042");
        files = List.of(
            new FileDto("Main.java", "testcontent"),
            new FileDto("util/Util.java", "testcontent")
        );
        
        SubmissionBuilder submissionBuilder = new SubmissionBuilder("JP042");
        submissionBuilder.addUtf8File(Path.of("Main.java"), "testcontent");
        submissionBuilder.addUtf8File(Path.of("util/Util.java"), "testcontent");
        Submission submission = submissionBuilder.build();
        
        acceptingManager = mock(SubmissionManager.class);
        acceptingResult = new SubmissionResultDto();
        acceptingResult.setAccepted(true);
        acceptingResult.setMessages(List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
        when(acceptingManager.submit(target, submission))
                .thenReturn(acceptingResult);
        
        rejectingManager = mock(SubmissionManager.class);
        rejectingResult = new SubmissionResultDto();
        rejectingResult.setAccepted(false);
        rejectingResult.setMessages(List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
        when(rejectingManager.submit(target, submission))
                .thenReturn(rejectingResult);
    }
    
    @Test
    public void submissionAccepted() throws StorageException {
        SubmissionController controller = new SubmissionController(acceptingManager);
        
        ResponseEntity<SubmissionResultDto> result = controller.submit(
                target.course(), target.assignmentName(), target.groupName(), files);
        
        assertAll(
            () -> assertSame(acceptingResult, result.getBody()),
            () -> assertEquals(HttpStatus.CREATED, result.getStatusCode())
        );
    }
    
    @Test
    public void submissionRejected() throws StorageException {
        SubmissionController controller = new SubmissionController(rejectingManager);
        
        ResponseEntity<SubmissionResultDto> result = controller.submit(
                target.course(), target.assignmentName(), target.groupName(), files);
        
        assertAll(
            () -> assertSame(rejectingResult, result.getBody()),
            () -> assertEquals(HttpStatus.OK, result.getStatusCode())
        );
    }
    
}
