package ui;

import java.nio.file.Path;
import java.util.Set;

public class Config {
    public final Path folder;
    public final Set<String> exts;
    public final Path output;
    public final String format;

    public Config(Path folder, Set<String> exts, Path output, String format) {
        this.folder = folder;
        this.exts   = exts;
        this.output = output;
        this.format = format;
    }
}