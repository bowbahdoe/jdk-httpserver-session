package dev.mccue.jdk.httpserver.session;

import java.util.Objects;
import java.util.UUID;

public record SessionKey(String value) {
    public SessionKey {
        Objects.requireNonNull(value);
    }

    public static SessionKey random() {
        return new SessionKey(UUID.randomUUID().toString());
    }
}
