// FileContentReader.java

package reader;

import java.io.IOException;
import java.nio.file.Path;

public interface FileContentReader {
    String read(Path file) throws IOException;
}