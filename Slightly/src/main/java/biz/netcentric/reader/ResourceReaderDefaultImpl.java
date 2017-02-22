package biz.netcentric.reader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by dev-camiloh on 2/21/17.
 */
public class ResourceReaderDefaultImpl implements ResourceReader {

    @Override
    public Resource getResource(URI uri) {

        Resource resource;

        try (Stream<String> stream = Files.lines(Paths.get(uri))) {
            resource = new Resource(stream.reduce("", (a, b) -> a + b));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return resource;
    }

}
