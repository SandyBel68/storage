package com.epam.ekc.storage.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class InstantDynamoConverter implements DynamoDBTypeConverter<String, Instant> {
    @Override
    public String convert(Instant object) {
        return object.toString();
    }

    @Override
    public Instant unconvert(String object) {
        return LocalDateTime.parse(object, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of("America/Toronto"))
                .toInstant();
    }
}
