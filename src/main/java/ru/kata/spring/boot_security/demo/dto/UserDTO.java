package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя обязательно для заполнения")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s\\-]+$",
            message = "Имя может содержать только буквы, пробелы и дефисы")
    @Length(min = 2, max = 50,
            message = "Имя должно содержать от 2 до 50 символов")
    private String name;

    @NotBlank(message = "Фамилия обязательна для заполнения")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s\\-]+$",
            message = "Фамилия может содержать только буквы, пробелы и дефисы")
    @Length(min = 2, max = 50,
            message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastname;

    @NotNull(message = "Количество полных лет обязательно")
    @Min(value = 18, message = "Полных лет должно быть не менее 18")
    @Max(value = 100, message = "Полных лет должно быть не более 100")
    private Integer age;

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @NotBlank(message = "Email обязательно")
    @Email(message = "Email не валиден")
    private String email;

    // Пароль не обязателен при обновлении, но обязателен при создании
    private String password;

    @NotEmpty(message = "Должна быть выбрана хотя бы одна роль")
    private Set<String> roles;

    // Флаг для определения типа операции (можно использовать для валидации)
    private boolean isNew = true;

    // Конструкторы
    public UserDTO() {
    }

    public UserDTO(Long id, String name, String lastname, Integer age,
                   String username, String email, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.age = age;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isNew = (id == null);
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.isNew = (id == null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    // Валидационные методы
    @AssertTrue(message = "Пароль обязателен для нового пользователя")
    public boolean isPasswordValid() {
        if (isNew) {
            return password != null && !password.trim().isEmpty();
        }
        return true; // Для существующих пользователей пароль не обязателен
    }

    @AssertTrue(message = "ID должен быть null для нового пользователя")
    public boolean isIdValidForNewUser() {
        if (isNew) {
            return id == null;
        }
        return id != null; // Для обновления ID должен быть указан
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", age=" + age +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", isNew=" + isNew +
                '}';
    }
}