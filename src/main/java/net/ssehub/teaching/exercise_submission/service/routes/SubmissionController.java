package net.ssehub.teaching.exercise_submission.service.routes;

import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.ssehub.teaching.exercise_submission.service.auth.AuthManager;
import net.ssehub.teaching.exercise_submission.service.dto.FileDto;
import net.ssehub.teaching.exercise_submission.service.dto.SubmissionResultDto;
import net.ssehub.teaching.exercise_submission.service.dto.VersionDto;
import net.ssehub.teaching.exercise_submission.service.storage.ISubmissionStorage;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionBuilder;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionManager;
import net.ssehub.teaching.exercise_submission.service.submission.SubmissionTarget;
import net.ssehub.teaching.exercise_submission.service.submission.Version;

/**
 * The controller for the /submission route. Used to post new submissions and get version history and retrieve previous
 * submissions. 
 * 
 * @author Adam
 */
@RestController
@RequestMapping(
    path = "/submission",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "submission")
@SecurityRequirement(name = "oauth2")
public class SubmissionController {
    
    private SubmissionManager manager;
    
    private ISubmissionStorage storage;
    
    private AuthManager authManager;
    
    /**
     * Creates this controller.
     * 
     * @param manager The manager that orchestrates the submissions (checks, storage, etc.).
     * @param storage The storage where submissions are placed (should be the same as used by the manager).
     * @param authManager Checks that users a authorized to do the given operations.
     */
    public SubmissionController(SubmissionManager manager, ISubmissionStorage storage, AuthManager authManager) {
        this.manager = manager;
        this.storage = storage;
        this.authManager = authManager;
    }

    /**
     * Route for adding a new submission.
     * 
     * @param course The course to add the submission for.
     * @param assignment The assignment to add the submission for.
     * @param group The group to add the submission for.
     * @param files The files of the submission.
     * @param auth The authentication.
     * 
     * @return The result of the submission.
     * 
     * @throws StorageException If a storage exception occurs.
     * @throws UnauthorizedException If the user is not allowed to submit a new version to this target.
     */
    @Operation(
        description = "Adds a new submission for the given assignment and group",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Submission accepted",
                content = {
                    @Content(
                        schema = @Schema(implementation = SubmissionResultDto.class),
                        examples = {
                            @ExampleObject(value = "{\"accepted\": true, \"messages\": []}")
                        })
                }),
            @ApiResponse(
                responseCode = "200",
                description = "Submission rejected based on submission checks",
                content = {
                    @Content(
                        schema = @Schema(implementation = SubmissionResultDto.class),
                        examples = {
                            @ExampleObject(value = "{\"accepted\": false, \"messages\": []}")
                        })
                }),
            @ApiResponse(
                responseCode = "400",
                description = "Input data malformed or invalid",
                content = {@Content}),
            @ApiResponse(
                responseCode = "403",
                description = "User is not authorized to add a new submission",
                content = {@Content}),
            @ApiResponse(
                responseCode = "404",
                description = "Assignment or group does not exist",
                content = {@Content}),
            @ApiResponse(
                responseCode = "500",
                description = "An unexpected internal server error occurred",
                content = {@Content})
        }
    )
    @PostMapping("/{course}/{assignment}/{group}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SubmissionResultDto> submit(
            @PathVariable
            @Parameter(
                description = "ID of the course that contains the assignment",
                example = "java-sose23")
            String course,
            
            @PathVariable
            @Parameter(
                description = "Name of the assignment to submit to",
                example = "Homework02")
            String assignment,
            
            @PathVariable
            @Parameter(
                description = "Name of the group (or username for single assignments) to submit to",
                example = "JP024")
            String group,
            
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The files of this submission")
            List<FileDto> files,
            
            Authentication auth)
            
            throws StorageException, UnauthorizedException {
        
        String username = auth.getName();
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);
        
        if (!authManager.isSubmissionAllowed(target, username)) {
            throw new UnauthorizedException();
        }
        
        
        ResponseEntity<SubmissionResultDto> result;
        try {
            SubmissionBuilder submissionBuilder = new SubmissionBuilder(username);
            for (FileDto file : files) {
                submissionBuilder.addFile(Path.of(file.getPath()), Base64.getDecoder().decode(file.getContent()));
            }
            
            SubmissionResultDto resultDto = manager.submit(target, submissionBuilder.build());
            HttpStatus status;
            if (resultDto.getAccepted()) {
                status = HttpStatus.CREATED;
            } else {
                status = HttpStatus.OK;
            }
            
            result = new ResponseEntity<>(resultDto, status);
            
        } catch (IllegalArgumentException e) {
            result = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        return result;
    }
    
    /**
     * Route for retrieving the list of submitted versions.
     * 
     * @param course The course where the submission is located.
     * @param assignment The assignment where the submission is located.
     * @param group The group where the submission is located.
     * @param auth The authentication.
     * 
     * @return The list of versions for that submission.
     * 
     * @throws StorageException If a storage exception occurs.
     * @throws UnauthorizedException If the user is not allowed to replay this target.
     */
    @Operation(
        description = "Retrieves a list of all submitted versions for the given assignment and group",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of versions in reverse-chronological order (i.e. latest version first) is returned",
                content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = VersionDto.class)))
                }),
            @ApiResponse(
                responseCode = "403",
                description = "User is not authorized to get the version list",
                content = {@Content}),
            @ApiResponse(
                responseCode = "404",
                description = "Assignment or group does not exist",
                content = {@Content}),
            @ApiResponse(
                responseCode = "500",
                description = "An unexpected internal server error occurred",
                content = {@Content})
        }
    )
    @GetMapping("/{course}/{assignment}/{group}/versions")
    @PreAuthorize("permitAll()")
    public List<VersionDto> listVersions(
            @PathVariable
            @Parameter(
                description = "ID of the course that contains the assignment",
                example = "java-sose23")
            String course,
            
            @PathVariable
            @Parameter(
                description = "Name of the assignment to get versions for",
                example = "Homework02")
            String assignment,
            
            @PathVariable
            @Parameter(
                description = "Name of the group (or username for single assignments) to get versions for",
                example = "JP024")
            String group,
            
            Authentication auth)
            
            throws StorageException, UnauthorizedException {
        
        String username = auth.getName();
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);
        
        if (!authManager.isReplayAllowed(target, username)) {
            throw new UnauthorizedException();
        }
        
        List<Version> version = storage.getVersions(target);
        
        return version.stream()
                .map(v -> {
                    VersionDto dto = new VersionDto();
                    dto.setAuthor(v.author());
                    dto.setTimestamp(v.creationTime().getEpochSecond());
                    return dto;
                })
                .toList();
    }
    
}
