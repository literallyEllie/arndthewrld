package com.arndthewld.app.domain.repository

import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.domain.UserCredentials
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

internal object Auth : LongIdTable() {
    val email: Column<String> = varchar("email", 200).uniqueIndex()
    val username: Column<String?> = varchar("username", 50).nullable() // unique index ?

    // password
    val password: Column<String?> = varchar("password", 150).nullable()
    val oauthProviderSource: Column<OAuthProviderSource?> =
        enumeration("oauthProviderSource", OAuthProviderSource::class).nullable()
    val oauthProviderId: Column<String?> = varchar("oauthProviderSourceId", 255).nullable()

    fun toDomain(row: ResultRow): UserCredentials {
        return UserCredentials(
            email = row[email],
            username = row[username],
            password = row[password],
            oAuthProviderSource = row[oauthProviderSource],
            oAuthProviderId = row[oauthProviderId],
        )
    }
}

class AuthRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(Auth)
        }
    }

    fun getByEmail(email: String): UserCredentials? {
        return transaction(Database.connect(dataSource)) {
            Auth.select(Auth.email, Auth.username, Auth.password, Auth.oauthProviderSource, Auth.oauthProviderId)
                .where { Auth.email.eq(email) }
                .map { Auth.toDomain(it) }
                .firstOrNull()
        }
    }

    fun getByUsername(username: String): UserCredentials? {
        return transaction(Database.connect(dataSource)) {
            Auth.select(Auth.email, Auth.username, Auth.password, Auth.oauthProviderSource, Auth.oauthProviderId)
                .where { Auth.username.eq(username) }
                .map { Auth.toDomain(it) }
                .firstOrNull()
        }
    }

    fun insert(credentials: UserCredentials) {
        return transaction(Database.connect(dataSource)) {
            Auth.insert { row ->
                row[Auth.email] = credentials.email!!
                row[Auth.username] = credentials.username!!
                credentials.password?.also { row[Auth.password] = credentials.password }
                credentials.oAuthProviderSource?.also {
                    row[Auth.oauthProviderSource] = credentials.oAuthProviderSource
                }
                credentials.oAuthProviderId?.also { row[Auth.oauthProviderId] = credentials.oAuthProviderId }
            }
        }
    }

    fun update(email: String, credentials: UserCredentials): UserCredentials? {
        transaction(Database.connect(dataSource)) {
            Auth.update({ Auth.email eq email }) { row ->
                row[Auth.email] = credentials.email!!

                credentials.username?.also { row[Auth.username] = it }
                credentials.password?.also { row[Auth.username] = it }
                credentials.oAuthProviderSource?.also { row[Auth.oauthProviderSource] = it }
                credentials.oAuthProviderId?.also { row[Auth.oauthProviderId] = it }
            }
        }

        return getByEmail(credentials.email!!)
    }

    fun delete(email: String) {
        transaction(Database.connect(dataSource)) {
            Auth.deleteWhere { Auth.email eq email }
        }
    }
}