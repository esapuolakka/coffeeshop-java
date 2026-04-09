package com.websites.coffeeshop.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.websites.coffeeshop.model.Role;
import com.websites.coffeeshop.model.User;
import com.websites.coffeeshop.repository.RoleRepository;
import com.websites.coffeeshop.repository.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.username:admin}")
  private String adminUsername;

  @Value("${app.admin.password:admin123}")
  private String adminPassword;

  public AdminUserInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (userRepository.existsByUsername(adminUsername)) {
      return;
    }

    Role adminRole = roleRepository.findByName("ROLE_ADMIN")
        .orElseGet(() -> {
          Role role = new Role();
          role.setName("ROLE_ADMIN");
          return roleRepository.save(role);
        });

    User adminUser = new User();
    adminUser.setUsername(adminUsername);
    adminUser.setPassword(passwordEncoder.encode(adminPassword));

    Set<Role> roles = new HashSet<>();
    roles.add(adminRole);
    adminUser.setRoles(roles);

    userRepository.save(adminUser);
  }
}
