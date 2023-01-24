package net.ssehub.teaching.exercise_submission.service.auth;

import org.springframework.stereotype.Component;

import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;

/**
 * Checks if a given user is authorized to do certain operations.
 * 
 * @author Adam
 */
@Component
public class AuthManager {

    /**
     * Checks if the given user is allowed to submit a new version.
     * 
     * @param target The target that the user wants to submit to.
     * @param username The username of the user that wants to submit.
     * 
     * @return Whether the user is allowed to do this operation.
     */
    public boolean isSubmissionAllowed(SubmissionTarget target, String username) {
        return true;
    }
    
    /**
     * Checks if the given user is allowed to replay the previous versions from the given target.
     * 
     * @param target The target that the user wants to replay from.
     * @param username The username of the user that wants to replay.
     * 
     * @return Whether the user is allowed to do this operation.
     */
    public boolean isReplayAllowed(SubmissionTarget target, String username) {
        return true;
    }
    
}
