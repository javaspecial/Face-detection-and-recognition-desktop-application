
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.avutil;
import com.googlecode.javacv.cpp.opencv_core;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Toxic
 */
public class FaceDetectorSecurityCamera extends JFrame {

    private JPanel cameraPane = new JPanel();
    private JPanel buttonPane = new JPanel();
    private JButton RECORD_VIDEO_BUTTON = new JButton("Record");
    private JPanel buttonPaneContainerPane = new JPanel();
    private JButton startButton = new JButton("Start");
    private JButton pausedButton = new JButton("Pause");
    private JButton capturebtn = new JButton("Capture");
    private JButton Video = new JButton("Switched off");

    private ImagePanel panel = new ImagePanel(new ImageIcon("images//camimage.png").getImage());

    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat fm = new Mat();
    MatOfByte mem = new MatOfByte();
    CascadeClassifier faceDetector = new CascadeClassifier(RightPanel.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    MatOfRect faceDetections = new MatOfRect();

    class DaemonThread implements Runnable {

        public volatile boolean runnable = false;

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (webSource.grab()) {
                        try {
                            webSource.retrieve(fm);
                            Graphics g = cameraPane.getGraphics();
                            faceDetector.detectMultiScale(fm, faceDetections);
                            for (Rect rect : faceDetections.toArray()) {
                                Imgproc.rectangle(fm, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                            }

                            Highgui.imencode(".bmp", fm, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;
                            if (g.drawImage(buff, 0, 0, getWidth(), getHeight(), 0, 0, buff.getWidth(), buff.getHeight(), null)) {

                                if (runnable == false) {
                                    System.out.println("Paused ..... ");
                                    this.wait();
                                }
                            }
                        } catch (Exception ex) {
                            ex.toString();
                        }
                    }
                }
            }
        }
    }

    public FaceDetectorSecurityCamera() {
        System.out.println(RightPanel.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
        JFrame.setDefaultLookAndFeelDecorated(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.DARK_GRAY);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredSoftBevelBorder(), ""));

        cameraPane.setLayout(null);
        cameraPane.setBounds(15, 10, 630, 490);
        cameraPane.setBackground(Color.BLACK);
        cameraPane.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        mainPanel.add(cameraPane);
        panel.setVisible(true);
        cameraPane.add(panel);

        buttonPane.setLayout(null);
        buttonPane.setBounds(15, 500, 630, 100);
        buttonPane.setBackground(Color.getHSBColor(300, 300, 300));
        buttonPane.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        mainPanel.add(buttonPane);

        buttonPaneContainerPane.setLayout(null);
        buttonPaneContainerPane.setBounds(150, 10, 320, 80);
        buttonPaneContainerPane.setBackground(Color.getHSBColor(700, 300, 700));
        buttonPaneContainerPane.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        buttonPane.add(buttonPaneContainerPane);

        startButton.setBounds(60, 8, 100, 30);
        startButton.setFocusPainted(false);
        startButton.setForeground(Color.BLACK);
        startButton.setBackground(Color.getHSBColor(400, 300, 100));
        startButton.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        buttonPaneContainerPane.add(startButton);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pausedButton.setText("Pause");
                panel.setVisible(false);

                webSource = new VideoCapture(0); // video capture from default cam
                myThread = new DaemonThread(); //create object of threat class
                Thread t = new Thread(myThread);
                t.setDaemon(true);
                myThread.runnable = true;
                t.start();                 //start thrad
                startButton.setEnabled(false);  // deactivate start button
                pausedButton.setEnabled(true);  //  activate stop button
                startButton.setText("Running");

            }
        });

        pausedButton.setBounds(170, 8, 100, 30);
        pausedButton.setFocusPainted(false);
        pausedButton.setForeground(Color.BLACK);
        pausedButton.setBackground(Color.getHSBColor(400, 300, 100));
        pausedButton.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        buttonPaneContainerPane.add(pausedButton);
        pausedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startButton.setText("Start");

                myThread.runnable = false;            // stop thread
                pausedButton.setEnabled(false);   // activate start button 
                startButton.setEnabled(true);     // deactivate stop button
                webSource.release(); // stop caturing fron cam
                pausedButton.setText("Paused");
            }
        });
        capturebtn.setBounds(60, 45, 100, 30);
        capturebtn.setFocusPainted(false);
        capturebtn.setForeground(Color.BLACK);
        capturebtn.setBackground(Color.getHSBColor(400, 300, 100));
        capturebtn.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        buttonPaneContainerPane.add(capturebtn);
        capturebtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (myThread.runnable == false) {
                    JOptionPane.showMessageDialog(null, "Please Start Camera Then Capture");
                } else {
                    JFileChooser chooser = new JFileChooser();
                    int returnValue = chooser.showSaveDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        Highgui.imwrite(file.getPath(), fm);
                    } else {

                        JOptionPane.showMessageDialog(null, "You Canceld!");
                    }
                }
            }
        });

//        RECORD_VIDEO_BUTTON.setBounds(5, 50, 60, 30);
//        RECORD_VIDEO_BUTTON.setFocusPainted(false);
//        RECORD_VIDEO_BUTTON.setForeground(Color.BLACK);
//        RECORD_VIDEO_BUTTON.setBackground(Color.getHSBColor(400, 300, 100));
//        RECORD_VIDEO_BUTTON.setBorder(BorderFactory.createRaisedSoftBevelBorder());
//        buttonPaneContainerPane.add(RECORD_VIDEO_BUTTON);
//        RECORD_VIDEO_BUTTON.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
        Video.setBounds(170, 45, 100, 30);
        Video.setFocusPainted(false);
        Video.setForeground(Color.BLACK);
        Video.setBackground(Color.getHSBColor(400, 300, 100));
        Video.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        buttonPaneContainerPane.add(Video);
        Video.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                myThread.runnable = false;
                webSource.release();
                panel.setVisible(true);
                startButton.setEnabled(true);
                pausedButton.setEnabled(true);
                startButton.setText("Start");
                pausedButton.setText("Pause");

            }
        });

    }

    public static void main(String args[]) {
        System.load(new File("D:\\opencv - 2.4.13\\build\\java\\x64\\opencv_java2413.dll").getAbsolutePath());
        System.load(new File("D:\\Opencv -3\\opencv\\build\\java\\x64\\opencv_java300.dll").getAbsolutePath());
        FaceDetectorSecurityCamera pane = new FaceDetectorSecurityCamera();
        pane.setTitle("Real Time Face Detection Security Camera");
        pane.setResizable(false);
        pane.setSize(668, 646);
        pane.setLocation(340, 83);
        pane.setVisible(true);

    }

    public static class ImagePanel extends JPanel {

        private Image img;

        public ImagePanel(String img) {
            this(new ImageIcon(img).getImage());
        }

        public ImagePanel(Image img) {
            this.img = img;
            Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setLayout(null);
        }

        public void paintComponent(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }
    }
}
