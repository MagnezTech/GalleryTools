import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private JScrollBar scrollBar1;

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
                chooser.setCurrentDirectory(new java.io.File("."));
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
                boolean resize = changeSizeCheckBox.isSelected();
                boolean keepOriginal = keepOryginalCheckBox.isSelected();
                int maxWidth = 0;
                int maxHeight = 0;
                if (resize) {
                    maxWidth = Integer.valueOf(maxWidthTextField.getText());
                    maxHeight = Integer.valueOf(maxHeightTextField.getText());
                }
                String newPath = targetDirectoryTextField.getText() + "\\" + prefixTextField.getText();
                for (int i = 0; i < files.length; i++) {
                    File newFile = new File(currentNumber(newPath) + ".jpg");
                    try {
                        if (resize) {
                            BufferedImage srcImage = ImageIO.read(files[i]);
                            BufferedImage scaledImage = Scalr.resize(srcImage, (Scalr.Method) qualityComboBox.getSelectedItem(), (Scalr.Mode) modeComboBox.getSelectedItem(), maxWidth, maxHeight);
                            ImageIO.write(scaledImage, "jpg", newFile);
                            if (!keepOriginal) {
                                files[i].delete();
                            }
                        } else {
                            if (keepOriginal) {
                                Files.copy(files[i].toPath(), newFile.toPath());
                            } else {
                                files[i].renameTo(newFile);
                            }
                        }
                        currentNumberSpinner.setValue(new Integer((Integer) currentNumberSpinner.getValue()) + 1);
                        log(files[i].getName());
                    } catch (FileAlreadyExistsException e) {
                        log("PLIK JUŻ ISTNIEJE: " + newFile.getPath());
                        System.err.println(e);
                    } catch (AccessDeniedException e) {
                        log("BRAK DOSTEPU DO KATALOGU: " + e.getMessage());
                        System.err.println(e);
                    } catch (Exception e) {
                        log("BŁĄD: " + e.toString());
                        System.err.println(e);
                    }
                }
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(690, 250));
        frame.pack();
        frame.setVisible(true);
    }
}
