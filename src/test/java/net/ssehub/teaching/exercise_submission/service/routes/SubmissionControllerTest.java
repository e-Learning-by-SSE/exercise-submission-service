package net.ssehub.teaching.exercise_submission.service.routes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import net.ssehub.teaching.exercise_submission.service.dto.CheckMessageDto;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionManager;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage.MessageType;

public class SubmissionControllerTest {

    @Test
    public void submissionAccepted() throws StorageException {
        String course = "java-sose23";
        String assignment = "Homework03";
        String group = "JP042";
        
        List<FileDto> files = List.of(
            new FileDto("Main.java", "testcontent"),
            new FileDto("util/Uitl.java", "testcontent")
        );
        
//        SubmissionBuilder sb = new SubmissionBuilder(group);
//        sb.addUtf8File(Path.of("Main.java"), "testcontent");
//        sb.addUtf8File(Path.of("util/Uitl.java"), "testcontent");
//        Submission submission = sb.build();
        
        SubmissionManager manager = mock(SubmissionManager.class);
        SubmissionResultDto managerResult = new SubmissionResultDto();
        managerResult.setAccepted(true);
        managerResult.setMessages(List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
        when(manager.submit(eq(new SubmissionTarget(course, assignment, group)), any()))
            // TODO: actually match submission above
                .thenReturn(managerResult);
        
        SubmissionController controller = new SubmissionController(manager);
        
        ResponseEntity<SubmissionResultDto> result = controller.submit(course, assignment, group, files);
        
        assertAll(
            () -> assertSame(managerResult, result.getBody()),
            () -> assertEquals(HttpStatus.CREATED, result.getStatusCode())
        );
    }
    
    @Test
    public void submissionRejected() throws StorageException {
        String course = "java-sose23";
        String assignment = "Homework03";
        String group = "JP042";
        
        List<FileDto> files = List.of(
            new FileDto("Main.java", "testcontent"),
            new FileDto("util/Uitl.java", "testcontent")
        );
        
//        SubmissionBuilder sb = new SubmissionBuilder(group);
//        sb.addUtf8File(Path.of("Main.java"), "testcontent");
//        sb.addUtf8File(Path.of("util/Uitl.java"), "testcontent");
//        Submission submission = sb.build();
        
        SubmissionManager manager = mock(SubmissionManager.class);
        SubmissionResultDto managerResult = new SubmissionResultDto();
        managerResult.setAccepted(false);
        managerResult.setMessages(List.of(new CheckMessageDto("test", MessageType.WARNING, "mock")));
        when(manager.submit(eq(new SubmissionTarget(course, assignment, group)), any()))
            // TODO: actually match submission above
                .thenReturn(managerResult);
        
        SubmissionController controller = new SubmissionController(manager);
        
        ResponseEntity<SubmissionResultDto> result = controller.submit(course, assignment, group, files);
        
        assertAll(
            () -> assertSame(managerResult, result.getBody()),
            () -> assertEquals(HttpStatus.OK, result.getStatusCode())
        );
    }
    
}
