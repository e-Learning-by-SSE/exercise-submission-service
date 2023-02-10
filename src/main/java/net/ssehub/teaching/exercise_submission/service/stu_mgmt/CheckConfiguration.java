package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

import java.util.Map;

import net.ssehub.teaching.exercise_submission.service.submission.checks.Check;

/**
 * A configuration for a {@link Check} for an {@link Assignment}, typically specified by the lecturer of the course.
 * 
 * @author Adam
 */
public record CheckConfiguration(String checkName, boolean rejecting, Map<String, String> properties) {

}
