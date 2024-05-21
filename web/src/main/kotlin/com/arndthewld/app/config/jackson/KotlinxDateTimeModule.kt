package com.arndthewld.app.config.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.datetime.LocalDateTime

// JavaTimeModule (jsr310) failed me.
class KotlinxDateTimeModule : SimpleModule() {
    init {
        addSerializer(LocalDateTime::class.java, Serializer())
        addDeserializer(LocalDateTime::class.java, Deserializer())
    }

    class Serializer : JsonSerializer<LocalDateTime>() {
        override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.toString())
        }
    }

    class Deserializer : JsonDeserializer<LocalDateTime>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
            // input will never be 'null'
            return LocalDateTime.parse(p.text)
        }
    }
}