package net.ssehub.teaching.exercise_submission.service.routes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that the user is not authorized to perform the operation.
 * 
 * @author Adam
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends Exception {

    private static final long serialVersionUID = 5183877547987814320L;

    /**
     * Creates this exception.
     */
    public UnauthorizedException() {
        super();
    }

}
