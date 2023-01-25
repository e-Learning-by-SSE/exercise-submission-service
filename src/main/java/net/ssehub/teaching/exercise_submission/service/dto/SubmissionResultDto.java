package net.ssehub.teaching.exercise_submission.service.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Represents the result of a submission.
 * 
 * @author Adam
 */
@Schema(description = "The result of a submission")
public record SubmissionResultDto(
    @Schema(
        description = "Whether the submission was accepted or rejected by automatic checks",
        requiredMode = RequiredMode.REQUIRED)
    boolean accepted,
    
    @Schema(
        description = "Messages created by automatic checks on the submission",
        requiredMode = RequiredMode.REQUIRED)
    List<CheckMessageDto> messages) {
    
}
