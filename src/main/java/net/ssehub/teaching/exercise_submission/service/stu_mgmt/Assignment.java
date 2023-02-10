package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

import java.util.List;
import java.util.Map;

/**
 * An assignment in the student management system.
 * 
 * @author Adam
 */
public record Assignment(
        String mgmtId,
        String name,
        AssignmentState state,
        Collaboration collaboration,
        Map<String, Group> groupsByNames,
        List<CheckConfiguration> checkConfigurations) {

}
