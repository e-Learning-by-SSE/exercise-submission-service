package net.ssehub.teaching.exercise_submission.service.submission;

import java.time.Instant;

/**
 * Pointer to a specific submitted version. Contains the timestamp and author name.
 * 
 * @author Adam
 */
public record Version(String author, Instant creationTime) {

}
