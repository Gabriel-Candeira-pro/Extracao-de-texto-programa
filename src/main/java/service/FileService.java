package service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import model.FileData;
import scanner.DirectoryScanner;
import reader.FileContentReader;

public class FileService {
    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    private final DirectoryScanner scanner;
    private final FileContentReader reader;

    public FileService(DirectoryScanner scanner, FileContentReader reader) {
        this.scanner = scanner;
        this.reader  = reader;
    }

    public List<FileData> loadAll(Path folder, Set<String> allowedExts) throws IOException {
        logger.info("Iniciando scan no diretório: " + folder);
        List<Path> allPaths = scanner.listFiles(folder);
        logger.info("Scan completo. Itens encontrados: " + allPaths.size());

        boolean acceptAll = allowedExts.contains("*");
        Set<String> normalizedExts = allowedExts.stream()
            .map(ext -> ext.replaceFirst("^\\*?\\.?","").toLowerCase())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());

        List<Path> filteredPaths = allPaths.stream()
            .filter(p -> p.toFile().isFile())
            .filter(p -> acceptAll || matchesExtension(p.getFileName().toString(), normalizedExts))
            .collect(Collectors.toList());
        logger.info("Arquivos após filtro de extensão: " + filteredPaths.size());

        List<FileData> dataList = new ArrayList<>();
        for (Path filePath : filteredPaths) {
            logger.fine("Lendo arquivo: " + filePath);
            try {
                String content = reader.read(filePath);
                List<String> lines = Arrays.asList(content.split("\\R"));
                dataList.add(new FileData(filePath.toString(), lines));
                logger.fine("Leitura bem-sucedida: " + filePath.getFileName());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Falha ao ler: " + filePath, e);
            }
        }

        logger.info("loadAll concluído. Total de FileData: " + dataList.size());
        return dataList;
    }

    /** Verifica se o nome do arquivo bate com algum padrão de extensão */
    private boolean matchesExtension(String fileName, Set<String> exts) {
        String lowerName = fileName.toLowerCase();
        for (String ext : exts) {
            if (lowerName.endsWith("." + ext) || lowerName.equals(ext)) {
                return true;
            }
        }
        return false;
    }
}