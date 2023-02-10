package net.ssehub.teaching.exercise_submission.service.stu_mgmt;

import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * A local view of the courses, users, groups, and assignment in the student management system.
 *  
 * @author Adam
 */
@Component
public class StuMgmtView {

    /**
     * Retrieves the {@link Course} specified by the given course ID.
     * 
     * @param courseId The ID of the course, e.g. <code>java-sose23</code>.
     * 
     * @return The specified {@link Course}; empty if course does not exist.
     */
    public Optional<Course> getCourse(String courseId) {
        return Optional.empty();
    }
    
}
