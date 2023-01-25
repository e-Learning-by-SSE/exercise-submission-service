package net.ssehub.teaching.exercise_submission.service.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;

/**
 * Indicates that the given submission target does not exist.
 * 
 * @author Adam
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchTargetException extends StorageException {

    private static final long serialVersionUID = -4647180918103452956L;
    
    /**
     * Creates this exception for a missing target.
     * 
     * @param target The assignment and group that doesn't exist.
     */
    public NoSuchTargetException(SubmissionTarget target) {
        super("The group " + target.groupName() + " for assignment " + target.assignmentName() + " in course "
                + target.course() + " does not exist");
    }
    
    /**
     * Creates this exception for a missing version within the given target.
     * 
     * @param target The assignment and group.
     * @param timestamp The timestamp of a version that does not exist in the target.
     */
    public NoSuchTargetException(SubmissionTarget target, long timestamp) {
        super("The version " + timestamp + "does not exist for group "
                + target.groupName() + " in assignment " + target.assignmentName() + " in course "
                + target.course());
    }

}
