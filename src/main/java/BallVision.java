package frc.robot.vision;

/*
Basic vision algorithm for detecting balls (powercells)
*/

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;

public class BallVision{

//Load 6 test images, process, and write 6 output images. To-Do: Replace with camera input
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String image1 = "src/main/java/BallImage.jpg";
        String image2 = "src/main/java/BallImage2.jpg";
        String image3 = "src/main/java/BallImage3.jpg";
        String image4 = "src/main/java/BallImage4.jpg";
        String image5 = "src/main/java/BallImage5.jpg";
        String image6 = "src/main/java/BallImage6.jpg";
        Mat input1 = Imgcodecs.imread(image1);
        Mat input2 = Imgcodecs.imread(image2);
        Mat input3 = Imgcodecs.imread(image3);
        Mat input4 = Imgcodecs.imread(image4);
        Mat input5 = Imgcodecs.imread(image5);
        Mat input6 = Imgcodecs.imread(image6);
        Mat output1 = algorithm(input1);
        Mat output2 = algorithm(input2);
        Mat output3 = algorithm(input3);
        Mat output4 = algorithm(input4);
        Mat output5 = algorithm(input5);
        Mat output6 = algorithm(input6);
        Imgcodecs.imwrite("Output Image1.jpg", output1);
        Imgcodecs.imwrite("Output Image2.jpg", output2);
        Imgcodecs.imwrite("Output Image3.jpg", output3);
        Imgcodecs.imwrite("Output Image4.jpg", output4);
        Imgcodecs.imwrite("Output Image5.jpg", output5);
        Imgcodecs.imwrite("Output Image6.jpg", output6);
        System.out.println("Done");
    }

    public static Mat algorithm(Mat input) {
        // Resize
        Mat resizeInput = input;
        double resizeWidth = 320.0;
        double resizeHeight = 240.0;
        Size toScale = new Size(resizeWidth, resizeHeight);
        Mat resizedOutput = new Mat();
        Imgproc.resize(resizeInput, resizedOutput, toScale);

       // Blur
        Mat blurInput = resizedOutput;
		double blurRadius = 2.7;
        double kernelSize = 2 * (blurRadius + 0.5) + 1;
        Mat blurOutput = new Mat();
        Imgproc.blur(blurInput, blurOutput, new Size(kernelSize, kernelSize));
        
       // Convert to HSV and Threshold
        Mat hsvThresholdInput = blurOutput;
		double[] hsvThresholdHue = {20.0, 180.0};
		double[] hsvThresholdSaturation = {130.0, 255.0};
        double[] hsvThresholdValue = {0.0, 255.0};
        Mat hsvOutput = new Mat();
        Imgproc.cvtColor(hsvThresholdInput, hsvOutput, Imgproc.COLOR_BGR2HSV);
		Core.inRange(hsvOutput, new Scalar(hsvThresholdHue[0], hsvThresholdSaturation[0], hsvThresholdValue[0]),
            new Scalar(hsvThresholdHue[1], hsvThresholdSaturation[1], hsvThresholdValue[1]), hsvOutput);

        //Find and draw contours
        Imgcodecs.imwrite("HSV Output.jpg", hsvOutput);
        Mat contourInput = hsvOutput;
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        contours.clear();
        Imgproc.findContours(contourInput, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(resizedOutput, contours, -1, new Scalar(0,0,255),1);
        
        //For each contour found, draw bounding rectangle and marker at center if over a certain size

        for(MatOfPoint contour : contours) {
            double ballArea = Imgproc.contourArea(contour);

            if(ballArea>=1000){
                Rect boundRect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(resizedOutput, new Point(boundRect.x,boundRect.y), new Point(boundRect.x+boundRect.width,boundRect.y+boundRect.height), new Scalar(0,255,0),5);
                long centerx = boundRect.x + boundRect.width / 2;
                long centery = boundRect.y + boundRect.height / 2;
                System.out.println("Target found!");
                System.out.println("Center at (" + centerx + ", " + centery + ")");
                Imgproc.drawMarker(resizedOutput, new Point(centerx,centery), new Scalar(0,255,0));
            }

        }
        return resizedOutput;
    }

}