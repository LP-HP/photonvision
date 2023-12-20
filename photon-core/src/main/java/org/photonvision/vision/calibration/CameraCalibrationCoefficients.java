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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import edu.wpi.first.math.geometry.Pose3d;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.photonvision.vision.opencv.Releasable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CameraCalibrationCoefficients implements Releasable {
    public static final class BoardObservation {
        @JsonProperty("locationInObjectSpace")
        public List<Point3> locationInObjectSpace; // Expected feature 3d location in the camera frame

        @JsonProperty("locationInImageSpace")
        public List<Point> locationInImageSpace; // Observed location in pixel space

        @JsonProperty("reprojectionErrors")
        public List<Point> reprojectionErrors; // (measured location in pixels) - (expected from FK)

        public Pose3d optimisedCameraToObject; // Solver optimized board poses

        @JsonCreator
        public BoardObservation(
                @JsonProperty("locationInObjectSpace") List<Point3> locationInObjectSpace,
                @JsonProperty("locationInImageSpace") List<Point> locationInImageSpace,
                @JsonProperty("reprojectionErrors") List<Point> reprojectionErrors,
                @JsonProperty("optimisedCameraToObject") Pose3d optimisedCameraToObject) {
            this.locationInObjectSpace = locationInObjectSpace;
            this.locationInImageSpace = locationInImageSpace;
            this.reprojectionErrors = reprojectionErrors;
            this.optimisedCameraToObject = optimisedCameraToObject;
        }
    }

    @JsonProperty("resolution")
    public final Size resolution;

    @JsonProperty("cameraIntrinsics")
    public final JsonMat cameraIntrinsics;

    @JsonProperty("cameraExtrinsics")
    @JsonAlias({"cameraExtrinsics", "distCoeffs"})
    public final JsonMat distCoeffs;

    @JsonProperty("observations")
    public final List<BoardObservation> observations;

    @JsonIgnore private final double[] intrinsicsArr = new double[9];
    @JsonIgnore private final double[] extrinsicsArr = new double[5];

    @JsonCreator
    public CameraCalibrationCoefficients(
            @JsonProperty("resolution") Size resolution,
            @JsonProperty("cameraIntrinsics") JsonMat cameraIntrinsics,
            @JsonProperty("cameraExtrinsics") JsonMat distCoeffs,
            @JsonProperty("observations") List<BoardObservation> observations) {
        this.resolution = resolution;
        this.cameraIntrinsics = cameraIntrinsics;
        this.distCoeffs = distCoeffs;

        // Legacy migration just to make sure that observations is at worst empty and never null
        if (observations == null) {
            observations = List.of();
        }
        this.observations = observations;

        // do this once so gets are quick
        getCameraIntrinsicsMat().get(0, 0, intrinsicsArr);
        getDistCoeffsMat().get(0, 0, extrinsicsArr);
    }

    @JsonIgnore
    public Mat getCameraIntrinsicsMat() {
        return cameraIntrinsics.getAsMat();
    }

    @JsonIgnore
    public MatOfDouble getDistCoeffsMat() {
        return distCoeffs.getAsMatOfDouble();
    }

    @JsonIgnore
    public double[] getIntrinsicsArr() {
        return intrinsicsArr;
    }

    @JsonIgnore
    public double[] getExtrinsicsArr() {
        return extrinsicsArr;
    }

    @JsonIgnore
    public List<BoardObservation> getPerViewErrors() {
        return observations;
    }

    @Override
    public void release() {
        cameraIntrinsics.release();
        distCoeffs.release();
    }

    public static CameraCalibrationCoefficients parseFromCalibdbJson(JsonNode json) {
        // camera_matrix is a row major, array of arrays
        var cam_matrix = json.get("camera_matrix");

        double[] cam_arr =
                new double[] {
                    cam_matrix.get(0).get(0).doubleValue(),
                    cam_matrix.get(0).get(1).doubleValue(),
                    cam_matrix.get(0).get(2).doubleValue(),
                    cam_matrix.get(1).get(0).doubleValue(),
                    cam_matrix.get(1).get(1).doubleValue(),
                    cam_matrix.get(1).get(2).doubleValue(),
                    cam_matrix.get(2).get(0).doubleValue(),
                    cam_matrix.get(2).get(1).doubleValue(),
                    cam_matrix.get(2).get(2).doubleValue()
                };

        var dist_coefs = json.get("distortion_coefficients");

        double[] dist_array =
                new double[] {
                    dist_coefs.get(0).doubleValue(),
                    dist_coefs.get(1).doubleValue(),
                    dist_coefs.get(2).doubleValue(),
                    dist_coefs.get(3).doubleValue(),
                    dist_coefs.get(4).doubleValue(),
                };

        var cam_jsonmat = new JsonMat(3, 3, cam_arr);
        var distortion_jsonmat = new JsonMat(1, 5, dist_array);

        var error = json.get("avg_reprojection_error").asDouble();
        var width = json.get("img_size").get(0).doubleValue();
        var height = json.get("img_size").get(1).doubleValue();

        return new CameraCalibrationCoefficients(
                new Size(width, height), cam_jsonmat, distortion_jsonmat, List.of());
    }
}
