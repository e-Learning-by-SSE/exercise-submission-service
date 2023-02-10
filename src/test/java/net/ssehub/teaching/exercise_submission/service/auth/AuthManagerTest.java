package net.ssehub.teaching.exercise_submission.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Assignment;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.AssignmentState;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Collaboration;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Course;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Group;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Participant;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.Role;
import net.ssehub.teaching.exercise_submission.service.stu_mgmt.StuMgmtView;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;

public class AuthManagerTest {

    @Nested
    public class StaticHelpers {
        
        private static final Map<Role, Integer> ROLE_INDEX = Map.of(
                Role.LECTURER, 0,
                Role.TUTOR, 1,
                Role
                .STUDENT, 2);
        
        private static final Map<AssignmentState, Integer> STATE_INDEX = Map.of(
                AssignmentState.INVISIBLE, 0,
                AssignmentState.CLOSED, 1,
                AssignmentState.IN_PROGRESS, 2,
                AssignmentState.IN_REVIEW, 3,
                AssignmentState.EVALUATED, 4);
        
        private static final boolean[][] SUBMISSIONS = {
                        /* INVISIBLE */ /* CLOSED */ /* IN_PROGRESS */ /* IN_REVIEW */ /* EVALUATED */
           /*LECTURER*/ {true,             true,        true,             true,           true},
           /*TUTOR*/    {true,             true,        true,             true,           true},
           /*STUDENT*/  {false,            false,       true,             false,          false}
        };
        
        private static final boolean[][] REPLAY = {
                        /* INVISIBLE */ /* CLOSED */ /* IN_PROGRESS */ /* IN_REVIEW */ /* EVALUATED */
           /*LECTURER*/ {true,             true,        true,             true,           true},
           /*TUTOR*/    {true,             true,        true,             true,           true},
           /*STUDENT*/  {false,            false,       true,             false,          true}
        };
        
        @Test
        public void roleCanSubmitToAssignmentState() {
            for (Role role : Role.values()) {
                for (AssignmentState  state : AssignmentState.values()) {
                    
                    boolean expected = SUBMISSIONS[ROLE_INDEX.get(role)][STATE_INDEX.get(state)];
                    assertEquals(
                            expected,
                            AuthManager.roleCanSubmitToAssignmentState(state, role),
                            "role " + role + " trying to submit to " + state + " should return " + expected);
                }
            }
        }
        
        @Test
        public void roleCanReplayAssignmentState() {
            for (Role role : Role.values()) {
                for (AssignmentState  state : AssignmentState.values()) {
                    
                    boolean expected = REPLAY[ROLE_INDEX.get(role)][STATE_INDEX.get(state)];
                    assertEquals(
                            expected,
                            AuthManager.roleCanReplayAssignmentState(state, role),
                            "role " + role + " trying to replay " + state + " should return " + expected);
                }
            }
        }
        
        @Nested
        public class ParticipantCanAccessGroup {
            
            @Test
            public void studentInSingleAssignmentCanAccessOwnName() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.SINGLE, null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(student, assignment, "student1"));
            }
            
            @Test
            public void studentInSingleAssignmentCannotAccessOtherGroup() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.SINGLE, null, null);
                
                assertFalse(AuthManager.participantCanAccessGroup(student, assignment, "student2"));
            }
            
            @Test
            public void studentInGroupAssignmentCannotAccessOwnName() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP,
                        Map.of(), null);
                
                assertFalse(AuthManager.participantCanAccessGroup(student, assignment, "student1"));
            }
            
            @Test
            public void studentInGroupAssignmentCanAccessOwnGroup() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP,
                        Map.of("JP024", new Group(null, "JP024", Map.of("student1", student))), null);
                
                assertTrue(AuthManager.participantCanAccessGroup(student, assignment, "JP024"));
            }
            
            @Test
            public void studentInGroupAssignmentCannotAccessOtherGroup() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Participant otherStudent = new Participant(null, "student2", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP,
                        Map.of("JP024", new Group(null, "JP026", Map.of("student2", otherStudent))), null);
                
                assertFalse(AuthManager.participantCanAccessGroup(student, assignment, "JP026"));
            }
            
            @Test
            public void studentInGroupOrSingleAssignmentCanAccessOwnName() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP_OR_SINGLE,
                        Map.of(), null);
                
                assertTrue(AuthManager.participantCanAccessGroup(student, assignment, "student1"));
            }
            
            @Test
            public void studentInGroupOrSingleAssignmentCanAccessOwnGroup() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP_OR_SINGLE,
                        Map.of("JP024", new Group(null, "JP024", Map.of("student1", student))), null);
                
                assertTrue(AuthManager.participantCanAccessGroup(student, assignment, "JP024"));
            }
            
            @Test
            public void studentInGroupOrSingleAssignmentCannotAccessOtherGroup() {
                Participant student = new Participant(null, "student1", Role.STUDENT);
                Participant otherStudent = new Participant(null, "student2", Role.STUDENT);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP_OR_SINGLE,
                        Map.of("JP024", new Group(null, "JP026", Map.of("student2", otherStudent))), null);
                
                assertFalse(AuthManager.participantCanAccessGroup(student, assignment, "JP026"));
            }
            
            @Test
            public void tutorInSingleAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.TUTOR);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.SINGLE, null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
            @Test
            public void lecturerInSingleAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.LECTURER);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.SINGLE, null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
            @Test
            public void tutorInGroupAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.TUTOR);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP, null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
            @Test
            public void lecturerInGroupAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.LECTURER);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP, null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
            @Test
            public void tutorInGroupOrSingleAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.TUTOR);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP_OR_SINGLE,
                        null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
            @Test
            public void lecturerInGroupOrSingleAssignmentCanAccessOtherGroup() {
                Participant tutor = new Participant(null, "tutor1", Role.LECTURER);
                Assignment assignment = new Assignment(null, "Homework02", null, Collaboration.GROUP_OR_SINGLE,
                        null, null);
                
                assertTrue(AuthManager.participantCanAccessGroup(tutor, assignment, "student2"));
            }
            
        }
        
    }
    
    @Nested
    public class IsSubmissionAllowed {
        
        @Test
        public void notExistingCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.empty());
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void userNotInCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23", Map.of(), Map.of())));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentNotInCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of())));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentInInvisibleStateReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.INVISIBLE,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentInProgressStateReturnsTrue() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.IN_PROGRESS,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertTrue(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentEvaluatedStateReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.EVALUATED,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void wrongGroupReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.IN_PROGRESS,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isSubmissionAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "JP024"), "student1"));
        }
        
    }
    
    @Nested
    public class IsReplayAllowed {
        
        @Test
        public void notExistingCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.empty());
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void userNotInCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23", Map.of(), Map.of())));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentNotInCourseReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of())));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentInInvisibleStateReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.INVISIBLE,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void assignmentEvaluatedStateReturnsTrue() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.EVALUATED,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertTrue(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "student1"), "student1"));
        }
        
        @Test
        public void wrongGroupReturnsFalse() {
            StuMgmtView stuMgmt = mock(StuMgmtView.class);
            when(stuMgmt.getCourse("java-sose23")).thenReturn(Optional.of(
                    new Course("java-sose23",
                            Map.of("student1", new Participant("123", "student1", Role.STUDENT)),
                            Map.of("Homework02", new Assignment("321", "Homework02", AssignmentState.IN_PROGRESS,
                                    Collaboration.SINGLE, Map.of(), List.of())))));
            
            AuthManager auth = new AuthManager(stuMgmt);
            
            assertFalse(auth.isReplayAllowed(
                    new SubmissionTarget("java-sose23", "Homework02", "JP024"), "student1"));
        }
        
    }
    
}
