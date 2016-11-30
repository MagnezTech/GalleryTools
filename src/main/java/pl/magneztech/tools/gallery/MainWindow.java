package pl.magneztech.tools.gallery;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
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
import java.util.Iterator;
import java.util.List;

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
    private JButton chooseGalleryButton;
    private DefaultListModel model;

    public MainWindow() {
        $$$setupUI$$$();
        qualityComboBox.setModel(new DefaultComboBoxModel<>(Scalr.Method.values()));
        qualityComboBox.setSelectedItem(Scalr.Method.QUALITY);
        modeComboBox.setModel(new DefaultComboBoxModel<>(Scalr.Mode.values()));
        modeComboBox.setSelectedItem(Scalr.Mode.AUTOMATIC);
        listOfFiles.setModel(new DefaultListModel<String>());
        targetDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("C:"));
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
        fire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GeneratePhotos().execute();
            }
        });
        new DropTarget(mainPanel, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragExit(DropTargetEvent dte) {

            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    try {
                        if (flavor.isFlavorJavaFileListType()) {
                            List<File> files = (List<File>) transferable.getTransferData(flavor);
                            new ReadPhotos(files).execute();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dtde.dropComplete(true);

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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(10, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setEnabled(true);
        mainPanel.setMaximumSize(new Dimension(600, 700));
        mainPanel.setMinimumSize(new Dimension(600, 400));
        targetDirectoryLabel = new JLabel();
        targetDirectoryLabel.setText("Katalog docelowy");
        mainPanel.add(targetDirectoryLabel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetDirectoryTextField = new JTextField();
        targetDirectoryTextField.setText("C:\\");
        mainPanel.add(targetDirectoryTextField, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        changeSizeCheckBox = new JCheckBox();
        changeSizeCheckBox.setText("Zmień rozmiar zdjęcia");
        mainPanel.add(changeSizeCheckBox, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxWidthLabel = new JLabel();
        maxWidthLabel.setEnabled(false);
        maxWidthLabel.setText("Max szerokość");
        mainPanel.add(maxWidthLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxWidthTextField = new JTextField();
        maxWidthTextField.setEnabled(false);
        mainPanel.add(maxWidthTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 24), null, 0, false));
        maxHeightLabel = new JLabel();
        maxHeightLabel.setEnabled(false);
        maxHeightLabel.setText("Max wysokość");
        mainPanel.add(maxHeightLabel, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxHeightTextField = new JTextField();
        maxHeightTextField.setEnabled(false);
        mainPanel.add(maxHeightTextField, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        targetDirectoryButton = new JButton();
        targetDirectoryButton.setText("Wybierz");
        mainPanel.add(targetDirectoryButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Nazwa dla zdjęć");
        mainPanel.add(nameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prefixTextField = new JTextField();
        prefixTextField.setText("Przykład_000");
        mainPanel.add(prefixTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 24), null, 0, false));
        currentNumberLabel = new JLabel();
        currentNumberLabel.setText("Aktualny nr");
        mainPanel.add(currentNumberLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentNumberSpinner = new JSpinner();
        mainPanel.add(currentNumberSpinner, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keepOryginalCheckBox = new JCheckBox();
        keepOryginalCheckBox.setSelected(true);
        keepOryginalCheckBox.setText("Zachowaj oryginał");
        mainPanel.add(keepOryginalCheckBox, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        qualityComboBox = new JComboBox();
        qualityComboBox.setEnabled(false);
        mainPanel.add(qualityComboBox, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, 26), null, 0, false));
        qualityLabel = new JLabel();
        qualityLabel.setEnabled(false);
        qualityLabel.setText("Jakość");
        mainPanel.add(qualityLabel, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modeLabel = new JLabel();
        modeLabel.setEnabled(false);
        modeLabel.setText("Tryb");
        mainPanel.add(modeLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modeComboBox = new JComboBox();
        modeComboBox.setEnabled(false);
        mainPanel.add(modeComboBox, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 100), new Dimension(-1, 100), new Dimension(-1, 100), 0, false));
        listOfFiles = new JList();
        listOfFiles.setAutoscrolls(true);
        listOfFiles.setEnabled(true);
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        listOfFiles.setModel(defaultListModel1);
        listOfFiles.setSelectionMode(1);
        scrollPane1.setViewportView(listOfFiles);
        previewScroll.setDoubleBuffered(false);
        mainPanel.add(previewScroll, new GridConstraints(9, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fire = new JButton();
        fire.setText("START");
        mainPanel.add(fire, new GridConstraints(8, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progress = new JProgressBar();
        mainPanel.add(progress, new GridConstraints(7, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
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
                    return new DataFlavor[]{DATA_FLAVOUR};
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

            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
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

                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                DefaultListModel model = (DefaultListModel) previewList.getModel();
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
        private List<File> files;

        public ReadPhotos(List<File> files) {
            this.files = files;
        }

        @Override
        public String doInBackground() {
            progress.setValue(0);
            double perPhoto = 100.0 / files.size();
            double percent = 0;
            for (File file : files) {
                try {
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
            ((DefaultListModel) listOfFiles.getModel()).addElement("----- WCZYTANO ZDJĘCIA -----");
            listOfFiles.ensureIndexIsVisible(listOfFiles.getModel().getSize() - 1);
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
            try {
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
                    File newFilea = new File(currentNumber(newPath) + "T.jpg");

                    try {
                        if (resize) {
                            ImageInputStream inputStream = new FileImageInputStream(file);
                            Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(inputStream);
                            ImageReader reader = readerIterator.next();
                            reader.setInput(inputStream);
                            Iterator<IIOImage> imageIterator = reader.readAll(null);
                            IIOImage img = imageIterator.next();
                            BufferedImage srcImage = (BufferedImage) img.getRenderedImage();
                            BufferedImage resultImage = Scalr.resize(srcImage, (Scalr.Method) qualityComboBox.getSelectedItem(), (Scalr.Mode) modeComboBox.getSelectedItem(), maxWidth, maxHeight);
                            img.setRenderedImage(resultImage);
                            ImageWriter writer = ImageIO.getImageWriter(reader);
                            ImageOutputStream outputStream = new FileImageOutputStream(newFile);
                            writer.setOutput(outputStream);
                            writer.write(img);

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
            ((DefaultListModel) listOfFiles.getModel()).addElement("----- KONIEC -----");
            listOfFiles.ensureIndexIsVisible(listOfFiles.getModel().getSize() - 1);
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
