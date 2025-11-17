class UserApp {
    constructor() {
        this.baseUrl = '/api';
        this.csrfToken = this.getCsrfToken();
        this.init();
    }

    getCsrfToken() {
        const csrfInput = document.querySelector('input[name="_csrf"]');
        return csrfInput ? csrfInput.value : '';
    }

    async init() {
        await this.loadRoles();

        if (window.location.pathname === '/admin/users') {
            await this.loadUsers();
            this.setupAdminEvents();
            this.setupModalHandlers(); // Добавляем обработчики модальных окон
        } else if (window.location.pathname === '/user') {
            await this.loadUserInfo();
        }

        this.formatRolesDisplay();
    }

    // Настройка обработчиков модальных окон
    setupModalHandlers() {
        // Обработчик для модального окна удаления
        const deleteModal = document.getElementById('deleteModal');
        deleteModal.addEventListener('show.bs.modal', (event) => {
            const button = event.relatedTarget;
            this.fillDeleteModal(button);
        });

        // Форма удаления пользователя
        document.getElementById('deleteUserForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.deleteUserFromModal();
        });
    }

    // Заполнение модального окна удаления
    fillDeleteModal(button) {
        const userId = button.getAttribute('data-user-id');
        const userName = button.getAttribute('data-user-name');
        const userLastname = button.getAttribute('data-user-lastname');
        const userUsername = button.getAttribute('data-user-username');
        const userAge = button.getAttribute('data-user-age');
        const userEmail = button.getAttribute('data-user-email');

        // Получаем роли из строки таблицы
        const tableRow = button.closest('tr');
        const rolesCell = tableRow.querySelector('td:nth-child(7)');
        const roleBadges = rolesCell.querySelectorAll('.badge');
        const userRoles = Array.from(roleBadges).map(badge => badge.textContent.trim());

        // Заполняем данные
        document.getElementById('deleteUserId').value = userId;
        document.getElementById('deleteUserDisplayId').textContent = userId;
        document.getElementById('deleteUserName').textContent = userName;
        document.getElementById('deleteUserLastname').textContent = userLastname;
        document.getElementById('deleteUserUsername').textContent = userUsername;
        document.getElementById('deleteUserAge').textContent = userAge;
        document.getElementById('deleteUserEmail').textContent = userEmail;

        // Заполняем роли как текст в div (имитируем select)
        const rolesContainer = document.getElementById('deleteUserRoles');
        const rolesText = userRoles.join(', ');
        rolesContainer.textContent = rolesText;
    }

    // Удаление пользователя из модального окна
    async deleteUserFromModal() {
        const userId = document.getElementById('deleteUserId').value;

        try {
            const response = await fetch(`${this.baseUrl}/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': this.csrfToken
                }
            });

            if (response.ok) {
                this.showAlert('User deleted successfully!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                await this.loadUsers();
            } else {
                const result = await response.json();
                this.showAlert(result.message || 'Error deleting user', 'danger');
            }
        } catch (error) {
            this.showAlert('Error deleting user: ' + error.message, 'danger');
        }
    }

    // Остальные методы остаются без изменений
    async loadUsers() {
        try {
            const response = await fetch(`${this.baseUrl}/users`);
            if (response.ok) {
                const users = await response.json();
                this.renderUsersTable(users);
            }
        } catch (error) {
            console.error('Error loading users:', error);
            this.showAlert('Ошибка загрузки пользователей', 'danger');
        }
    }

    // Рендер таблицы пользователей (обновляем для работы с модальным окном)
    renderUsersTable(users) {
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = users.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.lastname}</td>
                <td>${user.username}</td>
                <td>${user.age}</td>
                <td>${user.email}</td>
                <td>
                    ${user.roles.map(role =>
            `<span class="badge ${role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'}">${role.replace('ROLE_', '')}</span>`
        ).join(' ')}
                </td>
                <td>
                    <button type="button" class="action-btn delete-btn user-delete-btn"
                            data-bs-toggle="modal"
                            data-bs-target="#deleteModal"
                            data-user-id="${user.id}"
                            data-user-name="${user.name}"
                            data-user-lastname="${user.lastname}"
                            data-user-username="${user.username}"
                            data-user-age="${user.age}"
                            data-user-email="${user.email}">
                        Delete
                    </button>
                    <button class="action-btn edit-btn" onclick="app.editUser(${user.id})">Edit</button>
                </td>
            </tr>
        `).join('');
    }

    // УДАЛЯЕМ старый метод deleteUser (он больше не нужен)
    // async deleteUser(userId) { ... }

    // Остальные методы без изменений
    async loadUserInfo() {
        try {
            const response = await fetch(`${this.baseUrl}/auth/current`);
            if (response.ok) {
                const user = await response.json();
                this.renderUserInfo(user);
            }
        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }

    async loadRoles() {
        try {
            const response = await fetch(`${this.baseUrl}/roles`);
            if (response.ok) {
                this.roles = await response.json();
                this.renderRoleOptions();
            }
        } catch (error) {
            console.error('Error loading roles:', error);
        }
    }

    renderUserInfo(user) {
        const tbody = document.getElementById('userInfoBody');
        tbody.innerHTML = `
            <tr>
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.lastname}</td>
                <td>${user.username}</td>
                <td>${user.age}</td>
                <td>${user.email}</td>
                <td>
                    ${user.roles.map(role =>
            `<span class="badge ${role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'}">${role.replace('ROLE_', '')}</span>`
        ).join(' ')}
                </td>
            </tr>
        `;
    }

    renderRoleOptions() {
        const rolesSelect = document.getElementById('roles');
        const editRolesSelect = document.getElementById('editRoles');

        if (rolesSelect) {
            rolesSelect.innerHTML = this.roles.map(role =>
                `<option value="${role}">${role.replace('ROLE_', '')}</option>`
            ).join('');
        }

        if (editRolesSelect) {
            editRolesSelect.innerHTML = this.roles.map(role =>
                `<option value="${role}">${role.replace('ROLE_', '')}</option>`
            ).join('');
        }
    }

    setupAdminEvents() {
        document.getElementById('usersTableBtn').addEventListener('click', () => this.showUsersTable());
        document.getElementById('newUserBtn').addEventListener('click', () => this.showNewUserForm());

        document.getElementById('addUserForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.addUser(new FormData(e.target));
        });

        document.getElementById('editUserForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.updateUser(new FormData(e.target));
        });
    }

    showUsersTable() {
        document.getElementById('usersTable').classList.remove('d-none');
        document.getElementById('newUserForm').classList.add('d-none');
        document.getElementById('usersTableBtn').classList.add('active');
        document.getElementById('newUserBtn').classList.remove('active');
    }

    showNewUserForm() {
        document.getElementById('usersTable').classList.add('d-none');
        document.getElementById('newUserForm').classList.remove('d-none');
        document.getElementById('usersTableBtn').classList.remove('active');
        document.getElementById('newUserBtn').classList.add('active');
        document.getElementById('addUserForm').reset();
    }

    async addUser(formData) {
        const userData = {
            name: formData.get('name'),
            lastname: formData.get('lastname'),
            username: formData.get('username'),
            age: parseInt(formData.get('age')),
            email: formData.get('email'),
            password: formData.get('password'),
            roles: formData.getAll('roles')
        };

        try {
            const response = await fetch(`${this.baseUrl}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': this.csrfToken
                },
                body: JSON.stringify(userData)
            });

            const result = await response.json();

            if (response.ok) {
                this.showAlert('User created successfully!', 'success');
                this.showUsersTable();
                await this.loadUsers();
            } else {
                this.showAlert(result.message || 'Error creating user', 'danger');
            }
        } catch (error) {
            this.showAlert('Error creating user: ' + error.message, 'danger');
        }
    }

    async editUser(userId) {
        try {
            const response = await fetch(`${this.baseUrl}/users/${userId}`);
            if (response.ok) {
                const user = await response.json();
                this.fillEditForm(user);
                new bootstrap.Modal(document.getElementById('editModal')).show();
            }
        } catch (error) {
            this.showAlert('Error loading user: ' + error.message, 'danger');
        }
    }

    fillEditForm(user) {
        document.getElementById('editUserId').value = user.id;
        document.getElementById('editUserDisplayId').textContent = user.id;
        document.getElementById('editName').value = user.name;
        document.getElementById('editLastname').value = user.lastname;
        document.getElementById('editUsername').value = user.username;
        document.getElementById('editAge').value = user.age;
        document.getElementById('editEmail').value = user.email;
        document.getElementById('editPassword').value = '';

        const rolesSelect = document.getElementById('editRoles');
        Array.from(rolesSelect.options).forEach(option => {
            option.selected = user.roles.includes(option.value);
        });
    }

    async updateUser(formData) {
        const userId = formData.get('id');
        const userData = {
            name: formData.get('name'),
            lastname: formData.get('lastname'),
            username: formData.get('username'),
            age: parseInt(formData.get('age')),
            email: formData.get('email'),
            password: formData.get('password'),
            roles: formData.getAll('roles')
        };

        if (!userData.password) {
            delete userData.password;
        }

        try {
            const response = await fetch(`${this.baseUrl}/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': this.csrfToken
                },
                body: JSON.stringify(userData)
            });

            const result = await response.json();

            if (response.ok) {
                this.showAlert('User updated successfully!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
                await this.loadUsers();
            } else {
                this.showAlert(result.message || 'Error updating user', 'danger');
            }
        } catch (error) {
            this.showAlert('Error updating user: ' + error.message, 'danger');
        }
    }

    formatRolesDisplay() {
        const rolesElement = document.getElementById('roles-display');
        if (rolesElement) {
            const rolesText = rolesElement.textContent;
            rolesElement.textContent = rolesText
                .replace(/ROLE_/g, '')
                .replace(/[\[\]]/g, '')
                .replace(/,/g, ' ');
        }
    }

    showAlert(message, type) {
        const existingAlerts = document.querySelectorAll('.alert-position-fixed');
        existingAlerts.forEach(alert => alert.remove());

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show alert-position-fixed`;
        alertDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999;';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(alertDiv);

        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

// Инициализация приложения
const app = new UserApp();