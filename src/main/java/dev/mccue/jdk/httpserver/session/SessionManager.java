package dev.mccue.jdk.httpserver.session;


import com.sun.net.httpserver.Request;
import dev.mccue.jdk.httpserver.cookies.Cookies;
import dev.mccue.jdk.httpserver.cookies.SetCookieHeader;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class SessionManager {
    private final SessionStore store;
    private final String root;
    private final String cookieName;
    private final Consumer<SetCookieHeader.Builder> customizeCookie;
    private SessionManager(Builder builder) {
        this.store = builder.store;
        this.root = builder.root;
        this.cookieName = builder.cookieName;
        this.customizeCookie = builder.customizeCookie;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads session data from a {@link Request}.
     * @param request The {@link Request} which contains session data.
     * @return The active session, if one exists.
     */
    public Optional<Session> read(
            Request request
    ) {
        var cookies = Cookies.parse(request);
        var sessionCookie = cookies.get(cookieName)
                .orElse(null);
        if (sessionCookie == null){
            return Optional.empty();
        }

        var key = new SessionKey(sessionCookie);
        var value = this.store.read(key);
        return value.map(data -> new Session(key, data));
    }

    /**
     * Writes the given session into an appropriate {@code Set-Cookie}
     * header.
     *
     * <p>
     *     After doing this, the given {@link Session} should be considered invalid.
     * </p>
     *
     * @param session The session to write.
     * @return The header to put in the Response.
     */
    public String write(Session session) {
        var newSessionKey = store.write(session.key(), session.data());
        var header = SetCookieHeader.builder(cookieName, newSessionKey.value())
                .path(root);
        customizeCookie.accept(header);
        return header.build();
    }

    public static final class Builder {
        SessionStore store;
        String root;
        String cookieName;
        Consumer<SetCookieHeader.Builder> customizeCookie;

        Builder() {
            this.store = SessionStore.inMemory();
            this.root = "/";
            this.cookieName = "__session_cookie";
            this.customizeCookie = (cookie) -> cookie.httpOnly(true);
        }

        public Builder store(SessionStore store) {
            this.store = Objects.requireNonNull(store);
            return this;
        }

        public Builder root(String root) {
            this.root = Objects.requireNonNull(root);
            return this;
        }

        public Builder cookieName(String cookieName) {
            this.cookieName = Objects.requireNonNull(cookieName);
            return this;
        }

        public Builder customizeCookie(Consumer<SetCookieHeader.Builder> customizeCookie) {
            this.customizeCookie = Objects.requireNonNull(customizeCookie);
            return this;
        }

        public SessionManager build() {
            return new SessionManager(this);
        }
    }
}
