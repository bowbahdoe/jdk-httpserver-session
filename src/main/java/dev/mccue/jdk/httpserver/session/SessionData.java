package dev.mccue.jdk.httpserver.session;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonEncodable;
import dev.mccue.json.JsonObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public record SessionData(JsonObject value) {
    public SessionData(JsonObject value) {
        this.value = Objects.requireNonNullElse(value, JsonObject.empty());
    }

    public Optional<Json> get(String key) {
        return get(key, j -> j);
    }

    public <T> Optional<T> get(String key, JsonDecoder<? extends T> decoder) {
        return JsonDecoder.optionalField(value, key, decoder);
    }

    public SessionData with(String key, String value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, int value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, long value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, float value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, double value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, boolean value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, Integer value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, Long value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, Float value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, Double value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, Boolean value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData with(String key, JsonEncodable value) {
        return new SessionData(
                Json.objectBuilder(this.value)
                        .put(key, value)
                        .build()
        );
    }

    public SessionData without(String key) {
        var m = new HashMap<>(this.value);
        m.remove(key);
        return new SessionData(JsonObject.of(m));
    }
}
