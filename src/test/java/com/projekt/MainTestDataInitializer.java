package com.projekt;

import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.repositories.RoleRepository;
import com.projekt.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("test")
public class MainTestDataInitializer {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public MainTestDataInitializer(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Bean
    CommandLineRunner init() {
        return args -> {
            if (userRepository.findAll().isEmpty() && roleRepository.findAll().isEmpty()) {
                initializeTestData();
            }
        };
    }

    private void initializeTestData() {
        Role roleUser = roleRepository.save(new Role(Role.Types.ROLE_USER));
        Role roleOperator = roleRepository.save(new Role(Role.Types.ROLE_OPERATOR));
        Role roleAdmin = roleRepository.save(new Role(Role.Types.ROLE_ADMIN));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        User user = new User("user", true);
        user.setRole(roleUser);
        user.setEmail("example1@gmail.com");
        user.setName("Jan");
        user.setSurname("Kowalski");
        user.setPassword(passwordEncoder.encode("user"));

        User operator = new User("operator", true);
        operator.setRole(roleOperator);
        operator.setEmail("example2@gmail.com");
        operator.setName("Adam");
        operator.setSurname("Nowak");
        operator.setPassword(passwordEncoder.encode("operator"));

        User admin = new User("admin", true);
        admin.setRole(roleAdmin);
        admin.setEmail("example3@gmail.com");
        admin.setName("Piotr");
        admin.setSurname("Kowalski");
        admin.setPassword(passwordEncoder.encode("admin"));

        userRepository.save(user);
        userRepository.save(operator);
        userRepository.save(admin);
    }
}
