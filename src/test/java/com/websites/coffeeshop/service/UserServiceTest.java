package com.websites.coffeeshop.service;

import com.websites.coffeeshop.model.Role;
import com.websites.coffeeshop.model.User;
import com.websites.coffeeshop.repository.RoleRepository;
import com.websites.coffeeshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.sql.init.mode=never")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role roleUser;
    private Role roleVip;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser = roleRepository.save(roleUser);

        roleVip = new Role();
        roleVip.setName("ROLE_VIP");
        roleVip = roleRepository.save(roleVip);

        roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        roleAdmin = roleRepository.save(roleAdmin);
    }

    // ---- registerUser ----

    @Test
    void registerUserReturnsOkAndSavesUser() {
        ResponseEntity<String> response = userService.registerUser("newuser", "password123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.existsByUsername("newuser")).isTrue();
    }

    @Test
    void registerUserAssignsRoleUser() {
        userService.registerUser("newuser", "password123");

        User saved = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(saved.getRoles())
                .anyMatch(r -> r.getName().equals("ROLE_USER"));
    }

    @Test
    void registerUserReturnsBadRequestWhenUsernameAlreadyExists() {
        userService.registerUser("duplicate", "password123");

        ResponseEntity<String> second = userService.registerUser("duplicate", "password456");

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(second.getBody()).contains("Username already exists");
    }

    @Test
    void registerUserDoesNotSaveDuplicateUser() {
        userService.registerUser("duplicate", "password123");
        userService.registerUser("duplicate", "password456");

        assertThat(userRepository.findAll())
                .filteredOn(u -> "duplicate".equals(u.getUsername()))
                .hasSize(1);
    }

    // ---- updateUserRole: adding VIP ----

    @Test
    void updateUserRoleAddsVipToExistingRoles() {
        User user = createUserWithRole(roleUser);

        userService.updateUserRole(user.getId(), "ROLE_VIP");

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoles())
                .anyMatch(r -> r.getName().equals("ROLE_VIP"))
                .anyMatch(r -> r.getName().equals("ROLE_USER"));
    }

    @Test
    void updateUserRoleVipDoesNotRemoveOtherRoles() {
        User user = createUserWithRole(roleUser);

        userService.updateUserRole(user.getId(), "ROLE_VIP");

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoles()).anyMatch(r -> r.getName().equals("ROLE_USER"));
    }

    // ---- updateUserRole: stripping VIP ----

    @Test
    void updateUserRoleStripsVipWhenNonVipRoleIsSet() {
        User user = createUserWithRoles(roleUser, roleVip);

        userService.updateUserRole(user.getId(), "ROLE_ADMIN");

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoles())
                .noneMatch(r -> r.getName().equals("ROLE_VIP"));
    }

    @Test
    void updateUserRoleDoesNotAddVipWhenNonVipRoleIsSet() {
        User user = createUserWithRole(roleUser);

        userService.updateUserRole(user.getId(), "ROLE_ADMIN");

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRoles())
                .noneMatch(r -> r.getName().equals("ROLE_VIP"));
    }

    // ---- helpers ----

    private User createUserWithRole(Role role) {
        User user = new User();
        user.setUsername("testuser_" + System.nanoTime());
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(role)));
        return userRepository.save(user);
    }

    private User createUserWithRoles(Role... roles) {
        User user = new User();
        user.setUsername("testuser_" + System.nanoTime());
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(roles)));
        return userRepository.save(user);
    }
}
