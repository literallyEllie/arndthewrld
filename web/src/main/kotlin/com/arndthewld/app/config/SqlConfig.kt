package com.arndthewld.app.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class SqlConfig(jdbcUrl: String, username: String, password: String) {
    private val dataSource: DataSource = HikariConfig().let { config ->
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        HikariDataSource(config)
    }

    fun getDataSource(): DataSource {
        return dataSource
    }
}