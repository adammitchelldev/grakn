package grakn.core.cli;

/**
 * A definition of a Grakn component, containing all the information necessary to load it.
 *
 * The componentJarName is the name of the jar containing the commandClass.
 */
public class GraknComponentDefinition {
    private final String componentName;
    private final String commandClass;
    private final String componentJarName;
    private final String componentDistributionArtifact;
    private final String componentDistributionGroup;

    public GraknComponentDefinition(String componentName,
                                    String commandClass,
                                    String componentJarName,
                                    String componentDistributionArtifact,
                                    String componentDistributionGroup) {
        this.componentName = componentName;
        this.commandClass = commandClass;
        this.componentJarName = componentJarName;
        this.componentDistributionArtifact = componentDistributionArtifact;
        this.componentDistributionGroup = componentDistributionGroup;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getCommandClass() {
        return commandClass;
    }

    public String getComponentJarName() {
        return componentJarName;
    }

    public String getComponentDistributionArtifact() {
        return componentDistributionArtifact;
    }

    public String getComponentDistributionGroup() {
        return componentDistributionGroup;
    }
}
