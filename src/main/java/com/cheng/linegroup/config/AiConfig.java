package com.cheng.linegroup.config;

import com.cheng.ai.function.CurrentDateTimeFunction;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cheng
 * @since 2024/11/3 21:57
 **/
@Configuration
public class AiConfig {

    @Bean
    public FunctionCallback currentDateTime() {
        return FunctionCallbackWrapper.builder(new CurrentDateTimeFunction())
                .withName("CurrentDateTime") // Function Name
                .withDescription("Get the Date Time")  // Description
                .withResponseConverter((response) -> response.currDateTime().toString())
                .build();
    }
}
