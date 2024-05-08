import java.util.*

data class UserEntity(var id: UUID = UUID.randomUUID(), var name: String, var email: String)
