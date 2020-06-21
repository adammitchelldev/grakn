package grakn.core.cli;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

public class GraknFileManager {
    public static void download(URL from, Path to) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) from.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "grakn");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        transfer(connection.getInputStream(), to);
    }

    public static void unTarGz(Path from, Path to) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(open(from));
        TarArchiveInputStream inputStream = new TarArchiveInputStream(gzipInputStream);

        TarArchiveEntry entry;
        while ((entry = inputStream.getNextTarEntry()) != null) {
            // Need to account for name repeated
            Path path = Paths.get(entry.getName());
            Path entryPath = to.resolve(path.subpath(1, path.getNameCount()));
            if (entry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                Files.createDirectories(entryPath.getParent());
                final OutputStream outputFileStream = Files.newOutputStream(entryPath);
                IOUtils.copy(inputStream, outputFileStream);
                outputFileStream.close();
            }
        }
    }

    public static void recursiveDeleteIfExists(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static void transfer(InputStream from, Path to) throws IOException {
        Files.createDirectories(to.getParent());
        FileOutputStream fileOutputStream = new FileOutputStream(to.toFile());
        fileOutputStream.getChannel()
                .transferFrom(Channels.newChannel(from), 0, Long.MAX_VALUE);
        fileOutputStream.flush();
    }

    private static InputStream open(Path from) throws IOException {
        return new BufferedInputStream(Files.newInputStream(from));
    }
}
