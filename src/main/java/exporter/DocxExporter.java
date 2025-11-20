package exporter;

// DocxExporter.java
// responsável por exportar o conteúdo lido para um arquivo Word (.docx)

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.io.FileOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import model.FileData;

public class DocxExporter {
  private final Path folder;

  public DocxExporter(Path folder) {
    this.folder = folder;
  }

  public void export(List<FileData> files, Path output) throws IOException {
    // ordena por pasta…
    files.sort(Comparator.comparing(f -> folder.relativize(Path.of(f.getPath())).toString()));

    try (XWPFDocument doc = new XWPFDocument();
         FileOutputStream out = new FileOutputStream(output.toFile())) {

      String lastDir = "";
      for (FileData file : files) {
        Path rel = folder.relativize(Path.of(file.getPath()));
        String dir = rel.getParent() != null ? rel.getParent().toString() : "";

        if (!dir.equals(lastDir)) {
          XWPFParagraph h1 = doc.createParagraph();
          h1.setStyle("Heading1");
          h1.createRun().setText("Pasta: " + dir);
          lastDir = dir;
        }

        // título do arquivo
        XWPFParagraph titlePara = doc.createParagraph();
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(12);
        titleRun.setText("Arquivo: " + rel.getFileName());

        // agora cada linha vira um parágrafo
        for (String line : file.getLines()) {
          XWPFParagraph p = doc.createParagraph();
          p.setIndentationLeft(400);
          XWPFRun r = p.createRun();
          r.setFontFamily("Courier New");
          r.setFontSize(10);
          r.setText(line, 0); 
        }

        // separador em branco
        doc.createParagraph();
      }

      doc.write(out);
    }
  }
}