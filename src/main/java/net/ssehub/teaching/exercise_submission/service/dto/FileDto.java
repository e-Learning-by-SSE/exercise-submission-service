package net.ssehub.teaching.exercise_submission.service.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * A file in a submission.
 * 
 * @author Adam
 */
@Schema(description = "A single file in a submission")
public record FileDto(
    @Schema(
        description = "Relative path of this file in the submission directory",
        requiredMode = RequiredMode.REQUIRED,
        example = "dir/Main.java"
    )
    String path,
    
    @Schema(
        description = "Base64-encoded content of the file",
        requiredMode = RequiredMode.REQUIRED,
        example = "cGFja2FnZSBkaXI7CgpwdWJsaWMgY2xhc3MgTWFpbiB7CgogICAgcHVibGljIHN0YXRpYyB2b2lk"
                + "IG1haW4oU3RyaW5nW10gYXJncykgewogICAgICAgIFN5c3RlbS5vdXQucHJpbnRsbigiSGVsbG8g"
                + "V29ybGQhIik7CiAgICB9Cgp9Cg==")
    String content) {
    
    /**
     * Creates a {@link FileDto} with given path and content.
     * 
     * @param path The relative path of this file in the submission directory.
     * @param content The content of the file.
     */
    public FileDto(String path, byte[] content) {
        this(path, Base64.getEncoder().encodeToString(content));
    }
    
    /**
     * Convenience method to create an UTF-8 file from a string.
     * 
     * @param path The relative path of this file in the submission directory.
     * @param nonEncodedcontent The string content of the file.
     * 
     * @return The created {@link FileDto}.
     */
    public static FileDto fromStringContent(String path, String nonEncodedcontent) {
        return new FileDto(path, nonEncodedcontent.getBytes(StandardCharsets.UTF_8));
    }
    
}
