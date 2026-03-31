package com.fiap.agendamento_servico.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ConfiguracaoJson {

        @Bean
        @Primary
        public ObjectMapper objectMapper() {
                DateTimeFormatter formatadorLocalTime = DateTimeFormatter.ofPattern("HH:mm");
                DateTimeFormatter formatadorLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter formatadorLocalDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                JavaTimeModule javaTimeModule = new JavaTimeModule();
                javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(formatadorLocalTime));
                javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(formatadorLocalDate));
                javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatadorLocalDateTime));
                javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(formatadorLocalTime));
                javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(formatadorLocalDate));
                javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatadorLocalDateTime));

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(javaTimeModule);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return objectMapper;
        }
}
