package github.projects.authentication.utils;

import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.repositories.UserRepositoryI;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepositoryI userRepository;
    public CustomUserDetailsService(UserRepositoryI userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try{
            UserData userByUsername = userRepository.getUserByUsername(username);
            User user = new User(
                    userByUsername.getUsername(),
                    userByUsername.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority(userByUsername.getRole()))
            );
            return user;
        }catch(Exception e){
            throw new UsernameNotFoundException("The User With This Details is not found",e);
        }
    }
}
