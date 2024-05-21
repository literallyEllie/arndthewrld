package com.arndthewld.app.domain.repository

import com.arndthewld.app.domain.ProfileRole
import com.arndthewld.app.domain.User
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

internal object Users : LongIdTable() {
    // Index
    val email: Column<String> = varchar("email", 200).uniqueIndex()
    val username: Column<String?> = varchar("username", 50).nullable() // unique index ?

    // Meta
    val role = enumerationByName("role", 20, ProfileRole::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val lastLogin = datetime("last_login").nullable()

    fun toDomain(row: ResultRow): User {
        return User(
            userId = row[Users.id].value,
            email = row[email],
            username = row[username],
            role = row[role],
            createdAt = row[createdAt],
            lastLogin = row[lastLogin],
        )
    }
}

class UserRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(Users)
        }
    }

    /**
     * Inserts the user to the database and returns their user id.
     */
    fun insertUser(user: User): Long {
        return transaction(Database.connect(dataSource)) {
            Users.insertAndGetId {
                it[email] = user.email
                it[username] = user.username
                it[role] = user.role
            }
        }.value
    }

    fun findByUserId(id: Long): User? {
        return transaction(Database.connect(dataSource)) {
            Users.selectAll()
                .where { Users.id eq id }
                .map { Users.toDomain(it) }
                .firstOrNull()
        }
    }

    fun findUserIdByEmail(email: String): Long? {
        return transaction(Database.connect(dataSource)) {
            Users.select(Users.id)
                .where { Users.email eq email }
                .map { it[Users.id].value }
                .firstOrNull()
        }
    }

    fun findUserIdByUsername(username: String): Long? {
        return transaction(Database.connect(dataSource)) {
            Users.select(Users.id)
                .where { Users.username eq username }
                .map { it[Users.id].value }
                .firstOrNull()
        }
    }

    fun findUserByUsername(username: String): User? {
        return transaction(Database.connect(dataSource)) {
            Users.selectAll()
                .where { Users.username eq username }
                .map { Users.toDomain(it) }
                .firstOrNull()
        }
    }

    fun findUserByEmail(email: String): User? {
        return transaction(Database.connect(dataSource)) {
            Users.selectAll()
                .where { Users.email eq email }
                .map { Users.toDomain(it) }
                .firstOrNull()
        }
    }

    fun markLastLogin(userId: Long) {
        transaction(Database.connect(dataSource)) {
            Users.update({ Users.id eq userId }) {
                it[lastLogin] = now()
            }
        }
    }

    private fun now() = Clock.System.now().toLocalDateTime(TimeZone.UTC)
}
