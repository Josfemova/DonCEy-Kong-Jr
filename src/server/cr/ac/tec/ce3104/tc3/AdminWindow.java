package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

class AdminWindow {
    public AdminWindow(PrintStream realStdout) throws IOException {
        this.realStdout = realStdout;
        this.fakeStdout = new PipedOutputStream();
        this.stdoutSink = new BufferedReader(new InputStreamReader(new PipedInputStream(this.fakeStdout)));

        SwingUtilities.invokeLater(() -> this.start());
    }

    public OutputStream getOutputStream() {
        return this.fakeStdout;
    }

    private final PrintStream realStdout;
    private final PipedOutputStream fakeStdout;
    private final BufferedReader stdoutSink;

    private JFrame frame;
    private JTextArea consoleOutput;
    private JTextField inputLine;
    private Thread sinkThread;

    private void start() {
        this.frame = new JFrame("DonCEy Kong Jr. server");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.PAGE_AXIS));

        Font font = new Font("monospaced", Font.PLAIN, 16);

        this.consoleOutput = new JTextArea(40, 100);
        this.consoleOutput.setFont(font);
        this.consoleOutput.setEditable(false);
        this.consoleOutput.setBackground(Color.BLACK);
        this.consoleOutput.setForeground(Color.WHITE);
        this.frame.getContentPane().add(new JScrollPane(this.consoleOutput));

        ((DefaultCaret)this.consoleOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.frame.getContentPane().add(Box.createHorizontalGlue());

        this.inputLine = new JTextField();
        this.inputLine.setFont(font);
        this.frame.getContentPane().add(inputLine);

        this.frame.pack();
        this.frame.setVisible(true);

        this.sinkThread = new Thread(() -> this.readSink());
        this.sinkThread.start();
    }

    private void readSink() {
        try {
            while (true) {
                String line = this.stdoutSink.readLine();
                if (line == null) {
                    break;
                }

                this.realStdout.println(line);
                SwingUtilities.invokeAndWait(() -> this.consoleOutput.append(line + "\n"));
            }
        } catch (Exception exception) {
            System.setOut(this.realStdout);
            System.setErr(this.realStdout);

            exception.printStackTrace();
        }
    }
}
