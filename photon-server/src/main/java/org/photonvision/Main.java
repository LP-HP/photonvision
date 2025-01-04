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

package org.photonvision;

import edu.wpi.first.hal.HAL;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.*;
import org.opencv.core.CvType;
import org.opencv.core.Size;
import org.photonvision.common.configuration.CameraConfiguration;
import org.photonvision.common.configuration.ConfigManager;
import org.photonvision.common.configuration.NeuralNetworkModelManager;
import org.photonvision.common.dataflow.networktables.NetworkTablesManager;
import org.photonvision.common.hardware.HardwareManager;
import org.photonvision.common.hardware.OsImageVersion;
import org.photonvision.common.hardware.PiVersion;
import org.photonvision.common.hardware.Platform;
import org.photonvision.common.logging.KernelLogLogger;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.LogLevel;
import org.photonvision.common.logging.Logger;
import org.photonvision.common.logging.PvCSCoreLogger;
import org.photonvision.common.networking.NetworkManager;
import org.photonvision.common.util.TestUtils;
import org.photonvision.jni.PhotonTargetingJniLoader;
import org.photonvision.jni.RknnDetectorJNI;
import org.photonvision.mrcal.MrCalJNILoader;
import org.photonvision.raspi.LibCameraJNILoader;
import org.photonvision.server.Server;
import org.photonvision.vision.apriltag.AprilTagFamily;
import org.photonvision.vision.calibration.CameraCalibrationCoefficients;
import org.photonvision.vision.calibration.CameraLensModel;
import org.photonvision.vision.calibration.JsonMatOfDouble;
import org.photonvision.vision.camera.PVCameraInfo;
import org.photonvision.vision.opencv.CVMat;
import org.photonvision.vision.pipeline.AprilTagPipelineSettings;
import org.photonvision.vision.pipeline.CVPipelineSettings;
import org.photonvision.vision.pipeline.PipelineProfiler;
import org.photonvision.vision.processes.VisionSourceManager;
import org.photonvision.vision.target.TargetModel;

public class Main {
    public static final int DEFAULT_WEBPORT = 5800;

    private static final Logger logger = new Logger(Main.class, LogGroup.General);
    private static final boolean isRelease = PhotonVersion.isRelease;

    private static boolean isTestMode = false;
    private static boolean isSmoketest = false;
    private static Path testModeFolder = null;
    private static boolean printDebugLogs;

    private static boolean handleArgs(String[] args) throws ParseException {
        final var options = new Options();
        options.addOption("d", "debug", false, "Enable debug logging prints");
        options.addOption("h", "help", false, "Show this help text and exit");
        options.addOption(
                "t",
                "test-mode",
                false,
                "Run in test mode with 2019 and 2020 WPI field images in place of cameras");

        options.addOption("p", "path", true, "Point test mode to a specific folder");
        options.addOption(
                "i",
                "ignore-cameras",
                true,
                "Ignore cameras that match a regex. Uses camera name as provided by cscore.");
        options.addOption("n", "disable-networking", false, "Disables control device network settings");
        options.addOption(
                "c",
                "clear-config",
                false,
                "Clears PhotonVision pipeline and networking settings. Preserves log files");
        options.addOption(
                "s",
                "smoketest",
                false,
                "Exit Photon after loading native libraries and camera configs, but before starting up camera runners");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar photonvision.jar [options]", options);
            return false; // exit program
        } else {
            if (cmd.hasOption("debug")) {
                printDebugLogs = true;
                logger.info("Enabled debug logging");
            }

            if (cmd.hasOption("test-mode")) {
                isTestMode = true;
                logger.info("Running in test mode - Cameras will not be used");

                if (cmd.hasOption("path")) {
                    Path p = Path.of(System.getProperty("PATH_PREFIX", "") + cmd.getOptionValue("path"));
                    logger.info("Loading from Path " + p.toAbsolutePath().toString());
                    testModeFolder = p;
                }
            }

            if (cmd.hasOption("disable-networking")) {
                NetworkManager.getInstance().networkingIsDisabled = true;
            }

            if (cmd.hasOption("clear-config")) {
                ConfigManager.getInstance().clearConfig();
            }

            if (cmd.hasOption("smoketest")) {
                isSmoketest = true;
            }
        }
        return true;
    }

    private static void addTestModeSources() {
        ConfigManager.getInstance().load();

        CameraConfiguration camConf2025 =
                ConfigManager.getInstance().getConfig().getCameraConfigurations().get("WPI2025");
        if (camConf2025 == null || true) {
            camConf2025 =
                    new CameraConfiguration(
                            PVCameraInfo.fromFileInfo(
                                    // "/home/matt/Downloads/frc-2025-field-images/20241112_183833.jpg",
                                    "/home/matt/Downloads/frc-2025-field-images/20241112_184121.jpg",
                                    "WPI2025"));

            camConf2025.FOV = TestUtils.WPI2024Images.FOV;
            // same camera as 2023
            camConf2025.calibrations.add(new CameraCalibrationCoefficients(
                new Size(4000, 1868),
                new JsonMatOfDouble(
                    3, 3, CvType.CV_64F,
                    new double[]{
                        1000, 0, 4000/2,
                        0, 1000, 1868/2,
                        0, 0, 1
                    }
                ),
                new JsonMatOfDouble(1, 8, CvType.CV_64F, new double[]{0, 0, 0, 0, 0, 0, 0, 0}),
                new double[]{}, List.of(), new Size(), 0, CameraLensModel.LENSMODEL_OPENCV));

            var pipeline2025 = new AprilTagPipelineSettings();
            var path_split = Path.of(camConf2025.matchedCameraInfo.path()).getFileName().toString();
            pipeline2025.pipelineNickname = path_split.replace(".jpg", "");
            pipeline2025.targetModel = TargetModel.kAprilTag6p5in_36h11;
            pipeline2025.tagFamily = AprilTagFamily.kTag36h11;
            pipeline2025.inputShouldShow = true;
            pipeline2025.solvePNPEnabled = false;

            var psList2025 = new ArrayList<CVPipelineSettings>();
            psList2025.add(pipeline2025);
            camConf2025.pipelineSettings = psList2025;
        }

        var cameraConfigs = List.of(camConf2025);

        ConfigManager.getInstance().unloadCameraConfigs();
        cameraConfigs.stream().forEach(ConfigManager.getInstance()::addCameraConfiguration);
        VisionSourceManager.getInstance().registerLoadedConfigs(cameraConfigs);
    }

    public static void main(String[] args) {
        logger.info(
                "Starting PhotonVision version "
                        + PhotonVersion.versionString
                        + " on platform "
                        + Platform.getPlatformName()
                        + (Platform.isRaspberryPi() ? (" (Pi " + PiVersion.getPiVersion() + ")") : ""));

        if (OsImageVersion.IMAGE_VERSION.isPresent()) {
            logger.info("PhotonVision image version: " + OsImageVersion.IMAGE_VERSION.get());
        }

        try {
            if (!handleArgs(args)) {
                System.exit(1);
            }
        } catch (ParseException e) {
            logger.error("Failed to parse command-line options!", e);
        }

        // We don't want to trigger an exit in test mode or smoke test. This is specifically for MacOS.
        if (!(Platform.isSupported() || isSmoketest || isTestMode)) {
            logger.error("This platform is unsupported!");
            System.exit(1);
        }

        try {
            boolean success = TestUtils.loadLibraries();

            if (!success) {
                logger.error("Failed to load native libraries! Giving up :(");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.error("Failed to load native libraries!", e);
            System.exit(1);
        }
        logger.info("WPI JNI libraries loaded.");

        try {
            boolean success = PhotonTargetingJniLoader.load();

            if (!success) {
                logger.error("Failed to load native libraries! Giving up :(");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.error("Failed to load photon-targeting JNI!", e);
            System.exit(1);
        }
        logger.info("photon-targeting JNI libraries loaded.");

        if (!HAL.initialize(500, 0)) {
            logger.error("Failed to initialize the HAL! Giving up :(");
            System.exit(1);
        }

        try {
            if (Platform.isRaspberryPi()) {
                LibCameraJNILoader.forceLoad();
            }
        } catch (IOException e) {
            logger.error("Failed to load libcamera-JNI!", e);
        }
        try {
            if (Platform.isRK3588()) {
                RknnDetectorJNI.forceLoad();
            } else {
                logger.error("Platform does not support RKNN based machine learning!");
            }
        } catch (IOException e) {
            logger.error("Failed to load rknn-JNI!", e);
        }
        try {
            MrCalJNILoader.forceLoad();
        } catch (IOException e) {
            logger.warn(
                    "Failed to load mrcal-JNI! Camera calibration will fall back to opencv\n"
                            + e.getMessage());
        }

        CVMat.enablePrint(false);
        PipelineProfiler.enablePrint(false);

        var logLevel = printDebugLogs ? LogLevel.TRACE : LogLevel.DEBUG;
        Logger.setLevel(LogGroup.Camera, logLevel);
        Logger.setLevel(LogGroup.WebServer, logLevel);
        Logger.setLevel(LogGroup.VisionModule, logLevel);
        Logger.setLevel(LogGroup.Data, logLevel);
        Logger.setLevel(LogGroup.Config, logLevel);
        Logger.setLevel(LogGroup.General, logLevel);
        logger.info("Logging initialized in debug mode.");

        // Add Linux kernel log->Photon logger
        KernelLogLogger.getInstance();

        // Add CSCore->Photon logger
        PvCSCoreLogger.getInstance();

        logger.debug("Loading ConfigManager...");
        ConfigManager.getInstance().load(); // init config manager
        ConfigManager.getInstance().requestSave();

        logger.info("Loading ML models...");
        var modelManager = NeuralNetworkModelManager.getInstance();
        modelManager.extractModels(ConfigManager.getInstance().getModelsDirectory());
        modelManager.discoverModels(ConfigManager.getInstance().getModelsDirectory());

        logger.debug("Loading HardwareManager...");
        // Force load the hardware manager
        HardwareManager.getInstance();

        logger.debug("Loading NetworkManager...");
        NetworkManager.getInstance().reinitialize();

        logger.debug("Loading NetworkTablesManager...");
        NetworkTablesManager.getInstance()
                .setConfig(ConfigManager.getInstance().getConfig().getNetworkConfig());
        NetworkTablesManager.getInstance().registerTimedTasks();

        if (isSmoketest) {
            logger.info("PhotonVision base functionality loaded -- smoketest complete");
            System.exit(0);
        }

        // todo - should test mode just add test mode sources, but still allow local usb cameras to be
        // added?
        if (!isTestMode) {
            logger.debug("Loading VisionSourceManager...");
            VisionSourceManager.getInstance()
                    .registerLoadedConfigs(
                            ConfigManager.getInstance().getConfig().getCameraConfigurations().values());
        } else {
            if (testModeFolder == null) {
                addTestModeSources();
            }
        }

        VisionSourceManager.getInstance().registerTimedTasks();

        logger.info("Starting server...");
        HardwareManager.getInstance().setRunning(true);
        Server.initialize(DEFAULT_WEBPORT);
    }
}
