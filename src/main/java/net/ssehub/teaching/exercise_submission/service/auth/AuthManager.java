package net.ssehub.teaching.exercise_submission.service.auth;

import org.springframework.stereotype.Component;

import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Assignment;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.AssignmentState;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Group;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Participant;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Role;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.StuMgmtView;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;

/**
 * Checks if a given user is authorized to do certain operations.
 * 
 * @author Adam
 */
@Component
public class AuthManager {
    
    private StuMgmtView stuMgmtView;
    
    /**
     * Creates an {@link AuthManager}.
     * 
     * @param stuMgmtView The view on the student management system to get permissions from.
     */
    public AuthManager(StuMgmtView stuMgmtView) {
        this.stuMgmtView = stuMgmtView;
    }
    
    /**
     * Checks if a user with the given role can submit to an assignment in the given state.
     * <p>
     * Package visibility for test cases.
     * 
     * @param assignmentState The state of the assignment.
     * @param userRole The role of the user.
     * 
     * @return Whether the user can submit to the assignment.
     */
    static boolean roleCanSubmitToAssignmentState(AssignmentState assignmentState, Role userRole) {
        boolean isTutor = userRole == Role.LECTURER || userRole == Role.TUTOR;
        return isTutor || assignmentState == AssignmentState.IN_PROGRESS;
    }
    
    /**
     * Checks if a user with the given role can replay an assignment in the given state.
     * <p>
     * Package visibility for test cases.
     * 
     * @param assignmentState The state of the assignment.
     * @param userRole The role of the user.
     * 
     * @return Whether the user can replay the assignment.
     */
    static boolean roleCanReplayAssignmentState(AssignmentState assignmentState, Role userRole) {
        boolean isTutor = userRole == Role.LECTURER || userRole == Role.TUTOR;
        return isTutor
                || assignmentState == AssignmentState.IN_PROGRESS || assignmentState == AssignmentState.EVALUATED;
    }
    
    /**
     * Checks if the given participant is allowed to access (submit or replay) the given target group in the given
     * assignment. This depends on the collaboration type and the participants role.
     * 
     * @param participant The participant that tries to access the target group.
     * @param assignment The assignment that the participant tries to access.
     * @param targetGroupName The name of the group that the participant is trying to access.
     * 
     * @return Whether this access is allowed.
     */
    static boolean participantCanAccessGroup(Participant participant, Assignment assignment, String targetGroupName) {
        boolean allowed;
        boolean isTutor = participant.role() == Role.LECTURER || participant.role() == Role.TUTOR;
        
        if (isTutor) {
            allowed = true;
        } else {
            
            switch (assignment.collaboration()) {
            case SINGLE:
                allowed = targetGroupName.equals(participant.name());
                break;
                
            case GROUP: {
                Group group = assignment.groupsByNames().get(targetGroupName);
                if (group != null) {
                    allowed = group.participantsByName().containsKey(participant.name());
                } else {
                    allowed = false;
                }
                break;
            }
            
            case GROUP_OR_SINGLE: {
                Group group = assignment.groupsByNames().get(targetGroupName);
                if (targetGroupName.equals(participant.name())) {
                    allowed = true;
                } else if (group != null) {
                    allowed = group.participantsByName().containsKey(participant.name());
                } else {
                    allowed = false;
                }
                break;
            }
            
            default:
                allowed = false; // be safe here
                break;
            }
        }
        
        return allowed;
    }

    /**
     * Checks if the given user is allowed to submit a new version.
     * 
     * @param target The target that the user wants to submit to.
     * @param username The username of the user that wants to submit.
     * 
     * @return Whether the user is allowed to do this operation.
     */
    public boolean isSubmissionAllowed(SubmissionTarget target, String username) {
        return stuMgmtView.getCourse(target.course()).map(course -> {
            Participant participant = course.participantsByName().get(username);
            Assignment assignment = course.assignmentsByName().get(target.assignmentName());
            
            boolean allowed;
            if (participant != null && assignment != null) {
                
                allowed = roleCanSubmitToAssignmentState(assignment.state(), participant.role())
                        && participantCanAccessGroup(participant, assignment, target.groupName());
                
            } else {
                allowed = false;
            }
            return allowed;
        }).orElse(false);
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
        return stuMgmtView.getCourse(target.course()).map(course -> {
            Participant participant = course.participantsByName().get(username);
            Assignment assignment = course.assignmentsByName().get(target.assignmentName());
            
            boolean allowed;
            if (participant != null && assignment != null) {
                
                allowed = roleCanReplayAssignmentState(assignment.state(), participant.role())
                        && participantCanAccessGroup(participant, assignment, target.groupName());
                
            } else {
                allowed = false;
            }
            return allowed;
        }).orElse(false);
    }
    
}
