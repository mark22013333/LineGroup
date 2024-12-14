package com.cheng.linegroup.enums.mock;

import com.cheng.linegroup.enums.Api;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Cheng
 * @since 2024/12/10 00:20
 **/
@Slf4j
public class Mock {

    public static String getData(Api api) {
        return getData(api, null);
    }

    public static String getData(Api api, Map<String, String> params) {
        try {
            URI uri;
            switch (api) {
                case DATASET -> uri = ClassLoader.getSystemResource("/mock/dataSet.json").toURI();
                case DATA_STORE -> uri = ClassLoader.getSystemResource("/mock/dataStore.json").toURI();
                case DUMP_DATA_STORE -> {
                    if (params.values().stream().anyMatch(v -> v.contains("TWC_water"))) {
                        uri = ClassLoader.getSystemResource("/mock/dumpWaterOutageDataStore.json").toURI();
                    } else if (params.values().stream().anyMatch(v -> v.contains("electric"))) {
                        uri = ClassLoader.getSystemResource("/mock/dumpPowerOutageDataStore.json").toURI();
                    } else if (params.values().stream().anyMatch(v -> v.contains("airQuality"))) {
                        uri = ClassLoader.getSystemResource("/mock/dumpAQIDataStore.json").toURI();
                    } else {
                        throw new IllegalArgumentException("Unknown data type: " + params);
                    }
                }

                default -> throw new IllegalArgumentException("Unknown api: " + api);
            }

            return Files.readString(Paths.get(uri), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to read data set", e);
            return null;
        }
    }
}
