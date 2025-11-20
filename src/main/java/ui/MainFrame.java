package ui;

import exporter.DocxExporter;
import exporter.TxtExporter;
import model.FileData;
import scanner.FilesExplorer;
import service.FileService;
import reader.StreamingFileContentReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private static final Logger consoleLog = Logger.getLogger(MainFrame.class.getName());

    private static final String PH_FOLDER = "Ex: C:/meuProjeto";
    private static final String PH_EXTS   = "*.java,*.xml,*.md ou *";
    private static final String PH_OUTPUT = "ConteudoEscaneado.docx";

    private final JTextField txtFolder;
    private final JTextField txtExts;
    private final JTextField txtOutput;
    private final JComboBox<String> cbFormat;
    private final JTextArea logArea;
    private final JButton btnStart;

    public MainFrame() {
        super("Escaneador e Exportador de Conteúdo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(650, 450);

        txtFolder = createPlaceholderField(PH_FOLDER);
        txtFolder.setToolTipText("Selecione a pasta raiz do projeto");

        txtExts = createPlaceholderField(PH_EXTS);
        txtExts.setToolTipText("Separe por vírgula: ex *.java,md,* ou * para tudo");

        txtOutput = createPlaceholderField(PH_OUTPUT);
        txtOutput.setToolTipText("Nome do arquivo de saída com extensão");

        cbFormat = new JComboBox<>(new String[]{"docx", "txt"});
        cbFormat.setToolTipText("Formato de saída: Word (docx) ou texto (txt)");

        logArea = new JTextArea();
        logArea.setEditable(false);

        btnStart = new JButton("Iniciar");
        btnStart.addActionListener(e -> onStart());

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets   = new Insets(6,6,6,6);
        gbc.fill     = GridBagConstraints.HORIZONTAL;
        gbc.weightx  = 0;

        // Linha 1: Pasta de origem
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Pasta de origem:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtFolder, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton btnBrowse = new JButton("…");
        btnBrowse.setToolTipText("Abrir seletor de pasta");
        btnBrowse.addActionListener(e -> chooseDirectory(txtFolder));
        panel.add(btnBrowse, gbc);

        // Linha 2: Extensões
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel("Extensões:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(txtExts, gbc);
        gbc.gridwidth = 1;

        // Linha 3: Arquivo de saída
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel("Arquivo de saída:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtOutput, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton btnSave = new JButton("…");
        btnSave.setToolTipText("Escolher arquivo de saída");
        btnSave.addActionListener(e -> chooseFile(txtOutput, cbFormat.getSelectedItem().toString()));
        panel.add(btnSave, gbc);

        // Linha 4: Formato
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel("Formato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        panel.add(cbFormat, gbc);

        // Botão Iniciar
        gbc.gridx = 2;
        panel.add(btnStart, gbc);

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    private void onStart() {
        btnStart.setEnabled(false);
        logArea.setText("");

        String folderPath = txtFolder.getText().trim();
        String outputPath = txtOutput.getText().trim();
        Set<String> exts = Arrays.stream(txtExts.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                // 1) Início do SwingWorker
                System.out.println("==> [SWING] doInBackground iniciado");
                System.out.flush();

                publish("🔍 Escaneando em: " + folderPath);
                System.out.println("==> [SWING] Antes de service.loadAll");
                System.out.flush();

                List<FileData> files;
                try {
                    files = new FileService(
                            new FilesExplorer(),
                            new StreamingFileContentReader()
                    ).loadAll(Path.of(folderPath), exts);
                } catch (IOException e) {
                    e.printStackTrace();
                    publish("❌ Erro ao escanear: " + e.getMessage());
                    return null;
                }

                System.out.println("==> [SWING] Após service.loadAll");
                System.out.flush();
                publish("✅ Total de arquivos lidos: " + files.size());

                // 2) Exportação
                try {
                    String fmt = cbFormat.getSelectedItem().toString().toLowerCase();
                    System.out.println("==> [SWING] Iniciando export para " + fmt);
                    System.out.flush();
                    publish("==> Exportando para formato " + fmt + "...");

                    if ("docx".equals(fmt)) {
                        new DocxExporter(Path.of(folderPath))
                            .export(files, Path.of(outputPath));
                    } else {
                        new TxtExporter().export(files, Path.of(outputPath));
                    }

                    System.out.println("==> [SWING] Export concluída");
                    System.out.flush();
                    publish("📄 Documento gerado: " + outputPath);

                } catch (IOException ex) {
                    ex.printStackTrace();
                    publish("❌ Erro na exportação: " + ex.getMessage());
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                chunks.forEach(line -> logArea.append(line + "\n"));
            }

            @Override
            protected void done() {
                btnStart.setEnabled(true);
                System.out.println("==> [SWING] SwingWorker done()");
                System.out.flush();
                publish("🏁 Processo concluído.");
            }
        }.execute();
    }

    private void chooseDirectory(JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath());
            field.setForeground(Color.BLACK);
        }
    }

    private void chooseFile(JTextField field, String ext) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(field.getText().trim()));
        fc.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase(), ext));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath());
            field.setForeground(Color.BLACK);
        }
    }

    private JTextField createPlaceholderField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }
}