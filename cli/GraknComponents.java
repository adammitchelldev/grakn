package grakn.core.cli;

public class GraknComponents {
    public static final GraknComponentDefinition SERVER = new GraknComponentDefinition(
            "server",
            "grakn.core.daemon.GraknDaemon",
            "io-grakn-core-grakn-daemon",
            "none",
            "none"
    );

    public static final GraknComponentDefinition CONSOLE = new GraknComponentDefinition(
            "console",
            "grakn.console.GraknConsole",
            "io-grakn-console-grakn-console",
            "console-deps",
            "graknlabs_console"
    );

    public static final GraknComponentDefinition[] AVAILABLE_COMPONENTS = new GraknComponentDefinition[] {
            SERVER,
            CONSOLE
    };
}
