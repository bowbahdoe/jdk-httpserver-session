package dev.mccue.jdk.httpserver.session;

import dev.mccue.json.Json;

import java.util.Objects;
import java.util.function.Function;

public record Session(SessionKey key, SessionData data) {
    public Session {
        Objects.requireNonNull(key);
        Objects.requireNonNull(data);
    }

    public Session() {
        this(SessionKey.random(), new SessionData(Json.emptyObject()));
    }

    public Session update(Function<SessionData, SessionData> f) {
        return new Session(key, f.apply(data));
    }
}
