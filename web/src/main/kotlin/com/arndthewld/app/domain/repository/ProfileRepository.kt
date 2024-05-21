package com.arndthewld.app.domain.repository

import com.arndthewld.app.domain.Profile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

internal object Profiles : Table() {
    val userId: Column<Long> = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val biography = varchar("biography", 500).nullable()
    val avatar = varchar("avatar", 200).nullable()

    fun toDomain(row: ResultRow): Profile {
        return Profile(
            userId = row[userId],
            bio = row[biography],
            avatar = row[avatar],
        )
    }
}

class ProfileRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(Profiles)
        }
    }

    fun findByUserId(userId: Long): Profile? {
        return transaction(Database.connect(dataSource)) {
            Profiles.selectAll()
                .where { Profiles.userId eq userId }
                .map { Profiles.toDomain(it) }
                .firstOrNull()
        }
    }
}
