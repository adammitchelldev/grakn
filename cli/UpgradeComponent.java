package grakn.core.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "upgrade")
public class UpgradeComponent implements Callable<Integer> {

    private static final String REPO_TEMPLATE =
            "https://repo.grakn.ai/repository/distribution-snapshot/{group-name}/{artifact-name}/{version}/{artifact-name}-{version}.tgz";
    private static final String DOWNLOAD_PATH = "downloads/{group-name}/{artifact-name}/{version}/{artifact-name}-{version}.tgz";

    private final GraknComponentDefinition[] availableComponents;

    public UpgradeComponent(GraknComponentDefinition[] availableComponents) {
        this.availableComponents = availableComponents;
    }

    @Parameters(index = "0", description = "Component to upgrade.")
    String componentToUpgrade;

    @Option(names = {"-V", "--version"}, required = true, description = "The version to upgrade to.")
    String version;

    @Override
    public Integer call() throws Exception {
        Path graknHome = Paths.get(Objects.requireNonNull(System.getProperty("grakn.dir")));

        GraknComponentDefinition availableComponent = getAvailableComponent();
        if (availableComponent == null) {
            System.err.println("Component not available to upgrade.");
            return 1;
        }

        URL url = generateDownloadURL(availableComponent);
        Path downloadPath = graknHome.resolve(replaceVars(DOWNLOAD_PATH, availableComponent));

        System.out.println("Downloading " + url.toString() + " ...");
        GraknFileManager.download(url, downloadPath);
        System.out.println("Downloading complete.");

        Path componentPath = graknHome.resolve(componentToUpgrade);

        // Back-up old component
        Files.move(componentPath, graknHome.resolve(componentToUpgrade + "-BAK"));

        // Un-targz the new component
        GraknFileManager.unTarGz(downloadPath, componentPath);

        return 0;
    }

    private URL generateDownloadURL(GraknComponentDefinition component) throws MalformedURLException {
        return URI.create(replaceVars(REPO_TEMPLATE, component)).toURL();
    }

    private String replaceVars(String path, GraknComponentDefinition component) {
        return path
                .replace("{group-name}", component.getComponentDistributionGroup())
                .replace("{artifact-name}", component.getComponentDistributionArtifact())
                .replace("{version}", version);
    }

    private GraknComponentDefinition getAvailableComponent() {
        for (GraknComponentDefinition availableComponent : availableComponents) {
            if (availableComponent.getComponentName().equals(componentToUpgrade)) {
                return availableComponent;
            }
        }
        return null;
    }
}
