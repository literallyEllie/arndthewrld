package com.arndthewld.app.domain.repository

import com.arndthewld.app.config.oauth.OAuthProviderSource
import com.arndthewld.app.domain.UserCredentials
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

internal object Auth : Table() {
    val userId: Column<Long> = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)

    // pass
    val password: Column<String?> = varchar("password", 150).nullable()
    val oauthProviderSource: Column<OAuthProviderSource?> =
        enumeration("oauthProviderSource", OAuthProviderSource::class).nullable()
    val oauthProviderId: Column<String?> = varchar("oauthProviderSourceId", 255).nullable()

    fun toDomain(row: ResultRow): UserCredentials {
        return UserCredentials(
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

    fun findByUserId(userId: Long): UserCredentials? {
        return transaction(Database.connect(dataSource)) {
            Auth.selectAll()
                .where { Auth.userId eq userId }
                .map { Auth.toDomain(it) }
                .firstOrNull()
        }
    }

    fun insert(
        userId: Long,
        credentials: UserCredentials,
    ) {
        return transaction(Database.connect(dataSource)) {
            Auth.insert { row ->
                row[Auth.userId] = userId
                credentials.password?.also { row[Auth.password] = credentials.password }
                credentials.oAuthProviderSource?.also {
                    row[Auth.oauthProviderSource] = credentials.oAuthProviderSource
                }
                credentials.oAuthProviderId?.also { row[Auth.oauthProviderId] = credentials.oAuthProviderId }
            }
        }
    }

    fun update(
        userId: Long,
        credentials: UserCredentials,
    ): UserCredentials? {
        transaction(Database.connect(dataSource)) {
            Auth.update({ Auth.userId eq userId }) { row ->
                credentials.password?.also { row[Auth.password] = it }
                credentials.oAuthProviderSource?.also { row[Auth.oauthProviderSource] = it }
                credentials.oAuthProviderId?.also { row[Auth.oauthProviderId] = it }
            }
        }

        return findByUserId(userId)
    }
}
