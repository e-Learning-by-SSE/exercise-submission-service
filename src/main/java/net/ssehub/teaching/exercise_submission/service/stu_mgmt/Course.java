package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

import java.util.Map;

/**
 * A course in the student management system.
 * 
 * @author Adam
 */
public record Course(
        String id,
        Map<String, Participant> participantsByName,
        Map<String, Assignment> assignmentsByName) {

}
