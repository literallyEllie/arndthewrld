package com.arndthewld.app.config

import com.arndthewld.app.config.environment.AppEnvConfig
import com.arndthewld.app.config.session.DevSessionHandler
import com.arndthewld.app.web.ErrorExceptionMapping
import com.arndthewld.app.web.Router
import com.fasterxml.jackson.databind.SerializationFeature
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat

class AppConfig : KoinComponent {

    private val authConfig: AuthConfig by inject()
    private val appRouter: Router by inject()

    fun setup(): Javalin {
        // Load config
        AppEnvConfig.load()

        // DI
        GlobalContext.startKoin {
            modules(ModulesConfig.allModules)
        }

        configureMapper()
        val app = Javalin.create { config ->
            config.apply {
                bundledPlugins.enableDevLogging()
//                staticFiles.enableWebjars()

                jetty.defaultPort = AppEnvConfig["http.server_port"]?.toInt() ?: 8080

                // auth
                router.mount {
                    it.beforeMatched(authConfig::handleAccess)
                }
                router.contextPath = AppEnvConfig["http.context"] ?: "/api"
                config.jetty.modifyServletContextHandler { it.sessionHandler = DevSessionHandler().fileBased() }

                // routes
                appRouter.register(router)

//                enableCorsForAllOrigins()
            }
        }.events {
            it.serverStopping {
                GlobalContext.stopKoin()
            }
        }

        ErrorExceptionMapping.register(app)
        return app
    }

    private fun configureMapper() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        JavalinJackson.defaultMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(dateFormat)
            .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
    }
}