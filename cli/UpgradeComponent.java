package grakn.core.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "upgrade")
public class UpgradeComponent implements Callable<Integer> {

    private static final String REPO_TEMPLATE =
            "https://repo.grakn.ai/repository/distribution-snapshot/{group-name}/{artifact-name}/{version}/{artifact-name}-{version}.tgz";
    private static final String DOWNLOAD_DIR = "downloads";
    private static final String DOWNLOAD_PATH_TEMPLATE = "{group-name}/{artifact-name}/{version}/{artifact-name}-{version}.tgz";

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
        Path downloadDir = graknHome.resolve(DOWNLOAD_DIR);

        GraknComponentDefinition availableComponent = getAvailableComponent();
        if (availableComponent == null) {
            System.err.println("Component not available to upgrade.");
            return 1;
        }

        URL url = generateDownloadURL(availableComponent);
        Path downloadPath = downloadDir.resolve(replaceVars(DOWNLOAD_PATH_TEMPLATE, availableComponent));

        System.out.println("Downloading " + url.toString() + " ...");
        GraknFileManager.download(url, downloadPath);
        System.out.println("Downloading complete.");

        Path componentPath = graknHome.resolve(componentToUpgrade);
        Path backupPath = graknHome.resolve(componentToUpgrade + "-BAK");

        // Back-up old component
        Files.move(componentPath, backupPath);

        // Un-targz the new component
        GraknFileManager.unTarGz(downloadPath, componentPath);

        // Delete backup folder
        Files.walk(backupPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        // Delete temporary download and dirs by backwards recursion through the path
        Path backwardsPath = downloadPath;
        while (!backwardsPath.equals(downloadDir) && backwardsPath.getNameCount() > 0) {
            Files.delete(backwardsPath);
            backwardsPath = backwardsPath.getParent();
        }

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
