package dev.mccue.jdk.httpserver.session;

import java.util.Optional;

public interface SessionStore {
    Optional<SessionData> read(SessionKey key);

    SessionKey write(SessionKey key, SessionData data);

    default SessionKey write(SessionData data) {
        return write(SessionKey.random(), data);
    }

    Optional<SessionKey> delete(SessionKey key);

    static SessionStore inMemory() {
        return new InMemorySessionStore();
    }

    static SessionStore cookie(byte[] secretKey) {
        return new CookieSessionStore(secretKey);
    }
}
