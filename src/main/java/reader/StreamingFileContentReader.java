package reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StreamingFileContentReader implements FileContentReader {
    @Override
    public String read(Path file) throws IOException {
        return Files.readString(file);
    }
}