package exporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import model.FileData;

public class TxtExporter {
    public void export(List<FileData> files, Path output) throws IOException {
        try (PrintWriter pw = new PrintWriter(output.toFile())) {
            String lastDir = "";
            for (FileData f : files) {
                Path rel = output.getParent().relativize(Path.of(f.getPath()));
                String dir = rel.getParent() != null ? rel.getParent().toString() : "";
                if (!dir.equals(lastDir)) {
                    pw.println("=== Pasta: " + dir + " ===");
                    lastDir = dir;
                }
                pw.println("-- Arquivo: " + rel.getFileName());
                for (String line : f.getLines()) {
                    pw.println("    " + line);
                }
                pw.println();
            }
        }
    }
}