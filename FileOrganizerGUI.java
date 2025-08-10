import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class FileOrganizerGUI extends JFrame {
    private JTextField folderPathField;
    private JProgressBar progressBar;
    private static final String UNDO_LOG = "undo_log.txt";

    public FileOrganizerGUI() {
        setTitle("File Organizer");
        setSize(600, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        folderPathField = new JTextField(35);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JButton browseButton = new JButton("Browse");
        JButton organizeButton = new JButton("Organize");
        JButton undoButton = new JButton("Undo");

        add(new JLabel("Folder Path:"));
        add(folderPathField);
        add(browseButton);
        add(organizeButton);
        add(undoButton);
        add(progressBar);

        browseButton.addActionListener(e -> browseFolder());
        organizeButton.addActionListener(e -> organizeFiles(folderPathField.getText()));
        undoButton.addActionListener(e -> undoLastOrganization());

        // Make entire window accept folder drag and drop
        new DropTarget(this, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {}
            public void dragOver(DropTargetDragEvent dtde) {}
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            public void dragExit(DropTargetEvent dte) {}
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!droppedFiles.isEmpty()) {
                            File dropped = droppedFiles.get(0);
                            if (dropped.isDirectory()) {
                                folderPathField.setText(dropped.getAbsolutePath());
                            } else {
                                JOptionPane.showMessageDialog(FileOrganizerGUI.this,
                                        "Please drop a folder, not a file.",
                                        "Invalid Drop",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FileOrganizerGUI.this,
                            "Error during drag and drop: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void browseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void organizeFiles(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid folder path!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, String> extensionMap = new HashMap<>();
        extensionMap.put("jpg", "Images");
        extensionMap.put("jpeg", "Images");
        extensionMap.put("png", "Images");
        extensionMap.put("gif", "Images");
        extensionMap.put("pdf", "Documents");
        extensionMap.put("doc", "Documents");
        extensionMap.put("docx", "Documents");
        extensionMap.put("txt", "Documents");
        extensionMap.put("mp3", "Music");
        extensionMap.put("wav", "Music");
        extensionMap.put("mp4", "Videos");
        extensionMap.put("mkv", "Videos");

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "No files found in folder.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(UNDO_LOG))) {
                    int total = files.length;
                    int count = 0;
                    for (File file : files) {
                        if (file.isFile()) {
                            String extension = getFileExtension(file.getName());
                            String category = extensionMap.getOrDefault(extension, "Others");

                            File categoryFolder = new File(folderPath + "/" + category);
                            if (!categoryFolder.exists()) {
                                categoryFolder.mkdir();
                            }

                            File newFile = new File(categoryFolder, file.getName());
                            Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                            logWriter.write(newFile.getAbsolutePath() + "|" + file.getAbsolutePath());
                            logWriter.newLine();
                        }
                        count++;
                        int progress = (int) ((count / (double) total) * 100);
                        publish(progress);
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                progressBar.setValue(latestProgress);
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                JOptionPane.showMessageDialog(FileOrganizerGUI.this, "Files organized successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    private void undoLastOrganization() {
        File logFile = new File(UNDO_LOG);
        if (!logFile.exists()) {
            JOptionPane.showMessageDialog(this, "No undo log found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] paths = line.split("\\|");
                File currentFile = new File(paths[0]);
                File originalFile = new File(paths[1]);

                Files.move(currentFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            logFile.delete();
            progressBar.setValue(0);
            JOptionPane.showMessageDialog(this, "Files restored successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error during undo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileOrganizerGUI::new);
    }
}
