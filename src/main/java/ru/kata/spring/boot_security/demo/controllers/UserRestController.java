package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // Получить всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    // Получить пользователя по ID
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            UserDTO userDTO = convertToDTO(user);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return createErrorResponse("Пользователь не найден: " + id, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/auth/current")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserDTO userDTO = convertToDTO(user);
        userDTO.setPassword(null);
        return ResponseEntity.ok(userDTO);
    }

    // Создать нового пользователя
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return createValidationErrorResponse(bindingResult);
        }

        try {
            userDTO.setNew(true); // Устанавливаем флаг нового пользователя
            User user = convertToEntity(userDTO);
            userService.saveUser(user);
            UserDTO createdUserDTO = convertToDTO(user);
            createdUserDTO.setPassword(null); // Не возвращаем пароль
            return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }






    // Обновить пользователя
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UserDTO userDTO,
                                        BindingResult bindingResult) {

        // ВРУЧНУЮ УСТАНАВЛИВАЕМ ФЛАГ isNew = false для обновления
        userDTO.setId(id);
        userDTO.setNew(false);

        // ФИЛЬТРУЕМ ОШИБКИ - ИГНОРИРУЕМ ОШИБКИ ПАРОЛЯ ДЛЯ СУЩЕСТВУЮЩИХ ПОЛЬЗОВАТЕЛЕЙ
        if (bindingResult.hasErrors()) {
            List<FieldError> filteredErrors = bindingResult.getFieldErrors().stream()
                    .filter(error -> {
                        // Игнорируем ошибки passwordValid для существующих пользователей
                        if ("passwordValid".equals(error.getField())) {
                            return false; // ИГНОРИРУЕМ эту ошибку
                        }
                        return true; // Оставляем все остальные ошибки
                    })
                    .collect(Collectors.toList());

            if (!filteredErrors.isEmpty()) {
                // Создаем новый BindingResult с отфильтрованными ошибками
                BeanPropertyBindingResult filteredBindingResult =
                        new BeanPropertyBindingResult(userDTO, "user");
                for (FieldError error : filteredErrors) {
                    filteredBindingResult.addError(error);
                }
                return createValidationErrorResponse(filteredBindingResult);
            }
        }

        try {
            User user = convertToEntity(userDTO);

            // ЕСЛИ ПАРОЛЬ ПУСТОЙ - БЕРЕМ СТАРЫЙ ПАРОЛЬ
            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                User existingUser = userService.getById(id);
                user.setPassword(existingUser.getPassword());
            }

            userService.updateUser(user);
            UserDTO updatedUserDTO = convertToDTO(user);
            updatedUserDTO.setPassword(null);
            return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }






    // Удалить пользователя
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return createSuccessResponse("Пользователь успешно удален");
        } catch (RuntimeException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Получить все роли (для фронтенда)
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = userService.getAllRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toList());
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    // Вспомогательные методы для конвертации
    private UserDTO convertToDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getAge(),
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
        userDTO.setNew(false); // Существующий пользователь
        return userDTO;
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setLastname(userDTO.getLastname());
        user.setAge(userDTO.getAge());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());

        // Пароль устанавливаем только если он предоставлен
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(userDTO.getPassword());
        }

        // Конвертируем роли
        Set<Role> roles = userDTO.getRoles().stream()
                .map(roleName -> {
                    String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                    return userService.findRoleByName(fullRoleName);
                })
                .collect(Collectors.toSet());
        user.setRoles(roles);

        return user;
    }

    // Вспомогательные методы для ответов
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Ошибки валидации");

        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}