package net.ssehub.teaching.exercise_submission.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Represents a version.
 * 
 * @author Adam
 */
@Schema(description = "A version of a submission")
public record VersionDto(
    @Schema(
        description = "The username of the author that created the submission",
        requiredMode = RequiredMode.REQUIRED,
        example = "student1")
    String author,
    
    @Schema(
        description = "The timestamp when the version was created, as seconds since unix epoch",
        requiredMode = RequiredMode.REQUIRED,
        example = "1635177322")
    long timestamp) {

}
