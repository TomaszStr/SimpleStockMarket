package io.tomaszstr.simplestockmarket.config;

import org.springframework.stereotype.Component;

@Component
public class SystemManager {
    public void terminate(int status) {
        System.exit(status);
    }
}
