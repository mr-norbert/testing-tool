package bnorbert.objectdetection;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TimerTask;

public class ScreenRecorder extends TimerTask{

    private final AWTSequenceEncoder encoder;
    private final Rectangle screenDimension;
    private final Robot robot;
    /*
    private final Random random = SecureRandom.getInstanceStrong();
     */

    public ScreenRecorder(AWTSequenceEncoder sequenceEncoder, Rectangle rectangle) throws AWTException {
        encoder = sequenceEncoder;
        screenDimension = rectangle;
        robot = new Robot();
        RecordTimer.start();
    }

    @Override
    public void run() {
        BufferedImage capture = robot.createScreenCapture(screenDimension);
        try {
            detect(capture);
            encoder.encodeImage(capture);
        } catch (TranslateException | IOException | MalformedModelException | ModelNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void detect(BufferedImage image) throws TranslateException, ModelNotFoundException, MalformedModelException, IOException {
        int targetWidth = 416;
        int targetHeight = 416;

        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(targetWidth));
        pipeline.add(new ToTensor());

        Translator<ai.djl.modality.cv.Image, DetectedObjects> translator = YoloV5Translator
                .builder()
                .optSynset(Arrays.asList("target", "vehicle"))
                .setPipeline(pipeline)
                .build();

        ai.djl.modality.cv.Image img = ImageFactory
                .getInstance()
                .fromImage(image);

        Path modelDir = Paths.get("src/main/resources/build/model");
        Criteria<ai.djl.modality.cv.Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(ai.djl.modality.cv.Image.class, DetectedObjects.class)
                        .optModelPath(modelDir)
                        .optModelName("model2.torchscript.pt")
                        .optTranslator(translator)
                        .optEngine("PyTorch")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<ai.djl.modality.cv.Image, DetectedObjects> model = criteria.loadModel()) {
            try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects detection = predictor.predict(img);
                drawBoundingBoxes((BufferedImage) img.getWrappedImage(), detection);
            }
        }

    }

    //private Color getRandomColor() {
    //    return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
    //}

    private void drawBoundingBoxes(BufferedImage image, DetectedObjects detections) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        int stroke = 2;
        g.setStroke(new BasicStroke(stroke));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        image.getWidth();
        image.getHeight();

        java.util.List<DetectedObjects.DetectedObject> detectedObjects = detections.items();
        for (DetectedObjects.DetectedObject object : detectedObjects) {
            String className = object.getClassName();
            double probability = object.getProbability();
            double closest = (Math.ceil(probability * 100) /100);
            String percentage = toPercentage(closest);
            if(closest > 0.78 && object.getClassName().equals("target")){
                BoundingBox box = object.getBoundingBox();
                g.setPaint(Color.ORANGE);
                ai.djl.modality.cv.output.Rectangle rectangle = box.getBounds();
                int x = (int) (rectangle.getX());
                int y = (int) (rectangle.getY());
                g.drawRect(x, y, (int) (rectangle.getWidth()), (int) (rectangle.getHeight()));
                drawText(g, className, percentage, x, y, stroke);
            } else if(object.getClassName().equals("vehicle") && closest > 0.70) {
                BoundingBox box = object.getBoundingBox();
                g.setPaint(Color.CYAN);
                ai.djl.modality.cv.output.Rectangle rectangle = box.getBounds();
                int x = (int) (rectangle.getX());
                int y = (int) (rectangle.getY());
                g.drawRect(x, y, (int) (rectangle.getWidth()), (int) (rectangle.getHeight()));
                drawText(g, className, percentage, x, y, stroke);
            } else {
               BoundingBox box = object.getBoundingBox();
               g.setPaint(Color.GRAY);
               ai.djl.modality.cv.output.Rectangle rectangle = box.getBounds();
               int x = (int) (rectangle.getX());
               int y = (int) (rectangle.getY());
               g.drawRect(x, y, (int) (rectangle.getWidth()), (int) (rectangle.getHeight()));
               drawText(g, className, percentage, x, y, stroke);
           }
        }
        System.err.println(detectedObjects.size());
        g.dispose();
    }

    private void drawText(Graphics2D g, String text, String percentage, int x, int y, int stroke) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int ascent = metrics.getAscent();
        g.setPaint(Color.WHITE);
        g.drawString(text + " " + percentage, x + 4, y + ascent);
    }

    private String toPercentage(double value){
        return String.format("%.0f", value * 100)+"%";
    }
}
