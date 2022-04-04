package bnorbert.objectdetection;

import org.jcodec.api.awt.AWTSequenceEncoder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;
import java.util.Timer;

public class ScreenRecorderFrame extends JFrame{

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        EventQueue.invokeLater(() -> {
            try {
                new ScreenRecorderFrame().setVisible(true);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
    }

    public ScreenRecorderFrame() throws NoSuchAlgorithmException {
        userInterface();
    }

    private void userInterface() {

        JButton buttonStartRecording = new JButton();
        JButton buttonStopRecording = new JButton();
        JSeparator jSeparator = new JSeparator();
        JCheckBox jCheckBox1 = new JCheckBox();
        JLabel jLabel2 = new JLabel();
        recordStateLabel = new JLabel();
        JLabel jLabel4 = new JLabel();
        recordTimeLabel = new JLabel();
        JLabel jLabel6 = new JLabel();
        labelTimer = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        buttonStartRecording.setText("Start Recording");
        buttonStartRecording.addActionListener(evt -> {
            try {
                buttonStartRecording();
            } catch (AWTException | IOException e) {
                e.printStackTrace();
            }
        });

        buttonStopRecording.setText("Stop Recording");
        buttonStopRecording.addActionListener(evt -> {
            try {
                buttonStopRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        jCheckBox1.setText("5s delay");
        jLabel2.setText("Recording state : ");
        recordStateLabel.setText("Pending");
        jLabel4.setText("Record time : ");
        recordTimeLabel.setText("0 min");
        jLabel6.setText("Timer");
        labelTimer.setText("0");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator, GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonStopRecording)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonStartRecording)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
                                                .addComponent(jCheckBox1)
                                                .addGap(45, 45, 45))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel2)
                                                        .addComponent(jLabel4)
                                                        .addComponent(jLabel6))
                                                .addGap(23, 23, 23)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(recordStateLabel)
                                                        .addComponent(recordTimeLabel)
                                                        .addComponent(labelTimer))
                                                .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonStartRecording)
                                        .addComponent(jCheckBox1))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonStopRecording)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(recordStateLabel))
                                .addGap(23, 23, 23)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(recordTimeLabel))
                                .addGap(23, 23, 23)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(labelTimer))
                                .addContainerGap(50, Short.MAX_VALUE))
        );

        pack();
    }

    private final Random random = SecureRandom.getInstanceStrong();
    private JLabel labelTimer;
    private JLabel recordStateLabel;
    private JLabel recordTimeLabel;
    private transient Timer timer;
    private transient Timer timerCount;
    private transient Rectangle rectangle;
    private transient AWTSequenceEncoder encoder;
    private transient ScreenRecorder screenRecorder;
    private transient TimerCount timeInSec;
    boolean isRecording = false;

    private void buttonStopRecording() throws IOException {
        if (isRecording) {
            stopScreenRecording();
        }
        isRecording = false;
    }

    private void buttonStartRecording() throws AWTException, IOException {
        saveOutputFile();
        scheduleTimerTasks();
    }

    private void saveOutputFile() throws IOException {
        rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        String videoName = generateRandomString();
        File output = new File(System.getProperty("user.home") + "/Desktop/video" + LocalDate.now() + videoName + ".mp4");
        encoder = AWTSequenceEncoder.createSequenceEncoder(output, 2);
    }

    private String generateRandomString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return this.random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private void scheduleTimerTasks() throws AWTException {
        isRecording = true;
        int delay = 1000 / 24;
        RecordTimer.reset();
        timer = new Timer("Thread TimerRecord");
        timerCount = new Timer("TimerCount");
        screenRecorder = new ScreenRecorder(encoder, rectangle);
        timeInSec = new TimerCount(labelTimer);
        timer.scheduleAtFixedRate(screenRecorder, 0, delay);
        timerCount.scheduleAtFixedRate(timeInSec, 0, 1000);
        recordStateLabel.setText("Recorder started...");

    }

    private void stopScreenRecording() throws IOException {
        RecordTimer.stop();
        recordStateLabel.setText("Recording stopped");
        recordTimeLabel.setText("" + RecordTimer.getTimeInSec());
        timerCount.cancel();
        timerCount.purge();
        timer.cancel();
        timer.purge();
        screenRecorder.cancel();
        timeInSec.cancel();
        encoder.finish();
        recordStateLabel.setText("Recorder stopped...");
        recordTimeLabel.setText("" + RecordTimer.getTimeInMin() + "min");
    }
}
