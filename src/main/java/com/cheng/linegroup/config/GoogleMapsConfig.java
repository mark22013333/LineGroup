package com.cheng.linegroup.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class GoogleMapsConfig {

    /**
     * Google Maps API Key
     */
    @Value("${google.maps.api-key}")
    private String apiKey;

}
