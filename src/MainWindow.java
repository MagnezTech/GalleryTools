import javafx.scene.input.KeyCode;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

/**
 * Created by mgwozdek on 2016-10-31.
 */
public class MainWindow {
    private JPanel mainPanel;
    private JLabel targetDirectoryLabel;
    private JLabel nameLabel;
    private JLabel currentNumberLabel;
    private JLabel modeLabel;
    private JLabel maxWidthLabel;
    private JLabel maxHeightLabel;
    private JLabel qualityLabel;
    private JList listOfFiles;
    private JTextField targetDirectoryTextField;
    private JCheckBox changeSizeCheckBox;
    private JTextField maxWidthTextField;
    private JTextField maxHeightTextField;
    private JButton targetDirectoryButton;
    private JTextField prefixTextField;
    private JSpinner currentNumberSpinner;
    private JCheckBox keepOryginalCheckBox;
    private JComboBox qualityComboBox;
    private JComboBox modeComboBox;
    private JScrollPane previewScroll;
    private JButton fire;
    private JProgressBar progress;
    private DefaultListModel model;

    public MainWindow() {
        qualityComboBox.setModel(new DefaultComboBoxModel<>(Scalr.Method.values()));
        qualityComboBox.setSelectedItem(Scalr.Method.QUALITY);
        modeComboBox.setModel(new DefaultComboBoxModel<>(Scalr.Mode.values()));
        modeComboBox.setSelectedItem(Scalr.Mode.AUTOMATIC);
        listOfFiles.setModel(new DefaultListModel<String>());
        targetDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("C:"));
                chooser.setDialogTitle("Wybierz katalog docelowy");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(targetDirectoryButton) == JFileChooser.APPROVE_OPTION) {
                    targetDirectoryTextField.setText(chooser.getSelectedFile().toString());
                }
            }
        });
        changeSizeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxHeightLabel.setEnabled(changeSizeCheckBox.isSelected());
                maxHeightTextField.setEnabled(changeSizeCheckBox.isSelected());
                maxWidthLabel.setEnabled(changeSizeCheckBox.isSelected());
                maxWidthTextField.setEnabled(changeSizeCheckBox.isSelected());
                qualityLabel.setEnabled(changeSizeCheckBox.isSelected());
                qualityComboBox.setEnabled(changeSizeCheckBox.isSelected());
                modeLabel.setEnabled(changeSizeCheckBox.isSelected());
                modeComboBox.setEnabled(changeSizeCheckBox.isSelected());
            }
        });
        JComponent comp = currentNumberSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        new FileDrop(System.out, mainPanel, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                new ReadPhotos(files).execute();
            }
        });
        fire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GeneratePhotos().execute();
            }
        });
    }

    private void log(String message) {
        ((DefaultListModel) listOfFiles.getModel()).addElement(message);
        listOfFiles.ensureIndexIsVisible(listOfFiles.getModel().getSize() - 1);
    }

    private String currentNumber(String path) {
        int number = (Integer) currentNumberSpinner.getValue();
        if (number >= 10000) {
            return path.replaceAll("(.*0*)(00000)", "$1" + number);
        } else if (number >= 1000) {
            return path.replaceAll("(.*0*)(0000)", "$1" + number);
        } else if (number >= 100) {
            return path.replaceAll("(.*0*)(000)", "$1" + number);
        } else if (number >= 10) {
            return path.replaceAll("(.*0*)(00)", "$1" + number);
        } else {
            return path.replaceAll("(.*0*)(0)", "$1" + number);
        }
    }

    private JList createImageList() {

        final JList imageList = new JList(createModel());
        imageList.setCellRenderer(new ImageCellRenderer());
        imageList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imageList.setVisibleRowCount(-1);
        imageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        imageList.setFixedCellWidth(100);
        imageList.setFixedCellHeight(120);
        imageList.setDragEnabled(true);
        imageList.setDropMode(DropMode.INSERT);
        imageList.setTransferHandler(new ImageTransferHandler(imageList));
        imageList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    try {
                        DefaultListModel model = (DefaultListModel) imageList.getModel();
                        for (Object o : imageList.getSelectedValuesList()) {
                            while (model.contains(o)) {
                                model.removeElement(o);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        return imageList;
    }

    private DefaultListModel createModel() {
        model = new DefaultListModel();
        return model;
    }

    private void createUIComponents() {
        previewScroll = new JScrollPane(createImageList());
    }

    class ImageTransferHandler extends TransferHandler {

        private final DataFlavor DATA_FLAVOUR = new DataFlavor(ImagePreview.class, "Images");

        private final JList previewList;
        private boolean inDrag;

        ImageTransferHandler(JList previewList) {
            this.previewList = previewList;
        }

        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        protected Transferable createTransferable(JComponent c) {
            inDrag = true;
            return new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] {DATA_FLAVOUR};
                }

                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(DATA_FLAVOUR);
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    return previewList.getSelectedValue();
                }
            };
        }

        public boolean canImport(TransferSupport support) {
            if (!inDrag || !support.isDataFlavorSupported(DATA_FLAVOUR)) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
            if (dl.getIndex() == -1) {
                return false;
            } else {
                return true;
            }
        }

        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable transferable = support.getTransferable();
            try {
                Object draggedImage = transferable.getTransferData(DATA_FLAVOUR);

                JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
                DefaultListModel model = (DefaultListModel)previewList.getModel();
                int dropIndex = dl.getIndex();
                if (model.indexOf(draggedImage) < dropIndex) {
                    dropIndex--;
                }
                model.removeElement(draggedImage);
                model.add(dropIndex, draggedImage);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            inDrag = false;
        }
    }

    class ImageCellRenderer extends JPanel implements ListCellRenderer {

        DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
        JLabel imageLabel = new JLabel();
        JLabel textLabel = new JLabel();

        ImageCellRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
            imageLabel.setBorder(emptyBorder);
            imageLabel.setAlignmentX(new Float(0.5));
            add(imageLabel);
            textLabel.setBorder(emptyBorder);
            textLabel.setAlignmentX(new Float(0.5));
            add(textLabel);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(defaultListCellRenderer.getBorder());
            setBackground(defaultListCellRenderer.getBackground());
            imageLabel.setIcon(new ImageIcon(((ImagePreview) value).getImage()));
            textLabel.setText(((ImagePreview) value).getDiscription());
            return this;
        }
    }

    private class ReadPhotos extends SwingWorker {
        private File[] files;

        public ReadPhotos(File[] files) {
            this.files = files;
        }

        @Override
        public String doInBackground() {
            progress.setValue(0);
            double perPhoto = 100.0 / files.length;
            double percent = 0;
            for (File file : files) {
                try{
                    BufferedImage iconImage = Scalr.resize(ImageIO.read(file), Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 100, 100);
                    ImagePreview newPreview = new ImagePreview(iconImage, file.getName(), file.getCanonicalPath());
                    model.addElement(newPreview);
                    percent += perPhoto;
                    progress.setValue((int) Math.round(percent));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            progress.setValue(100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progress.setValue(0);
        }
    }

    private class GeneratePhotos extends SwingWorker {

        @Override
        public String doInBackground() {
            progress.setValue(0);
            double perPhoto = 100.0 / model.toArray().length;
            double percent = 0;
            try{
                fire.setEnabled(false);
                boolean resize = changeSizeCheckBox.isSelected();
                boolean keepOriginal = keepOryginalCheckBox.isSelected();
                int maxWidth = 0;
                int maxHeight = 0;
                if (resize) {
                    maxWidth = Integer.valueOf(maxWidthTextField.getText());
                    maxHeight = Integer.valueOf(maxHeightTextField.getText());
                }
                String newPath = targetDirectoryTextField.getText() + "\\" + prefixTextField.getText();

                for (Object o : model.toArray()) {
                    ImagePreview image = (ImagePreview) o;
                    File file = new File(image.getPath());

                    File newFile = new File(currentNumber(newPath) + ".jpg");
                    try {
                        BufferedImage srcImage = ImageIO.read(file);
                        if (resize) {
                            BufferedImage scaledImage = Scalr.resize(srcImage, (Scalr.Method) qualityComboBox.getSelectedItem(), (Scalr.Mode) modeComboBox.getSelectedItem(), maxWidth, maxHeight);
                            ImageIO.write(scaledImage, "jpg", newFile);
                            if (!keepOriginal) {
                                file.delete();
                            }
                        } else {
                            if (keepOriginal) {
                                Files.copy(file.toPath(), newFile.toPath());
                            } else {
                                file.renameTo(newFile);
                            }
                        }
                        currentNumberSpinner.setValue(new Integer((Integer) currentNumberSpinner.getValue()) + 1);
                        log(file.getName());
                    } catch (FileAlreadyExistsException ex) {
                        log("PLIK JUŻ ISTNIEJE: " + newFile.getPath());
                        ex.printStackTrace();
                    } catch (AccessDeniedException ex) {
                        log("BRAK DOSTEPU DO KATALOGU: " + ex.getMessage());
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        log("BŁĄD: " + ex.toString());
                        ex.printStackTrace();
                    }
                    percent += perPhoto;
                    progress.setValue((int) Math.round(percent));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                fire.setEnabled(true);
            }

            return null;
        }

        @Override
        protected void done() {
            progress.setValue(100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progress.setValue(0);
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame frame = new JFrame("MainWindow");
        frame.setLocationByPlatform(true);
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.pack();
        frame.setVisible(true);
    }

}
