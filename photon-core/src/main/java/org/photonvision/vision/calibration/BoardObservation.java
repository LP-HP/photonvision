/*
 * Copyright (C) Photon Vision.
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

package org.photonvision.vision.calibration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Pose3d;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.photonvision.common.util.ColorHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BoardObservation implements Cloneable {
    // Expected feature 3d location in the camera frame
    @JsonProperty("locationInObjectSpace")
    public List<Point3> locationInObjectSpace;

    // Observed location in pixel space
    @JsonProperty("locationInImageSpace")
    public List<Point> locationInImageSpace;

    // (measured location in pixels) - (expected from FK)
    @JsonProperty("reprojectionErrors")
    public List<Point> reprojectionErrors;

    // Solver optimized board poses
    @JsonProperty("optimisedCameraToObject")
    public Pose3d optimisedCameraToObject;

    // If we should use this observation when re-calculating camera calibration
    @JsonProperty("cornersUsed")
    public boolean[] cornersUsed;

    @JsonProperty("snapshotName")
    public String snapshotName;

    @JsonProperty("snapshotData")
    public JsonImageMat snapshotData;

    @JsonCreator
    public BoardObservation(
            @JsonProperty("locationInObjectSpace") List<Point3> locationInObjectSpace,
            @JsonProperty("locationInImageSpace") List<Point> locationInImageSpace,
            @JsonProperty("reprojectionErrors") List<Point> reprojectionErrors,
            @JsonProperty("optimisedCameraToObject") Pose3d optimisedCameraToObject,
            @JsonProperty("cornersUsed") boolean[] cornersUsed,
            @JsonProperty("snapshotName") String snapshotName,
            @JsonProperty("snapshotData") JsonImageMat snapshotData) {
        this.locationInObjectSpace = locationInObjectSpace;
        this.locationInImageSpace = locationInImageSpace;
        this.reprojectionErrors = reprojectionErrors;
        this.optimisedCameraToObject = optimisedCameraToObject;
        this.snapshotName = snapshotName;
        this.snapshotData = snapshotData;

        // legacy migration -- we assume all points are inliers
        if (cornersUsed == null) {
            cornersUsed = new boolean[locationInObjectSpace.size()];
            Arrays.fill(cornersUsed, true);
        }
        this.cornersUsed = cornersUsed;
    }

    @Override
    public String toString() {
        return "BoardObservation [locationInObjectSpace="
                + locationInObjectSpace
                + ", locationInImageSpace="
                + locationInImageSpace
                + ", reprojectionErrors="
                + reprojectionErrors
                + ", optimisedCameraToObject="
                + optimisedCameraToObject
                + ", cornersUsed="
                + cornersUsed
                + ", snapshotName="
                + snapshotName
                + ", snapshotData="
                + snapshotData
                + "]";
    }

    @Override
    public BoardObservation clone() {
        try {
            return (BoardObservation) super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Guhhh clone buh");
            return null;
        }
    }

    @JsonIgnore
    public Mat getAnnotatedImage() {
        var image = snapshotData.getAsMat().clone();
        var diag = Math.hypot(image.width(), image.height());
        int thickness = (int) Math.max(diag * 1.0 / 600.0, 1);
        int r = (int) Math.max(diag * 4.0 / 500.0, 3);
        var r2 = r / Math.sqrt(2);
        for (int i = 0; i < this.locationInImageSpace.size(); i++) {
            Scalar color;
            if (cornersUsed[i]) {
                color = ColorHelper.colorToScalar(Color.green);
            } else {
                color = ColorHelper.colorToScalar(Color.red);
            }
            var c = locationInImageSpace.get(i);
            Imgproc.circle(image, c, r, color, thickness);
            Imgproc.line(
                    image, new Point(c.x - r2, c.y - r2), new Point(c.x + r2, c.y + r2), color, thickness);
            Imgproc.line(
                    image, new Point(c.x + r2, c.y - r2), new Point(c.x - r2, c.y + r2), color, thickness);
        }
        return image;
    }
}
