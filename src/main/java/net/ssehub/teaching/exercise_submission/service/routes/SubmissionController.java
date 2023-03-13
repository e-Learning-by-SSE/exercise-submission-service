package net.ssehub.teaching.exercise_submission.service.routes;

import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import net.ssehub.teaching.exercise_submission.service.storage.NoSuchTargetException;
import net.ssehub.teaching.exercise_submission.service.storage.StorageException;
import net.ssehub.teaching.exercise_submission.service.submission.Submission;
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
@SecurityRequirement(name = "oidc")
public class SubmissionController {
    
    private static final Log LOGGER = LogFactory.getLog(SubmissionController.class);
    
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
     * @throws NoSuchTargetException If the given target does not exist.
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
            
            throws NoSuchTargetException, StorageException, UnauthorizedException {
        
        String username = auth.getName();
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);
        
        LOGGER.info("Submission by " + username + " to " + target);
        
        if (!authManager.isSubmissionAllowed(target, username)) {
            throw new UnauthorizedException();
        }
        
        
        ResponseEntity<SubmissionResultDto> result;
        try {
            SubmissionBuilder submissionBuilder = new SubmissionBuilder(username);
            for (FileDto file : files) {
                submissionBuilder.addFile(Path.of(file.path()), Base64.getDecoder().decode(file.content()));
            }
            
            SubmissionResultDto resultDto = manager.submit(target, submissionBuilder.build());
            HttpStatus status;
            if (resultDto.accepted()) {
                status = HttpStatus.CREATED;
            } else {
                status = HttpStatus.OK;
            }
            
            result = new ResponseEntity<>(resultDto, status);
            
        } catch (IllegalArgumentException e) {
            LOGGER.info("Found relative file path in submission", e);
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
     * @throws NoSuchTargetException If the given target does not exist.
     * @throws StorageException If a storage exception occurs.
     * @throws UnauthorizedException If the user is not allowed to replay this target.
     */
    @Operation(
        description = "Retrieves a list of all submitted versions for the given assignment and group",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of versions in reverse-chronological order (i.e. latest version first)"
                        + " is returned"),
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
            
            throws NoSuchTargetException, StorageException, UnauthorizedException {
        
        String username = auth.getName();
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);
        
        LOGGER.info("Listing versions of " + target + " for user " + username);
        
        if (!authManager.isReplayAllowed(target, username)) {
            throw new UnauthorizedException();
        }
        
        List<Version> version = storage.getVersions(target);
        
        LOGGER.info("Returning list of " + version.size() + " versions");
        
        return version.stream()
                .map(v -> new VersionDto(v.author(), v.creationTime().getEpochSecond()))
                .toList();
    }
    
    /**
     * Route for retrieving a given version of a submission.
     * 
     * @param course The course where the submission is located.
     * @param assignment The assignment where the submission is located.
     * @param group The group where the submission is located.
     * @param timestamp The timestamp identifying the submission.
     * @param auth The authentication.
     * 
     * @return The list of files of the submission.
     * 
     * @throws NoSuchTargetException If the given target does not exist.
     * @throws StorageException If a storage excpetion occurs.
     * @throws UnauthorizedException If the user is not allowed to replay this target.
     */
    @Operation(
        description = "Retrieves the specified submission of the given assignment and group",
        responses = {
            @ApiResponse(responseCode = "200", description = "Submission is returned"),
            @ApiResponse(
                responseCode = "403",
                description = "User is not authorized to retrieve a submission",
                content = {@Content}),
            @ApiResponse(
                responseCode = "404",
                description = "Assignment or group does not exist, or the specified version does not exist",
                content = {@Content}),
            @ApiResponse(
                responseCode = "500",
                description = "An unexpected internal server error occurred",
                content = {@Content})
        }
    )
    @GetMapping("/{course}/{assignment}/{group}/{version}")
    public List<FileDto> getVersion(
            @PathVariable
            @Parameter(
                description = "ID of the course that contains the assignment",
                example = "java-sose23")
            String course,
            
            @PathVariable
            @Parameter(
                description = "Name of the assignment to retrieve from",
                example = "Homework02")
            String assignment,
            
            @PathVariable
            @Parameter(
                description = "Name of the group (or username for single assignments) to retrieve from",
                example = "JP024")
            String group,
            
            @PathVariable
            @Parameter(
                description = "Identifies the version as a unix timestamp (seconds since epoch)",
                example = "1635177322")
            long timestamp,
            
            Authentication auth)
    
            throws NoSuchTargetException, StorageException, UnauthorizedException {
        
        String username = auth.getName();
        SubmissionTarget target = new SubmissionTarget(course, assignment, group);

        LOGGER.info("Replaying version " + timestamp + " of " + target + " for user " + username);
        
        if (!authManager.isReplayAllowed(target, username)) {
            throw new UnauthorizedException();
        }
        
        List<Version> versions = storage.getVersions(target);
        Version match = null;
        for (Version version : versions) {
            if (version.creationTime().getEpochSecond() == timestamp) {
                match = version;
                break;
            }
        }
        
        if (match != null) {
            Submission submission = storage.getSubmission(target, match);
            
            List<FileDto> files = new LinkedList<>();
            for (Path filepath : submission.getFilepaths()) {
                files.add(new FileDto(
                        filepath.toString().replace('\\', '/'),
                        submission.getFileContent(filepath)));
            }
            
            LOGGER.info("Returning previous submission content with " + files.size() + " files");
            
            return files;
            
        } else {
            LOGGER.info("No version " + timestamp + " found for " + target);
            throw new NoSuchTargetException(target, timestamp);
        }
    }
    
}
