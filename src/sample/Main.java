package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Date;
import java.util.Random;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("МиАПР Лаб.2");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        Controller controller = fxmlLoader.getController();
        Canvas canvas = new Canvas(controller.canvasArea.getWidth(), controller.canvasArea.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTSKYBLUE);
        gc.setStroke(Color.BLACK);
        controller.canvasArea.getChildren().add(canvas);


        gc.strokeLine(0, 0, 200, 200);

        Form1 form1 = new Form1(gc, controller);


        controller.buildButton.setOnAction(e -> form1.button1_Click());

    }


    public static void main(String[] args) {
        launch(args);
    }

    public class Form1 {

        private int MIN_POINTS_COUNT = 10;
        private int MAX_POINTS_COUNT = 100000;
        private int MIN_CLASS_COUNT = 2;
        private int MAX_CLASS_COUNT = 128;
        private int POINTS_WIDTH = 1;
        private int KERNELS_WIDTH = 10;
        private int INTEND = 5;    //Border width
        private int MINAVERAGESQUAREDISTANCE = 5000;
        private int PRECISION1 = 500;    //Точность 1
        private int MAX_RGB_COMPONENT_VALUE = 256;
        private int PRECISION2 = 100;    //Точность 2

        private class Point {
            int x;
            int y;
            int class_;
        }

        private class ClassDistance {
            int class_;
            double distance;
        }

        private boolean isMaximin;
        private int points_count, class_count;
        private Color[] colors = new Color[MAX_CLASS_COUNT];
        private Point[] points = new Point[MAX_POINTS_COUNT];
        private Point[] oldKernels = new Point[MAX_CLASS_COUNT];
        private Point[] newKernels = new Point[MAX_CLASS_COUNT];
        private Point[][] PointsInClasses = new Point[MAX_CLASS_COUNT][MAX_POINTS_COUNT];
        private int[] CountPointsInClasses = new int[MAX_CLASS_COUNT];
        private double sumPairDistance = 0;
        private int pairCount = 0;

        GraphicsContext gc;
        Controller controller;

        Form1(GraphicsContext gc, Controller controller) {
            Random rand = new Random();

            colors[0] = Color.WHITE;
            colors[1] = Color.YELLOW;
            colors[2] = Color.RED;
            colors[3] = Color.LIME;
            colors[4] = Color.BLUE;
            colors[5] = Color.AQUA;
            colors[6] = Color.FUCHSIA;
            colors[7] = Color.ORANGE;
            colors[8] = Color.GREEN;
            colors[9] = Color.GRAY;
            for (int i = 10; i < MAX_CLASS_COUNT; i++) {
                colors[i] = newRGBcolor(
                        rand.nextInt(MAX_RGB_COMPONENT_VALUE),
                        rand.nextInt(MAX_RGB_COMPONENT_VALUE),
                        rand.nextInt(MAX_RGB_COMPONENT_VALUE));
            }
            this.gc = gc;
            this.controller = controller;
            clearForm();
        }

        private Color newRGBcolor(int r, int g, int b) {
            return Color.rgb(r, g, b);
        }

        private void clearForm() {
            clearImage();
            isMaximin = true;
            controller.pointsCount.setText("100000");
            controller.classCount.setText("0");
            points_count = Integer.valueOf(controller.pointsCount.getText());
            class_count = Integer.valueOf(controller.classCount.getText());
        }

        private void clearImage() {
            gc.clearRect(0, 0, controller.canvasArea.getWidth(), controller.canvasArea.getHeight());
        }

        // TODO: Проверка вводимых значений!
//        private void textBox1_KeyPress(object sender, KeyPressEventArgs e) {
//            if ((e.KeyChar < '0' || e.KeyChar > '9') && e.KeyChar != 8)
//                e.KeyChar = '\0';
//        }

        private double EvklidDistance(Point a, Point b) {
            return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        }

        private int min(int a, int b) {
            if (a < b)
                return a;
            else
                return b;
        }

        private void pointsClassification() {
            for (int i = 0; i < class_count; i++) {
                oldKernels[i] = newKernels[i];
                CountPointsInClasses[i] = 0;
            }

            for (int i = 0; i < points_count; i++) {
                ClassDistance[] distances = new ClassDistance[class_count];
                ClassDistance min = new ClassDistance();

                for (int j = 0; j < class_count; j++) {
                    distances[j].class_ = j;
                    distances[j].distance = EvklidDistance(points[i], newKernels[j]);
                }

                min.class_ = distances[0].class_;
                min.distance = distances[0].distance;
                for (int k = 1; k < class_count; k++)
                    if (distances[k].distance < min.distance) {
                        min.class_ = distances[k].class_;
                        min.distance = distances[k].distance;
                    }
                points[i].class_ = min.class_;
                CountPointsInClasses[points[i].class_]++;
            }
        }

        private void pointsDistributionIntoClasses() {
            for (int i = 0; i < class_count; i++) {
                int tempCountPoints = 0;

                for (int j = 0; j < points_count; j++)
                    if (points[j].class_ == i) {
                        PointsInClasses[i][tempCountPoints] = points[j];
                        tempCountPoints++;
                    }
            }
        }

        public void button1_Click() {
            if (isMaximin) {
                if (class_count == 0) {
                    Random rand = new Random();

                    points_count = Integer.valueOf(controller.classCount.getText());
                    if (points_count < MIN_POINTS_COUNT)
                        points_count = MIN_POINTS_COUNT;
                    if (points_count > MAX_POINTS_COUNT)
                        points_count = MAX_POINTS_COUNT;
                    controller.classCount.setText(String.valueOf(points_count));
                    for (int i = 0; i < points_count; i++) {
                        points[i].x = INTEND + rand.nextInt((int) (controller.canvasArea.getWidth() - 2 * INTEND));
                        points[i].y = INTEND + rand.nextInt((int) (controller.canvasArea.getHeight() - 2 * INTEND));
                        points[i].class_ = 0;

                        gc.setFill(colors[points[i].class_]);
                        gc.fillRect(points[i].x, points[i].y, POINTS_WIDTH, POINTS_WIDTH);
                    }

                    oldKernels[class_count] = newKernels[class_count] = points[rand.nextInt(points_count)];
                    oldKernels[class_count].class_ = newKernels[class_count].class_ = class_count;
//                    bufferedGraphics.Graphics.FillEllipse(
//                            new SolidBrush(colors[newKernels[class_count].class_]),
//                            newKernels[class_count].x - KERNELS_WIDTH / 2,
//                            newKernels[class_count].y - KERNELS_WIDTH / 2,
//                            KERNELS_WIDTH, KERNELS_WIDTH);
                }

                if (class_count > 0) {
                    double[] maxDistances = new double[class_count];
                    int[] newKernelIndexes = new int[class_count];
                    double maxDistance = 0;
                    int newKernelIndex = 0;

                    for (int i = 0; i < class_count; i++) {
                        double maxDistanceInClass = 0;
                        int newKernelIndexInClass = 0;

                        for (int j = 0; j < points_count; j++)
                            if (points[j].class_ == i)
                                if (EvklidDistance(points[j], newKernels[i]) > maxDistanceInClass) {
                                    maxDistanceInClass = EvklidDistance(points[j], newKernels[i]);
                                    newKernelIndexInClass = j;
                                }

                        maxDistances[i] = maxDistanceInClass;
                        newKernelIndexes[i] = newKernelIndexInClass;
                    }

                    for (int i = 0; i < class_count; i++)
                        if (maxDistances[i] > maxDistance) {
                            maxDistance = maxDistances[i];
                            newKernelIndex = newKernelIndexes[i];
                        }

                    for (int i = 0; i < class_count; i++) {
                        sumPairDistance += EvklidDistance(newKernels[i], newKernels[class_count]);
                        pairCount++;
                    }

                    if (maxDistance > sumPairDistance / (pairCount * 2)) {
                        oldKernels[class_count] = newKernels[class_count] = points[newKernelIndex];
                        oldKernels[class_count].class_ = newKernels[class_count].class_ = class_count;
//                        bufferedGraphics.Graphics.FillEllipse(new SolidBrush
//                                        (colors[newKernels[class_count].class_]),
//                                newKernels[class_count].x - KERNELS_WIDTH / 2,
//                                newKernels[class_count].y - KERNELS_WIDTH / 2,
//                                KERNELS_WIDTH, KERNELS_WIDTH);
                    } else
                        isMaximin = false;
                }

                if (isMaximin) {
                    class_count++;
                    controller.classCount.setText(String.valueOf(class_count));
                    pointsClassification();
                    pointsDistributionIntoClasses();
                    clearImage();   //Draw points with temp kernels
//                    for (int i = 0; i < points_count; i++) {
//                        bufferedGraphics.Graphics.FillRectangle(new SolidBrush(colors[points[i].class_]),
//                                points[i].x, points[i].y, POINTS_WIDTH, POINTS_WIDTH);
//                    }
//                    for (int i = 0; i < class_count; i++) {
//                        bufferedGraphics.Graphics.FillEllipse(new SolidBrush(colors[newKernels[i].class_]),
//                                newKernels[i].x - KERNELS_WIDTH / 2, newKernels[i].y - KERNELS_WIDTH / 2,
//                                KERNELS_WIDTH, KERNELS_WIDTH);
//                    }
//                    bufferedGraphics.Render();
                } else {
                    String caption = "Алгоритм Максимина закончен", message;

//                    button1.Enabled = false;
//                    button3.Enabled = true;
//                    button3.Focus();
                    message = caption + ". Количество классов - " + class_count;
//                    MessageBox.Show(message, caption, MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
            }
        }

        private void button2_Click() {
            clearForm();
//            bufferedGraphics.Render();
        }

        private void button3_Click() {
            int step = 0;

            Date start_time = new Date();
            boolean is_class_change = true;
            String caption = "Найдены наилучшие ядра", message;

            while (is_class_change) {
                step++;
                is_class_change = false;

                pointsClassification();
                pointsDistributionIntoClasses();

                for (int i = 0; i < class_count; i++)   //Search best kernels
                {
                    double MinAverageSquareDistance = MINAVERAGESQUAREDISTANCE;
                    int number = -1;

                    for (int j = 0; j < min(CountPointsInClasses[i], PRECISION1); j++) {
                        double SumSquareDistance = 0, TempAverageSquareDistances;

                        for (int k = 0; k < min(CountPointsInClasses[i], PRECISION2); k++)
                            SumSquareDistance += Math.pow(EvklidDistance(PointsInClasses[i][j],
                                    PointsInClasses[i][k]), 2);
                        TempAverageSquareDistances = Math.sqrt(SumSquareDistance / CountPointsInClasses[i]);
                        if (TempAverageSquareDistances < MinAverageSquareDistance)   //Best kernel was found
                        {
                            MinAverageSquareDistance = TempAverageSquareDistances;
                            number = j;
                        }
                    }
                    if (number != -1)
                        newKernels[i] = PointsInClasses[i][number];
                }

                for (int i = 0; i < class_count; i++)
                    if ((oldKernels[i].x != newKernels[i].x) || (oldKernels[i].y != newKernels[i].y))
                        is_class_change = true;

                clearImage();   //Draw points with new kernels
//                for (int i = 0; i < points_count; i++) {
//                    bufferedGraphics.Graphics.FillRectangle(new SolidBrush(colors[points[i].class_]),
//                            points[i].x, points[i].y, POINTS_WIDTH, POINTS_WIDTH);
//                }
//                for (int i = 0; i < class_count; i++) {
//                    bufferedGraphics.Graphics.FillEllipse(new SolidBrush(colors[newKernels[i].class_]),
//                            newKernels[i].x - KERNELS_WIDTH / 2, newKernels[i].y - KERNELS_WIDTH / 2,
//                            KERNELS_WIDTH, KERNELS_WIDTH);
//                }
//                bufferedGraphics.Render();
            }

            Date end_time = new Date();

//            message = caption + " за " + (end_time - start_time).Minutes.ToString() + " мин " +
//                    (end_time - start_time).Seconds.ToString() + " с " +
//                    (end_time - start_time).Milliseconds.ToString() + " мс.";
//            caption += '!';
//            MessageBox.Show(message, caption, MessageBoxButtons.OK, MessageBoxIcon.Information);
//
//            button3.Enabled = false;
        }
    }
}
