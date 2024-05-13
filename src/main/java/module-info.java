module dev.mccue.jdk.httpserver.session {
    requires transitive jdk.httpserver;
    requires dev.mccue.jdk.httpserver.cookies;
    requires transitive dev.mccue.json;

    exports dev.mccue.jdk.httpserver.session;
}