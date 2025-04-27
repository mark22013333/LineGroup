package com.cheng.linegroup.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * @author cheng
 * @since 2024/3/18 23:36
 **/
@OpenAPIDefinition(
        info = @Info(
                title = "LineGroup-API",
                description = "Line相關API",
                version = "0.0.2",
                termsOfService = "https://termify.io/",
                contact = @Contact(
                        name = "Cheng®",
                        email = "mark22013333@gmail.com",
                        url = "https://mark22013333.github.io"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "https://cheng.tplinkdns.com/apps/",
                        description = "General Url"
                )
        },
        security = @SecurityRequirement(name = "JWT"))
public class SwaggerConfig {

}
