///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Stream;

class update {
    public static final Path BAZAAR_DIR = Path.of("C:\\Program Files\\Tempo Launcher - Beta\\The Bazaar game_64\\bazaarwinprodlatest\\TheBazaar_Data\\StreamingAssets");
    public static final Path OUT_DIR = Path.of("./StreamingAssets");
    public static final Set<String> SPLIT_FILES = Set.of(
        "v2_Cards.json"
    );
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());


    public static void main(String[] args) throws IOException {
        new update().run();
    }

    private void run() throws IOException {
        try (Stream<Path> dirs = Files.walk(BAZAAR_DIR)) {
            dirs.forEach(path -> {
                try {
                    processFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void processFile(Path path) throws IOException {
        if (!path.toString().endsWith(".json")) {
            return;
        }
        if (SPLIT_FILES.contains(path.getFileName().toString())) {
            System.out.println("Splitting " + path);
            splitFile(path);
        } else {
            System.out.println("Copying " + path);
            writeFile(path);
        }
    }

    private void writeFile(Path path) throws IOException {
        Path outPath = OUT_DIR.resolve(BAZAAR_DIR.relativize(path));
        Files.createDirectories(outPath.getParent());
        Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void splitFile(Path path) throws IOException {
        JsonNode jsonFile = mapper.readTree(path.toFile());
        Path outDir = OUT_DIR.resolve(BAZAAR_DIR.relativize(trimEnding(path)));
        Files.createDirectories(outDir);
        for (JsonNode jsonNode : jsonFile) {
            Path outPath = outDir.resolve(jsonNode.get("Id").asText() + ".json");
            writer.writeValue(outPath.toFile(), jsonNode);
        }
    }

    private static Path trimEnding(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return path;
        }
        return path.resolveSibling(fileName.substring(0, index));
    }
}