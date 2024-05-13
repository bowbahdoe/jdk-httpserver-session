package dev.mccue.jdk.httpserver.session;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class InMemorySessionStore implements SessionStore {
    private final ConcurrentHashMap<SessionKey, SessionData> map;

    InMemorySessionStore() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<SessionData> read(SessionKey key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public SessionKey write(SessionKey key, SessionData data) {
        map.put(key, data);
        return key;
    }

    @Override
    public Optional<SessionKey> delete(SessionKey key) {
        map.remove(key);
        return Optional.empty();
    }
}