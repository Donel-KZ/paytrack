package org.paytrack.com;

import org.springframework.boot.SpringApplication;

public class TestPaytrackApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaytrackApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
