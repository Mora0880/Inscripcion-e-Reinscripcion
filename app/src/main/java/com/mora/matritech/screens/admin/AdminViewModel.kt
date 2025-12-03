package com.mora.matritech.screens.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -----------------------------------------------------------------
// DATA CLASSES - Modelos de datos
// -----------------------------------------------------------------

/**
 * Estadísticas del panel de administración
 */
data class AdminStats(
    val totalUsers: Int = 0,
    val students: Int = 0,
    val teachers: Int = 0,
    val admins: Int = 0,
    val activeUsers: Int = 0,
    val pendingApprovals: Int = 0
)

/**
 * Información de una notificación
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.INFO
)

enum class NotificationType {
    INFO, WARNING, ERROR, SUCCESS
}

/**
 * Información de una acción rápida
 */
data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
)

/**
 * Estado completo de la UI del Admin
 */
data class AdminUiState(
    val isDrawerOpen: Boolean = false,
    val selectedBottomItem: String = "home",
    val stats: AdminStats = AdminStats(),
    val notifications: List<Notification> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "Administrador",
    val userRole: String = "ADMINISTRADOR"
)

// -----------------------------------------------------------------
// VIEW MODEL - Lógica de negocio completa
// -----------------------------------------------------------------
class AdminViewModel : ViewModel() {

    private val _uiState = mutableStateOf(AdminUiState())
    val uiState: State<AdminUiState> = _uiState

    init {
        // Cargar datos iniciales
        loadInitialData()
    }

    // -----------------------------------------------------------------
    // Funciones de inicialización
    // -----------------------------------------------------------------

    /**
     * Carga todos los datos iniciales necesarios
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Simular carga de datos (aquí conectarías con tu API/Repository)
                delay(1000)

                loadStats()
                loadNotifications()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    // -----------------------------------------------------------------
    // Funciones para controlar el Drawer
    // -----------------------------------------------------------------

    fun openDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = true)
    }

    fun closeDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = false)
    }

    fun toggleDrawer() {
        _uiState.value = _uiState.value.copy(
            isDrawerOpen = !_uiState.value.isDrawerOpen
        )
    }

    // -----------------------------------------------------------------
    // Funciones para el Bottom Navigation Bar
    // -----------------------------------------------------------------

    fun onBottomItemSelected(item: String) {
        _uiState.value = _uiState.value.copy(selectedBottomItem = item)

        // Aquí puedes agregar lógica adicional según el item seleccionado
        when (item) {
            "home" -> loadStats()
            "users" -> loadUserManagement()
            "reports" -> loadReports()
        }
    }

    // -----------------------------------------------------------------
    // Funciones para estadísticas
    // -----------------------------------------------------------------

    /**
     * Carga las estadísticas del sistema
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                // Aquí conectarías con tu repositorio/API
                // Ejemplo con datos mock:
                delay(500)

                val stats = AdminStats(
                    totalUsers = 150,
                    students = 120,
                    teachers = 25,
                    admins = 5,
                    activeUsers = 142,
                    pendingApprovals = 8
                )

                _uiState.value = _uiState.value.copy(stats = stats)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar estadísticas: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualiza las estadísticas manualmente
     */
    fun updateStats(newStats: AdminStats) {
        _uiState.value = _uiState.value.copy(stats = newStats)
    }

    /**
     * Refresca todas las estadísticas
     */
    fun refreshStats() {
        loadStats()
    }

    // -----------------------------------------------------------------
    // Funciones para notificaciones
    // -----------------------------------------------------------------

    /**
     * Carga las notificaciones del administrador
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                // Ejemplo con datos mock
                delay(300)

                val notifications = listOf(
                    Notification(
                        id = "1",
                        title = "Nuevo usuario registrado",
                        message = "Juan Pérez se ha registrado en el sistema",
                        timestamp = System.currentTimeMillis() - 3600000,
                        type = NotificationType.INFO
                    ),
                    Notification(
                        id = "2",
                        title = "Aprobación pendiente",
                        message = "3 usuarios esperan aprobación",
                        timestamp = System.currentTimeMillis() - 7200000,
                        isRead = false,
                        type = NotificationType.WARNING
                    ),
                    Notification(
                        id = "3",
                        title = "Reporte generado",
                        message = "El reporte mensual está disponible",
                        timestamp = System.currentTimeMillis() - 86400000,
                        isRead = true,
                        type = NotificationType.SUCCESS
                    )
                )

                val unreadCount = notifications.count { !it.isRead }

                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    unreadNotifications = unreadCount
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar notificaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * Marca una notificación como leída
     */
    fun markNotificationAsRead(notificationId: String) {
        val updatedNotifications = _uiState.value.notifications.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        val unreadCount = updatedNotifications.count { !it.isRead }

        _uiState.value = _uiState.value.copy(
            notifications = updatedNotifications,
            unreadNotifications = unreadCount
        )
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    fun markAllNotificationsAsRead() {
        val updatedNotifications = _uiState.value.notifications.map {
            it.copy(isRead = true)
        }

        _uiState.value = _uiState.value.copy(
            notifications = updatedNotifications,
            unreadNotifications = 0
        )
    }

    // -----------------------------------------------------------------
    // Funciones para gestión de usuarios
    // -----------------------------------------------------------------

    private fun loadUserManagement() {
        // Aquí cargarías la lista de usuarios
        viewModelScope.launch {
            try {
                delay(500)
                // Cargar datos de usuarios
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar usuarios: ${e.message}"
                )
            }
        }
    }

    // -----------------------------------------------------------------
    // Funciones para reportes
    // -----------------------------------------------------------------

    private fun loadReports() {
        // Aquí cargarías los reportes
        viewModelScope.launch {
            try {
                delay(500)
                // Cargar datos de reportes
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar reportes: ${e.message}"
                )
            }
        }
    }

    // -----------------------------------------------------------------
    // Funciones para acciones rápidas
    // -----------------------------------------------------------------

    /**
     * Ejecuta una acción rápida según su ID
     */
    fun executeQuickAction(actionId: String) {
        when (actionId) {
            "add_user" -> handleAddUser()
            "export_data" -> handleExportData()
            "view_reports" -> handleViewReports()
            "settings" -> handleSettings()
        }
    }

    private fun handleAddUser() {
        // Lógica para agregar usuario
        viewModelScope.launch {
            // Implementar navegación o mostrar diálogo
        }
    }

    private fun handleExportData() {
        // Lógica para exportar datos
        viewModelScope.launch {
            try {
                delay(1000)
                // Simular exportación
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Datos exportados exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al exportar datos: ${e.message}"
                )
            }
        }
    }

    private fun handleViewReports() {
        onBottomItemSelected("reports")
    }

    private fun handleSettings() {
        // Navegar a configuración
    }

    // -----------------------------------------------------------------
    // Funciones de utilidad
    // -----------------------------------------------------------------

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Actualiza la información del usuario
     */
    fun updateUserInfo(name: String, role: String) {
        _uiState.value = _uiState.value.copy(
            userName = name,
            userRole = role
        )
    }

    /**
     * Refresca todos los datos
     */
    fun refreshAll() {
        loadInitialData()
    }

    // -----------------------------------------------------------------
    // Limpieza de recursos
    // -----------------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        // Aquí puedes limpiar recursos si es necesario
    }
}