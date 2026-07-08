package github.projects.authentication.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("No user found with id " + id);
    }
}
