package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

import java.util.Map;

/**
 * A group of participants that work together on an assignment.
 * 
 * @author Adam
 */
public record Group(String mgmtId, String name, Map<String, Participant> participantsByName) {

}
