package net.ssehub.teaching.exercise_submission.service.submission;

/**
 * Represents a target for a submission, including course, assignment, and group.
 * 
 * @author Adam
 */
public record SubmissionTarget(String course, String assignmentName, String groupName) {

}
