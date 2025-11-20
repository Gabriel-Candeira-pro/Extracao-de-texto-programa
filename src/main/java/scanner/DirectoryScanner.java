package scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DirectoryScanner {
    List<Path> listFiles(Path directory) throws IOException;
}