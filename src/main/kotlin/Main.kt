import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

interface UserDAO {
    fun create(user: UserEntity): UserEntity?
    fun getAll(): List<UserEntity>
    fun getById(id: UUID): UserEntity?
    fun update(user: UserEntity):UserEntity?
    fun delete(id: UUID): Boolean
}


class UserDAOH2(private val dataSource: DataSource, private val console: Console) : UserDAO {

    override fun create(user: UserEntity): UserEntity? {
        val sql = "INSERT INTO tuser (id, name, email) VALUES (?, ?, ?)"
        return try {

            dataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, user.id.toString())
                    stmt.setString(2, user.name)
                    stmt.setString(3, user.email)
                    val rs = stmt.executeUpdate()
                    if (rs == 1){
                        user
                    }
                    else{
                        console.showMessage("***Error*** INSERT Query failed. e: $rs.")
                        null
                    }
                }
            }
        }catch (e: SQLException){
            console.showMessage("***Error*** INSERT Query failed.")
            null
        }
    }

    override fun getById(id: UUID): UserEntity? {
        val sql = "SELECT * FROM tuser WHERE id = ?"
        return try {

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id.toString())
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    UserEntity(
                        id = UUID.fromString(rs.getString("id")),
                        name = rs.getString("name"),
                        email = rs.getString("email")
                    )
                } else {
                    console.showMessage("***Error*** Query failed.")
                    null
                }
            }
        }
        }catch (e: SQLException){
            console.showMessage("***Error*** Query failed.")
            null
        }
    }

    override fun getAll(): List<UserEntity> {
        val sql = "SELECT * FROM tuser"
        return dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                val rs = stmt.executeQuery()
                val users = mutableListOf<UserEntity>()
                while (rs.next()) {
                    users.add(
                        UserEntity(
                            id = UUID.fromString(rs.getString("id")),
                            name = rs.getString("name"),
                            email = rs.getString("email")
                        )
                    )
                }
                users
            }
        }
    }

    override fun update(user: UserEntity):UserEntity? {
        val sql = "UPDATE tuser SET name = ?, email = ? WHERE id = ?"
        return try {

            dataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, user.name)
                    stmt.setString(2, user.email)
                    stmt.setString(3, user.id.toString())
                    val rs = stmt.executeUpdate()
                    if (rs == 1){
                        user
                    }
                    else{
                        console.showMessage("***Error*** UPDATE Query failed.")
                        null
                    }
                }
            }
        }catch (e: SQLException){
            null
        }
    }

    override fun delete(id: UUID): Boolean {
        val sql = "DELETE FROM tuser WHERE id = ?"
        return try {

            dataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, id.toString())
                    return (stmt.executeUpdate() == 1)
                }
            }
        } catch (e: SQLException){
            console.showMessage("***Error*** Query failed.")
            false
        }
    }
}


interface UserService {
    fun create(user: UserEntity): UserEntity?
    fun getById(id: UUID): UserEntity?
    fun update(user: UserEntity): UserEntity?
    fun delete(id: UUID)
    fun getAll(): List<UserEntity>
}


class UserServiceImpl(private val userDao: UserDAO) : UserService {
    override fun create(user: UserEntity): UserEntity? {
        return userDao.create(user)
    }

    override fun getById(id: UUID): UserEntity? {
        return userDao.getById(id)
    }

    override fun update(user: UserEntity): UserEntity? {
        return userDao.update(user)
    }

    override fun delete(id: UUID) {
        userDao.delete(id)
    }

    override fun getAll(): List<UserEntity> {
        return userDao.getAll()
    }
}


object DataSourceFactory {
    enum class DataSourceType {
        HIKARI,
        JDBC
    }

    fun getDS(dataSourceType: DataSourceType): DataSource {
        return when (dataSourceType) {
            DataSourceType.HIKARI -> {
                val config = HikariConfig()
                config.jdbcUrl = "jdbc:h2:./default"
                config.username = "user"
                config.password = "user"
                config.driverClassName = "org.h2.Driver"
                config.maximumPoolSize = 10
                config.isAutoCommit = true
                config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                HikariDataSource(config)
            }

            DataSourceType.JDBC -> TODO()
        }
    }
}


fun main() {

    val consola = Console()

    // Creamos la instancia de la base de datos
    val dataSource = DataSourceFactory.getDS(DataSourceFactory.DataSourceType.HIKARI)

    // Creamos la instancia de UserDAO
    val userDao = UserDAOH2(dataSource, consola)

    // Creamos la instancia de UserService
    val userService = UserServiceImpl(userDao)

    // Creamos un nuevo usuario
    val newUser = UserEntity(name = "John Doe", email = "johndoe@example.com")
    var createdUser = newUser.let { userService.create(it) }
    consola.showMessage("Created user: $createdUser")

    // Obtenemos un usuario por su ID
    val foundUser = createdUser?.let { userService.getById(it.id) }
    consola.showMessage("Found user: $foundUser")

    // Actualizamos el usuario
    val updatedUser = foundUser?.copy(name = "Jane Doe")
    val savedUser = updatedUser?.let { userService.update(it) }
    consola.showMessage("Updated user: $savedUser")

    val otherUser = UserEntity(name = "Eduardo Fernandez", email = "eferoli@gmail.com")
    createdUser = userService.create( otherUser)
    consola.showMessage("Created user: $createdUser")


    // Obtenemos todos los usuarios
    var allUsers = userService.getAll()
    consola.show(allUsers)

    // Eliminamos el usuario
    if (savedUser != null) {
        userService.delete(savedUser.id)
    }
    consola.showMessage("User deleted")

    // Obtenemos todos los usuarios
    allUsers = userService.getAll()
    consola.showMessage("All users: $allUsers")

    // Eliminamos el usuario
    userService.delete(otherUser.id)
    consola.showMessage("User deleted")
}
