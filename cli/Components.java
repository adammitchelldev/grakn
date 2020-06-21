package grakn.core.cli;

public class Components {
    public static final GraknComponentDefinition SERVER = new GraknComponentDefinition(
            "server",
            "grakn.core.daemon.GraknDaemon",
            "io-grakn-core-grakn-daemon-0.0.0.jar",
            "none",
            "none"
    );

    public static final GraknComponentDefinition CONSOLE = new GraknComponentDefinition(
            "console",
            "grakn.console.GraknConsole",
            "io-grakn-console-grakn-console-0.0.0.jar",
            "console-deps",
            "graknlabs_console"
    );

    public static final GraknComponentDefinition[] AVAILABLE_COMPONENTS = new GraknComponentDefinition[] {
            SERVER,
            CONSOLE
    };
}
