package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            initializeRoles(user);
        }
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public User getById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        initializeRoles(user);
        return user;
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException(
                    "Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException(
                    "Email already exists: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }


    @Transactional
    @Override
    public void updateUser(User updatedUser) {
        User existingUser = getById(updatedUser.getId());

        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException(
                    "Username already exists: " + updatedUser.getUsername());
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException(
                    "Email already exists: " + updatedUser.getEmail());
        }

        // ОБНОВЛЯЕМ ВСЕ ПОЛЯ
        existingUser.setName(updatedUser.getName());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRoles(updatedUser.getRoles());

        // ПАРОЛЬ ОБНОВЛЯЕМ ТОЛЬКО ЕСЛИ ПЕРЕДАН НОВЫЙ
        String newPassword = updatedUser.getPassword();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        // ЕСЛИ ПАРОЛЬ ПУСТОЙ - ОСТАВЛЯЕМ СТАРЫЙ

        userRepository.save(existingUser);
    }





    @Transactional
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException(
                    "User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Role findRoleByName(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = roleRepository.findByName("ROLE_" + name);
        }
        if (role == null) {
            throw new RuntimeException("Role not found: " + name);
        }
        return role;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            initializeRoles(user);
        }
        return user;
    }
    @Transactional(readOnly = true)
    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            initializeRoles(user);
        }
        return user;
    }



    private void initializeRoles(User user) {
        if (user.getRoles() != null) {
            user.getRoles().size();
        }
    }
    @Transactional(readOnly = true)
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}