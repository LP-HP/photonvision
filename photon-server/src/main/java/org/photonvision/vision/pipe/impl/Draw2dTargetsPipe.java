/*
 * Copyright (C) 2020 Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.vision.pipe.impl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.common.util.ColorHelper;
import org.photonvision.vision.frame.FrameDivisor;
import org.photonvision.vision.pipe.MutatingPipe;
import org.photonvision.vision.target.TrackedTarget;

public class Draw2dTargetsPipe
        extends MutatingPipe<Pair<Mat, List<TrackedTarget>>, Draw2dTargetsPipe.Draw2dTargetsParams> {

    private List<MatOfPoint> m_drawnContours = new ArrayList<>();
    MatOfPoint tempMat = new MatOfPoint();
    private static final Logger logger = new Logger(Draw2dTargetsPipe.class, LogGroup.General);

    @Override
    protected Void process(Pair<Mat, List<TrackedTarget>> in) {
        var imageSize = Math.sqrt(in.getLeft().rows() * in.getLeft().cols());
        var textSize = params.kPixelsToText * imageSize;
        var thickness = params.kPixelsToThickness * imageSize;

        if (!params.shouldDraw) return null;

        if (!in.getRight().isEmpty()
                && (params.showCentroid
                        || params.showMaximumBox
                        || params.showRotatedBox
                        || params.showShape)) {

            var centroidColour = ColorHelper.colorToScalar(params.centroidColor);
            var maximumBoxColour = ColorHelper.colorToScalar(params.maximumBoxColor);
            var rotatedBoxColour = ColorHelper.colorToScalar(params.rotatedBoxColor);
            var shapeColour = ColorHelper.colorToScalar(params.shapeOutlineColour);

            for (int i = 0; i < (params.showMultipleTargets ? in.getRight().size() : 1); i++) {
                Point[] vertices = new Point[4];
                MatOfPoint contour = new MatOfPoint();

                if (i != 0 && !params.showMultipleTargets) {
                    break;
                }

                TrackedTarget target = in.getRight().get(i);
                RotatedRect r = target.getMinAreaRect();

                if (r == null) continue;

                m_drawnContours.forEach(Mat::release);
                m_drawnContours.clear();
                m_drawnContours = new ArrayList<>();

                r.points(vertices);
                contour.fromArray(vertices);
                m_drawnContours.add(contour);

                if (params.showRotatedBox) {
                    Imgproc.drawContours(
                            in.getLeft(),
                            m_drawnContours,
                            0,
                            rotatedBoxColour,
                            (int) Math.ceil(imageSize * params.kPixelsToBoxThickness));
                }

                if (params.showMaximumBox) {
                    Rect box = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(
                            in.getLeft(),
                            new Point(box.x, box.y),
                            new Point(box.x + box.width, box.y + box.height),
                            maximumBoxColour,
                            (int) Math.ceil(imageSize * params.kPixelsToBoxThickness));
                }

                if (params.showShape) {
                    Imgproc.drawContours(
                            in.getLeft(),
                            List.of(target.m_mainContour.mat),
                            -1,
                            shapeColour,
                            (int) Math.ceil(imageSize * params.kPixelsToBoxThickness));
                }

                if (params.showContourNumber) {
                    var center = target.m_mainContour.getCenterPoint();
                    var textPos =
                            new Point(
                                    center.x + params.kPixelsToOffset * imageSize,
                                    center.y - params.kPixelsToOffset * imageSize);

                    Imgproc.putText(
                            in.getLeft(),
                            String.valueOf(i),
                            textPos,
                            0,
                            textSize,
                            ColorHelper.colorToScalar(params.textColor),
                            (int) thickness);
                }

                if (params.showCentroid) {

                    Point centroid = target.getTargetOffsetPoint();
                    var crosshairRadius = (int) (imageSize * params.kPixelsToCentroidRadius);
                    var x = centroid.x;
                    var y = centroid.y;
                    Point xMax = new Point(x + crosshairRadius, y);
                    Point xMin = new Point(x - crosshairRadius, y);
                    Point yMax = new Point(x, y + crosshairRadius);
                    Point yMin = new Point(x, y - crosshairRadius);

                    Imgproc.line(
                            in.getLeft(),
                            xMax,
                            xMin,
                            centroidColour,
                            (int) Math.ceil(imageSize * params.kPixelsToBoxThickness));
                    Imgproc.line(
                            in.getLeft(),
                            yMax,
                            yMin,
                            centroidColour,
                            (int) Math.ceil(imageSize * params.kPixelsToBoxThickness));
                }
            }
        }

        return null;
    }

    private void divideMat(MatOfPoint src, MatOfPoint dst) {
        var hull = src.toArray();
        for (Point point : hull) {
            dividePoint(point);
        }
        dst.fromArray(hull);
    }

    /**
     * Scale a given point list by the current frame divisor. the point list is mutated!
     */
    private void dividePointList(List<Point> points) {
        for(var p: points) {
            dividePoint(p);
        }
    }

    /**
     * Scale a given point array by the current frame divisor. the point list is mutated!
     */
    private void dividePointArray(Point[] points) {
        for(var p: points) {
            dividePoint(p);
        }
    }

    private void dividePoint(Point p) {
        p.x = p.x / (double) params.divisor.value;
        p.y = p.y / (double) params.divisor.value;
    }

    public static class Draw2dTargetsParams {
        public double kPixelsToText = 0.0025;
        public double kPixelsToThickness = 0.008;
        public double kPixelsToOffset = 0.02;
        public double kPixelsToBoxThickness = 0.007;
        public double kPixelsToCentroidRadius = 0.03;
        public boolean showCentroid = true;
        public boolean showRotatedBox = true;
        public boolean showShape = false;
        public boolean showMaximumBox = true;
        public boolean showContourNumber = true;
        public Color centroidColor = Color.GREEN; // Color.decode("#ff5ebf");
        public Color rotatedBoxColor = Color.BLUE;
        public Color maximumBoxColor = Color.RED;
        public Color shapeOutlineColour = Color.MAGENTA;
        public Color textColor = Color.GREEN;

        public final boolean showMultipleTargets;
        public final boolean shouldDraw;

        public final FrameDivisor divisor;

        public Draw2dTargetsParams(boolean shouldDraw, boolean showMultipleTargets, FrameDivisor divisor) {
            this.shouldDraw = shouldDraw;
            this.showMultipleTargets = showMultipleTargets;
            this.divisor = divisor;
        }
    }
}
