package net.ssehub.teaching.exercise_submission.service.submission;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import net.ssehub.teaching.exercise_submission.service.dto.CheckMessageDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.storage.ISubmissionStorage;
import net.ssehub.teaching.exercise_submission.service.storage.NoSuchTargetException;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.checks.Check;

/**
 * This class orchestrates a complete submission process. This should be the entry point for starting a submission.
 * 
 * @author Adam
 */
@Component
public class SubmissionManager {

    private ISubmissionStorage storage;
    
    private List<Check> checks;
    
    /**
     * Creates a new {@link SubmissionManager}.
     * 
     * @param storage The storage component to use.
     */
    public SubmissionManager(ISubmissionStorage storage) {
        this.storage = storage;
        this.checks = new LinkedList<>();
    }
    
    /**
     * Adds a check that is run for each submission. TODO: this should be gotten from the assignment configuration in
     * the student management system.
     * 
     * @param check The check to run.
     */
    public void addCheck(Check check) {
        this.checks.add(check);
    }
    
    /**
     * Executes a full submission.
     * <p>
     * This class runs the necessary {@link Check}s.
     * 
     * @param target The assignment and group to submit to.
     * @param submission The submission to add.
     * 
     * @return The result of the submission, including the messages created by the {@link Check}s.
     * 
     * @throws NoSuchTargetException If the given target does not exist.
     * @throws StorageException If an exception occurred in the storage backend.
     */
    public SubmissionResultDto submit(SubmissionTarget target, Submission submission)
            throws NoSuchTargetException, StorageException {
        
        List<CheckMessageDto> messages = new LinkedList<>();
        
        boolean allPassed = true;
        for (Check check : this.checks) {
            
            boolean passed = check.run(null);
            check.getResultMessages().stream()
                .map(m -> new CheckMessageDto(m))
                .forEach(messages::add);
            
            if (!passed) {
                allPassed = false;
                break;
            }
        }
        
        if (allPassed) {
            storage.submitNewVersion(target, submission);
        }
        
        return new SubmissionResultDto(allPassed, messages);
    }
    
}
