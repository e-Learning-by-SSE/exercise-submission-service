package net.ssehub.teaching.exercise_submission.service.submission;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.ssehub.teaching.exercise_submission.service.dto.CheckMessageDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.storage.ISubmissionStorage;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.checks.Check;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage.MessageType;

public class SubmissionManagerTest {
    
    @Test
    public void failedCheckNotAcceptedAndNotStored() throws StorageException {
        ISubmissionStorage storage = mock(ISubmissionStorage.class);
        
        Check check = mock(Check.class);
        when(check.run(any())).thenReturn(false);
        
        SubmissionManager manager = new SubmissionManager(storage);
        manager.addCheck(check);
        
        SubmissionTarget target = new SubmissionTarget("c", "a", "g");
        Submission submission = new SubmissionBuilder("s").build();
        SubmissionResultDto result = assertDoesNotThrow(() -> manager.submit(target, submission));
        
        assertAll(
            () -> verify(storage, never()).submitNewVersion(any(), any()),
            () -> assertFalse(result.accepted())
        );
    }
    
    @Test
    public void notFailedCheckAcceptedAndStored() throws StorageException {
        ISubmissionStorage storage = mock(ISubmissionStorage.class);
        
        Check check = mock(Check.class);
        when(check.run(any())).thenReturn(true);
        
        SubmissionManager manager = new SubmissionManager(storage);
        manager.addCheck(check);
        
        SubmissionTarget target = new SubmissionTarget("c", "a", "g");
        Submission submission = new SubmissionBuilder("s").build();
        SubmissionResultDto result = assertDoesNotThrow(() -> manager.submit(target, submission));
        
        assertAll(
            () -> verify(storage, times(1)).submitNewVersion(target, submission),
            () -> assertTrue(result.accepted())
        );
    }
    
    @Test
    public void failedCheckMessagesReturned() throws StorageException {
        ISubmissionStorage storage = mock(ISubmissionStorage.class);
        
        Check check = mock(Check.class);
        when(check.run(any())).thenReturn(false);
        when(check.getResultMessages()).thenReturn(List.of(new ResultMessage("test", MessageType.ERROR, "mock")));
        
        SubmissionManager manager = new SubmissionManager(storage);
        manager.addCheck(check);
        
        SubmissionTarget target = new SubmissionTarget("c", "a", "g");
        Submission submission = new SubmissionBuilder("s").build();
        SubmissionResultDto result = assertDoesNotThrow(() -> manager.submit(target, submission));

        assertEquals(List.of(new CheckMessageDto("test", MessageType.ERROR, "mock")), result.messages());
    }
    
    @Test
    public void notFailedCheckMessagesReturned() throws StorageException {
        ISubmissionStorage storage = mock(ISubmissionStorage.class);
        
        Check check = mock(Check.class);
        when(check.run(any())).thenReturn(true);
        when(check.getResultMessages()).thenReturn(List.of(new ResultMessage("test", MessageType.ERROR, "mock")));
        
        SubmissionManager manager = new SubmissionManager(storage);
        manager.addCheck(check);
        
        SubmissionTarget target = new SubmissionTarget("c", "a", "g");
        Submission submission = new SubmissionBuilder("s").build();
        SubmissionResultDto result = assertDoesNotThrow(() -> manager.submit(target, submission));

        assertEquals(List.of(new CheckMessageDto("test", MessageType.ERROR, "mock")), result.messages());
    }
    
}
