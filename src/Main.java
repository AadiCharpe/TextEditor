import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JCheckBoxMenuItem;

import javax.swing.filechooser.FileFilter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.awt.Font;
import java.awt.Toolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        TextEditorFrame frame = new TextEditorFrame();
        frame.setVisible(true);
    }
}

class TextEditorFrame extends JFrame {
    private int font = 18;
    private int fontType = Font.PLAIN;
    private final TextEditorFrame parent = this;
    private File openFile = null;
    public TextEditorFrame() {
        setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        setTitle("Text Editor - New Unsaved File");
        setSize(800, 600);
        JFileChooser chooser = new JFileChooser();

        JTextArea text = new JTextArea();
        text.setFont(new Font("Sanserif", fontType, font));
        add(new JScrollPane(text));

        JPanel panel = new JPanel();
        JTextField from = new JTextField(12);
        JTextField to = new JTextField(12);
        JButton replace = new JButton("Replace");
        replace.addActionListener(e -> text.setText(text.getText().replaceFirst(from.getText(), to.getText())));
        JButton replaceAll = new JButton("Replace All");
        replaceAll.addActionListener(e -> text.setText(text.getText().replaceAll(from.getText(), to.getText())));
        panel.add(replace);
        panel.add(replaceAll);
        panel.add(from);
        panel.add(new JLabel("with"));
        panel.add(to);
        add(panel, "South");

        Timer saveTimer = new Timer(5000, e -> save(text.getText()));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu file = new JMenu("File");
        file.add("New").addActionListener(e -> {
            setTitle("Text Editor - New Unsaved File");
            openFile = null;
            saveTimer.stop();
            text.setText("");
        });
        file.add("Open").addActionListener(evt -> {
            try {
                chooser.setCurrentDirectory(new File(".."));
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
                    }

                    public String getDescription() {
                        return "Text Files";
                    }
                });
                int result = chooser.showOpenDialog(parent);
                if(result == JFileChooser.APPROVE_OPTION) {
                    setTitle("Text Editor - " + chooser.getSelectedFile().getName());
                    openFile = chooser.getSelectedFile();
                    text.setText("");
                    BufferedReader in = new BufferedReader(new FileReader(openFile));
                    String s;
                    while((s = in.readLine()) != null)
                        text.append(s + "\n");
                    in.close();
                    saveTimer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        file.addSeparator();
        file.add("Save As").addActionListener(evt -> {
            try {
                String name = JOptionPane.showInputDialog("Enter Name For File (With extension):");
                chooser.setCurrentDirectory(new File(".."));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showSaveDialog(parent);
                if (result == JFileChooser.APPROVE_OPTION) {
                    setTitle("Text Editor - " + name);
                    openFile = new File(chooser.getSelectedFile().getPath(), name);
                    save(text.getText());
                    saveTimer.start();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        file.addSeparator();
        file.add("Exit").addActionListener(e -> System.exit(0));
        menuBar.add(file);

        JMenu edit = new JMenu("Edit");
        edit.add("Cut").addActionListener(e -> {
            copy(text.getSelectedText());
            text.replaceSelection("");
        });
        edit.add("Copy").addActionListener(e -> copy(text.getSelectedText()));
        edit.add("Paste").addActionListener(e -> text.replaceSelection(paste()));
        menuBar.add(edit);

        JMenu view = new JMenu("View");
        view.add("Font +").addActionListener(e -> text.setFont(new Font("Sanserif", fontType, ++font)));
        view.add("Font -").addActionListener(e -> text.setFont(new Font("Sanserif", fontType, --font)));
        view.addSeparator();
        JCheckBoxMenuItem bold = new JCheckBoxMenuItem("Bold");
        JCheckBoxMenuItem italic = new JCheckBoxMenuItem("Italic");
        bold.addActionListener(e -> {
            fontType = 0;
            fontType += bold.isSelected() ? Font.BOLD : 0;
            fontType += italic.isSelected() ? Font.ITALIC : 0;
            text.setFont(new Font("Sanserif", fontType, font));
        });
        view.add(bold);
        italic.addActionListener(e -> {
            fontType = 0;
            fontType += bold.isSelected() ? Font.BOLD : 0;
            fontType += italic.isSelected() ? Font.ITALIC : 0;
            text.setFont(new Font("Sanserif", fontType, font));
        });
        view.add(italic);
        menuBar.add(view);
    }
    public void copy(String s) {
        if(s == null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s),null);
    }
    public String paste() {
        try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public void save(String text) {
        if(openFile != null) {
            try {
                PrintWriter out = new PrintWriter(openFile);
                out.print(text);
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}