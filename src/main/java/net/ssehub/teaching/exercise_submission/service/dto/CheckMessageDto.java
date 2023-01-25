package net.ssehub.teaching.exercise_submission.service.dto;

import java.nio.file.Path;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import net.ssehub.teaching.exercise_submission.service.submission.checks.Check;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage;
import net.ssehub.teaching.exercise_submission.service.submission.checks.ResultMessage.MessageType;

/**
 * A message from a {@link Check}.
 * 
 * @author Adam
 */
@Schema(description = "A message created by an automatic check on a submission")
public record CheckMessageDto(
    @Schema(
        description = "The name of the automatic check",
        requiredMode = RequiredMode.REQUIRED,
        example = "javac")
    String checkName,
    
    @Schema(
        description = "Whether this message is an error or a warning",
        requiredMode = RequiredMode.REQUIRED,
        example = "ERROR")
    MessageType type,
    
    @Schema(
        description = "The message created by the check",
        requiredMode = RequiredMode.REQUIRED,
        example = "';' expected")
    String message,
    
    @Schema(
        description = "The relative path of the file this message is about",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "dir/Main.java")
    String file,
    
    @Schema(
        description = "The line number in the file that this message is about",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "4")
    Integer line,
    
    @Schema(
        description = "The column of the line in the file that this message is about",
        requiredMode = RequiredMode.NOT_REQUIRED,
        example = "43")
    Integer column) {
    
    /**
     * Creates this instance by copying from the given {@link ResultMessage}.
     * 
     * @param message The message to copy from.
     */
    public CheckMessageDto(ResultMessage message) {
        this(message.getCheckName(), message.getType(), message.getMessage(),
                pathToString(message.getFile()), message.getLine(), message.getColumn());
    }
    
    /**
     * Creates a new message with a simple message.
     * <p>
     * Convenience method for test cases.
     *  
     * @param checkName The name of the {@link Check} that created this message.
     * @param type The type of this message.
     * @param message The message describing the result of the check.
     */
    public CheckMessageDto(String checkName, ResultMessage.MessageType type, String message) {
        this(checkName, type, message, null, null, null);
    }
    
    /**
     * Converts a path to a string, replacing any \ with /. Helper method for {@link #CheckMessageDto(ResultMessage)}.
     * 
     * @param path The path to convert. May be <code>null</code>.
     * 
     * @return The path as string. If path is <code>null</code>, this is also <code>null</code>.
     */
    private static String pathToString(Path path) {
        String str = null;
        if (path != null) {
            str = path.toString().replace('\\', '/');
        }
        return str;
    }
    
}
