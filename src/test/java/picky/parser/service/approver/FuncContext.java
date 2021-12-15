package picky.parser.service.approver;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static picky.parser.service.FuncUtil.normalizeName;


public class FuncContext {

    private final ContextType type;
    private final Map<String, Map<String, Path>> data;

    public FuncContext(ContextType type) {
        ImmutableMap.Builder<String, Map<String, Path>> s = ImmutableMap.builder();
        URL funcDir = this.getClass().getClassLoader().getResource(type.path);
        if (funcDir != null) {
            File pageDirFilePath = Paths.get(funcDir.getPath()).toFile();
            for (File sourceDir : pageDirFilePath.listFiles()) {
                ImmutableMap.Builder<String, Path> p = ImmutableMap.builder();
                File[] pages = sourceDir.listFiles();
                if (pages != null) {
                    for (File page : pages) {
                        p.put(FilenameUtils.getBaseName(page.getName()), page.toPath());
                    }
                }
                s.put(sourceDir.getName(), p.build());
            }
        }
        data = s.build();
        this.type = type;
    }

    public Set<String> getKeys() {
        return data.keySet();
    }

    public Set<String> getValueKeys(String sourceName) {
        return data.get(sourceName).keySet();
    }

    public byte[] get(String sourceName, String sourcePageName) {
        try {
            sourceName = normalizeName(sourceName);
            if (!data.containsKey(sourceName)) {
                throw new IllegalArgumentException("source:notfound:" + sourceName);
            }
            sourcePageName = normalizeName(sourcePageName);
            Path path = data.get(sourceName).get(sourcePageName);
            if (path == null) {
                return new byte[0];
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean exists(String sourceName, String sourcePageName) {
        Path resolvedPageContentFile = resolvePath(sourceName, sourcePageName);
        return Files.exists(resolvedPageContentFile);
    }

    public Path approve(String sourceName, String sourcePageName, String pageContent) {

        Path resolvedPageContentFile = resolvePath(sourceName, sourcePageName);
        if (Files.exists(resolvedPageContentFile)) {
            return resolvedPageContentFile;
        }
        try {
            Path pageContentFile = Files.createFile(resolvedPageContentFile);
            return Files.write(pageContentFile, pageContent.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path resolvePath(String sourceName, String sourcePageName) {
        try {
            sourceName = normalizeName(sourceName);
            sourcePageName = normalizeName(sourcePageName);
            Path sourcePageDir = Files.createDirectories(
                getResourcesDir().resolve(sourceName.toLowerCase())
            );
            return sourcePageDir.resolve(sourcePageName + type.extension);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private Path getResourcesDir() {
        Path rootDir = Paths.get("");
        if (!rootDir.toAbsolutePath().startsWith("parser")) {
            rootDir = rootDir.resolve("parser-service");
        }
        return rootDir.resolve("src/test/resources").resolve(type.path);
    }

    @AllArgsConstructor
    public enum ContextType {
        SOURCE_PAGE(".json"), WEB_PAGE(".html"), NEWS(".json");

        private final String extension;
        private final String path = this.name().toLowerCase(Locale.ROOT);
    }

}
