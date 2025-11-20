// Renomeie o arquivo para FilesExplorer.java
package scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesExplorer implements DirectoryScanner {

    public List<Path> scan(Path folder) {
        List<Path> result = new ArrayList<>();
        try {
            Files.walk(folder).forEach(result::add);
        } catch (IOException e) {
    
        }
        return result;
    }

    @Override
    public List<Path> listFiles(Path directory) throws IOException {
        try (var stream = Files.walk(directory)) {
            List<Path> result = stream
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
            System.out.println("==> VARREDURA CONCLUÍDA – arquivos encontrados: " + result.size());
            System.out.flush();
            return result;
        }
    }
}