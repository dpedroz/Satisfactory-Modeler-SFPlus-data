package org.yesu.smsfp;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class Sorter {

    public static void main(String[] args) throws IOException {

        try (Stream<Path> pathStream = Files.list(Path.of("game_data"))) {
            pathStream
                    .filter(path -> path.toString().endsWith(".json"))
                    .peek(path -> log.info("Processing {}", path))
                    .forEach(Sorter::sortFile);
        }

    }

    static void sortFile(Path filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        String content;
        try {
            log.info("Reading '{}'", filePath);
            content = new String(Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new IllegalStateException("Error reading file " + filePath, e);
        }


        // parse json
        JsonObject source = gson.fromJson(content, JsonObject.class);

        Stream.of("Machines", "MultiMachines", "Parts", "Recipes").forEach(key -> {
            log.info("Processing '{}'", key);

            // validate
            checkUniqueElements(source.getAsJsonArray(key), key);

            // sort entries in each array
            source.add(key, sortedList(source.getAsJsonArray(key)));
        });

        try {
            log.info("Writing '{}'", filePath);
            Files.writeString(filePath, gson.toJson(source));
        } catch (IOException e) {
            throw new IllegalStateException("Error writing file " + filePath, e);
        }
    }

    static void checkUniqueElements(JsonArray jsonArray, String key) {
        Set<String> names = HashSet.newHashSet(jsonArray.size());
        final AtomicBoolean isError = new AtomicBoolean(false);
        jsonArray.forEach(jsonElement -> {
            String name = jsonElement.getAsJsonObject().get("Name").getAsString();
            if (!names.add(name)) {
                log.error("Duplicate name: {} in collection {}", name, key);
                isError.set(true);
            }
        });
        if (isError.get()) throw new IllegalStateException("Duplicate names found in collection " + key + ".");
    }

    static JsonArray sortedList(JsonArray array) {
        List<JsonElement> list = array.asList();
        list.sort(Comparator.comparing(el -> el.getAsJsonObject().get("Name").getAsString()));
        JsonArray output = new JsonArray(array.size());
        list.forEach(el -> output.add(el.getAsJsonObject()));
        return output;
    }
}