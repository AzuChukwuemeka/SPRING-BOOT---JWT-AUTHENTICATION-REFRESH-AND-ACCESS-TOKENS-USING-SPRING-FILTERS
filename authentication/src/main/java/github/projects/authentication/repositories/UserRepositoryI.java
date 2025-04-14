package github.projects.authentication.repositories;

import github.projects.authentication.dataClasses.UserData;

import java.util.UUID;

public interface UserRepositoryI {

    UserData createUser(UserData userData);
    UserData getUserById(UUID id);
    UserData getUserByUsername(String username);
}
