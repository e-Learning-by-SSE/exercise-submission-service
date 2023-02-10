package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

/**
 * The state of an {@link Assignment}.
 * 
 * @author Adam
 */
public enum AssignmentState {
    INVISIBLE,
    CLOSED,
    IN_PROGRESS,
    IN_REVIEW,
    EVALUATED;
}
