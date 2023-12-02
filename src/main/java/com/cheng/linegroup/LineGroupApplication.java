package com.cheng.linegroup;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class LineGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(LineGroupApplication.class, args);
    }

}
