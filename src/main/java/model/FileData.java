// FileData.java
// responsável por armazenar o caminho e o conteúdo do arquivo para DTO (Objeto de Transferência de Dados)
package model;


import java.util.List;

public class FileData {
  private final String path; 
  private final List<String> lines;

  public FileData(String path, List<String> lines) {
    this.path = path;
    this.lines = lines;
  }

  public String getPath() {
    return path;
  }

  public List<String> getLines() {
    return lines;
  }
}
