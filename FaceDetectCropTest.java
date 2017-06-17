
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetectCropTest extends JFrame {
//CAMERA

    private JPanel cameraPane = new JPanel();
    public static DaemonThread myThread = null;
    int count = 0;
    public static VideoCapture webSource = null;
    Mat fm = new Mat();
    private Rect rectCrop = null;// crop face initialization..............
    int x = 20, y = 25;
    MatOfByte mem = new MatOfByte();
    CascadeClassifier faceDetector = new CascadeClassifier(FaceDetectCropTest.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    MatOfRect faceDetections = new MatOfRect();

    

    public class DaemonThread implements Runnable {

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
                                  rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);// crop only face

                            }
                            Highgui.imencode(".bmp", fm, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;
                            if (g.drawImage(buff, 0, 0, getWidth(), getHeight() - 150, 0, 0, buff.getWidth(), buff.getHeight(), null)) {
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
//CAMERA

    private JButton btn = new JButton("Start");
    private JButton btnCompare = new JButton("Recognize");
    public static JPanel mainPanel = new JPanel();

    private BufferedImage image_1, image_2;
    private boolean image_1_load_status = false, image_2_load_status = false;
    private JLabel label_camera = new JLabel("You should recognize here");
    private JLabel label_start = new JLabel("Press start button please");

    public FaceDetectCropTest() throws IOException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        System.out.println(RightPanel.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));

        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.getHSBColor(300, 300, 300));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredSoftBevelBorder(), ""));

        cameraPane.setLayout(null);
        cameraPane.setBounds(20, 16, 383, 280);
        cameraPane.setBackground(Color.BLACK);
        cameraPane.setForeground(Color.red);
        cameraPane.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        mainPanel.add(cameraPane);

        label_camera.setBounds(25, 130, 400, 30);
        label_camera.setFont(new Font("Times New Roman", Font.BOLD, 30));
        label_camera.setForeground(Color.red);
        label_camera.setVisible(true);
        cameraPane.add(label_camera);

        label_start.setBounds(95, 170, 400, 20);
        label_start.setFont(new Font("Times New Roman", Font.BOLD, 20));
        label_start.setForeground(Color.red);
        label_start.setVisible(true);
        cameraPane.add(label_start);

        btn.setBounds(95, 315, 114, 60);
        btn.setVisible(true);
        btn.setFocusPainted(false);
        btn.setToolTipText("Press to start");
        btn.setBackground(Color.getHSBColor(100, 100, 900));
        btn.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        mainPanel.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                label_camera.setVisible(false);
                label_start.setVisible(false);
                btn.setEnabled(false);
                btnCompare.setVisible(true);
                btnCompare.setEnabled(true);

                webSource = new VideoCapture(0); // video capture from default cam
                myThread = new FaceDetectCropTest.DaemonThread(); //create object of threat class
                Thread t = new Thread(myThread);
                t.setDaemon(true);
                myThread.runnable = true;
                t.start();

            }
        });

        btnCompare.setBounds(225, 315, 114, 60);
        btnCompare.setVisible(true);
        btnCompare.setEnabled(false);
        btnCompare.setFocusPainted(false);
        btnCompare.setToolTipText("Press to recognize");
        btnCompare.setBackground(Color.getHSBColor(100, 100, 900));
        btnCompare.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        mainPanel.add(btnCompare);
        btnCompare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Mat crop = new Mat(fm, rectCrop); //crop only face which get into rectangle........
                Highgui.imwrite("D:\\database_opencv\\me.png", crop);

                try {

                    image_2 = ImageIO.read(new File("D:\\database_opencv\\.png"));
                    image_2_load_status = true;
                    image_2 = img_resize(image_2);//this method created under below as img_resize

                } catch (IOException e1) {

                }

                try {
                    image_1 = ImageIO.read(new File("D:\\database_opencv\\me.png"));
                    image_1_load_status = true;
                    image_1 = img_resize(image_1);//this method created under below as img_resize

                } catch (IOException e1) {

                }

                if (image_1_load_status && image_2_load_status) {

                    try {
                        compare_image(image_1, image_2);//this method created under below as compare_image
                    } catch (IOException ex) {
                        Logger.getLogger(FaceRecognizeFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {

                }

            }
        });

    }

    //Recognization
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private BufferedImage img_resize(BufferedImage img_temp) {  //Its called by above method img_resize()
        BufferedImage dimg = new BufferedImage(180, 180, img_temp.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img_temp, 0, 0, 179, 179, 0, 0, img_temp.getWidth(), img_temp.getHeight(), null);
        g.dispose();
        return dimg;
    }

    public void compare_image(BufferedImage img_1, BufferedImage img_2) throws IOException {//Its called by above method compare_image()
        Mat mat_1 = conv_Mat(img_1);
        Mat mat_2 = conv_Mat(img_2);

        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();

        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(25);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0),
                new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0),
                new Mat(), hist_2, histSize, ranges);

        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);
        Double d = new Double(res * 100);

        disp_percen(d.intValue());

    }

    void disp_percen(int d) throws IOException {

        if (d >= 99) {

            JOptionPane.showMessageDialog(null, "Welcome Login\n" + "Similarity : " + d + " %");
            FaceDetectCropTest.myThread.runnable = false;// stop thread
            FaceDetectCropTest.webSource.release(); // stop caturing fron cam

            Component comp = SwingUtilities.getRoot(this);// dispose or close this Frame
            ((Window) comp).dispose();

            new MainMenu();

        } else {
            JOptionPane.showMessageDialog(null, " Login Failed \n" + "Similarity : " + d + " %");

        }
    }

    private Mat conv_Mat(BufferedImage img) {
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        Mat mat1 = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2HSV);

        return mat1;
    }
//Recognization

    //
    public static void main(String[] args) throws IOException {
        System.load(new File("D:\\opencv - 2.4.13\\build\\java\\x64\\opencv_java2413.dll").getAbsolutePath());
        System.load(new File("D:\\Opencv -3\\opencv\\build\\java\\x64\\opencv_java300.dll").getAbsolutePath());
        System.load(new File("C:\\Users\\Toxic\\Desktop\\Java-Image-Comparing-with-OpenCV\\lib\\x64\\opencv_java246.dll").getAbsolutePath());

        FaceDetectCropTest pane = new FaceDetectCropTest();
        pane.setTitle("Admin login module");
        pane.setSize(439, 435);
        pane.setLocation(440, 110);
        pane.setVisible(true);
        pane.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

}
