package org.una.programmingIII.UTEMP_Project.services.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificationNumber) throws UsernameNotFoundException {
        User user = userRepository.findByIdentificationNumber(identificationNumber);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with identification number: " + identificationNumber);
        }
        return new CustomUserDetails(user);
    }
}