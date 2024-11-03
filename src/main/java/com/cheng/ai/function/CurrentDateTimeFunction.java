package com.cheng.ai.function;

import java.util.Date;
import java.util.function.Function;

/**
 * @author Cheng
 * @since 2024/11/3 21:55
 **/
public class CurrentDateTimeFunction implements Function<CurrentDateTimeFunction.Request, CurrentDateTimeFunction.Response> {
    @Override
    public Response apply(Request request) {
        return new Response(new Date());
    }

    public record Request(String State) {
    }

    public record Response(Date currDateTime) {
    }
}
