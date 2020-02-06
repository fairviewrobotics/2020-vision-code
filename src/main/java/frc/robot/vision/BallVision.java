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

    static Mat resizeInput = new Mat();
    static Size toScale = new Size();

    static Mat resizedOutput = new Mat();

    static Mat blurInput = new Mat();
    static Mat blurOutput = new Mat();

    static Size kernel = new Size();

    static Mat hsvThresholdInput = new Mat();
    static Mat hsvOutput = new Mat();

    static Mat contourInput = new Mat();

    static List<MatOfPoint> contours = new ArrayList<>();
    static Mat hierarchy = new Mat();


//Load 6 test images, process, and write 6 output images. To-Do: Replace with camera input
    public static void main(String[] args) {
        /*
        long hm_images = 18;

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        for (int i = 1; i<=hm_images; i++){
            String image= "src/test_inputs/BallImage";
            image = image + i + ".jpg";
            Mat input = Imgcodecs.imread(image);
            Mat output = algorithm(input);
            String output_fname = "src/test_outputs/Output Image " + i + ".jpg";
            Imgcodecs.imwrite(output_fname, output);
        }
        System.out.println("Done");*/
    }

    /* calculate yaw or pitch (in radians) */
    static double calcAngle(long val, long centerVal, double focalLen) {
        return Math.atan(((double)(val - centerVal)) / focalLen);
    }

    public static TargetLocation algorithm(Mat input) {
        // Resize
        resizeInput = input;
        double resizeWidth = 320.0;
        double resizeHeight = 181.3;
        toScale.width = resizeWidth;
        toScale.height = resizeHeight;
        Imgproc.resize(resizeInput, resizedOutput, toScale);

        /* calculate camera information
        double aspectDiag = Math.hypot(aspectH, aspectV);

        double fieldViewH = Math.atan(Math.tan(diagFieldView / 2.0) * (aspectH / aspectDiag)) * 2.0;
        double fieldViewV = Math.atan(Math.tan(diagFieldView / 2.0) * (aspectV / aspectDiag)) * 2.0;

        double hFocalLen = resizedOutput.width / (2.0 * Math.tan((fieldViewH / 2.0)));
        double vFocalLen = resizedOutput.height / (2.0 * Math.tan((fieldViewV / 2.0)));

         */
        // Blur
        blurInput = resizedOutput;
		double blurRadius = 2.7;//2.7;
        double kernelSize = 2 * (blurRadius + 0.5) + 1;
        blurOutput = new Mat();
        kernel.width = kernelSize;
        kernel.height = kernelSize;
        Imgproc.blur(blurInput, blurOutput, kernel);
        
        // Convert to HSV and Threshold
        hsvThresholdInput = blurOutput;
		double[] hsvThresholdHue = {20.0, 90.0};//{20.0, 180.0};
		double[] hsvThresholdSaturation = {130.0, 255.0};
        double[] hsvThresholdValue = {0.0, 255.0};
        Imgproc.cvtColor(hsvThresholdInput, hsvOutput, Imgproc.COLOR_BGR2HSV);
		Core.inRange(hsvOutput, new Scalar(hsvThresholdHue[0], hsvThresholdSaturation[0], hsvThresholdValue[0]),
            new Scalar(hsvThresholdHue[1], hsvThresholdSaturation[1], hsvThresholdValue[1]), hsvOutput);

        //Find and draw contours
        //Imgcodecs.imwrite("HSV Output.jpg", hsvOutput);
        contourInput = hsvOutput;
        contours.clear();
        Imgproc.findContours(contourInput, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(resizedOutput, contours, -1, new Scalar(0,0,255),1);
        
        //For each contour found, draw bounding rectangle and marker at center if over a certain size
        double tolerance = 0.3;
        boolean using_tolerances = false;

        long best_centerx = 0;
        long best_centery = 0;
        long largest_width = 0;
        Rect bestBoundRect = new Rect(0, 0, 0, 0);

        for(MatOfPoint contour : contours) {
            double ballArea = Imgproc.contourArea(contour);

            Rect boundRect = Imgproc.boundingRect(contour);

            boolean in_tolerance = true;
            if (using_tolerances) {
                if (boundRect.height == 0) {
                    continue;
                }

                double ratio = (double) boundRect.width / (double) boundRect.height;
                //System.out.println(ratio);
                in_tolerance = Math.abs(1 - ratio) < tolerance;

                //in_tolerance = boundRect.width >= boundRect.height;*/
            }
            if(ballArea>=100 && in_tolerance){
                Imgproc.rectangle(resizedOutput, new Point(boundRect.x,boundRect.y), new Point(boundRect.x+boundRect.width,boundRect.y+boundRect.height), new Scalar(255,0,0),5);
                long centerx = boundRect.x + boundRect.width / 2;
                long centery = boundRect.y + boundRect.height / 2;
                System.out.println("Target found!");
                System.out.println("Center at (" + centerx + ", " + centery + ")");
                System.out.println("Boundary width: " + boundRect.width);
                System.out.println("Boundary height: " + boundRect.height);
                Imgproc.drawMarker(resizedOutput, new Point(centerx,centery), new Scalar(255,0,0));

                // Determine if this is a better target
                long width = boundRect.width;
                if (width >= largest_width) {
                    best_centerx = centerx;
                    best_centery = centery;
                    largest_width = width;
                    bestBoundRect = boundRect;
                }

                /*
                double focalLen = 55.1; // estimated focal length of Edward's camera
                double ballSize = 177.8; // 7 inches in mm
                double sensorHeight = 32.0;
                // distance = (width of object)(focal length)/(Pixel width of object)
                double distance = (focalLen * ballSize * resizeWidth) / ((double)boundRect.width * sensorHeight);
                //double distance = (ballSize * focalLen) / (double)boundRect.width;
                System.out.println("Distance from Camera " + distance + " mm");
                 */
            }

        }
        Imgproc.rectangle(resizedOutput, new Point(bestBoundRect.x,bestBoundRect.y), new Point(bestBoundRect.x+bestBoundRect.width,bestBoundRect.y+bestBoundRect.height), new Scalar(0,255,0),5);
        Imgproc.drawMarker(resizedOutput, new Point(best_centerx,best_centery), new Scalar(0,255,0));

        // Calculate hFocalLen
        double aspectH = resizeWidth;
        double aspectV = resizeHeight;
        double aspectDiag = Math.hypot(aspectH, aspectV);
        double diagFieldView = Math.toRadians(68.5);

        double fieldViewH = Math.atan(Math.tan(diagFieldView / 2.0) * (aspectH / aspectDiag)) * 2.0;
        double fieldViewV = Math.atan(Math.tan(diagFieldView / 2.0) * (aspectV / aspectDiag)) * 2.0;

        double hFocalLen = resizeWidth / (2.0 * Math.tan((fieldViewH / 2.0)));
        //double vFocalLen = resizedOutput.height / (2.0 * Math.tan((fieldViewV / 2.0)));

        // Calculate yaw
        double yaw = calcAngle(best_centerx, (long)resizeWidth/2, hFocalLen);
        System.out.println(yaw);

        TargetLocation target = new TargetLocation(bestBoundRect.x, bestBoundRect.y, bestBoundRect.width, bestBoundRect.height, yaw, 0.0);
        return target;
    }

}
