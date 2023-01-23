package net.ssehub.teaching.exercise_submission.service.storage;

import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.Version;

/**
 * Indicates that the given submission target does not exist.
 * 
 * @author Adam
 */
public class NoSuchTargetException extends StorageException {

    private static final long serialVersionUID = -4647180918103452956L;
    
    /**
     * Creates this exception for a missing assignment.
     * 
     * @param course The course of the assignment that doesn't exist.
     * @param assignmentName The name of the assignment that doesn't exist.
     */
    public NoSuchTargetException(String course, String assignmentName) {
        super("The assignment " + assignmentName + " in course " + course + " does not exist");
    }
    
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
     * @param version The version that does not exist in the target.
     */
    public NoSuchTargetException(SubmissionTarget target, Version version) {
        super("The version " + version.creationTime().getEpochSecond() + "does not exist for group "
                + target.groupName() + " in assignment " + target.assignmentName() + " in course "
                + target.course());
    }

}
