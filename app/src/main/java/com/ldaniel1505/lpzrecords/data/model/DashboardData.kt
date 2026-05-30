import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    val total_usuarios: Int,
    val nuevos_hoy: Int
)