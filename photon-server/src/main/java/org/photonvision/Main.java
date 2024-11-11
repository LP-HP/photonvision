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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.apache.commons.cli.*;
import org.photonvision.common.configuration.CameraConfiguration;
import org.photonvision.common.configuration.ConfigManager;
import org.photonvision.common.configuration.NeuralNetworkModelManager;
import org.photonvision.common.dataflow.networktables.NetworkTablesManager;
import org.photonvision.common.hardware.HardwareManager;
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
import org.photonvision.vision.camera.FileVisionSource;
import org.photonvision.vision.opencv.CVMat;
import org.photonvision.vision.pipeline.AprilTagPipelineSettings;
import org.photonvision.vision.pipeline.CVPipelineSettings;
import org.photonvision.vision.pipeline.PipelineProfiler;
import org.photonvision.vision.processes.VisionModule;
import org.photonvision.vision.processes.VisionModuleManager;
import org.photonvision.vision.processes.VisionSource;
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

            if (cmd.hasOption("ignore-cameras")) {
                VisionSourceManager.getInstance()
                        .setIgnoredCamerasRegex(cmd.getOptionValue("ignore-cameras"));
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

    private static void addTestModeFromFolder() {
        ConfigManager.getInstance().load();

        try {
            List<VisionSource> collectedSources =
                    Files.list(testModeFolder)
                            .filter(p -> p.toFile().isFile())
                            .map(
                                    p -> {
                                        try {
                                            CameraConfiguration camConf =
                                                    new CameraConfiguration(
                                                            p.getFileName().toString(), p.toAbsolutePath().toString());
                                            camConf.FOV = TestUtils.WPI2019Image.FOV; // Good guess?
                                            camConf.addCalibration(TestUtils.get2020LifeCamCoeffs());

                                            var pipeSettings = new AprilTagPipelineSettings();
                                            pipeSettings.pipelineNickname = p.getFileName().toString();
                                            pipeSettings.outputShowMultipleTargets = true;
                                            pipeSettings.inputShouldShow = true;
                                            pipeSettings.outputShouldShow = false;
                                            pipeSettings.solvePNPEnabled = true;

                                            var aprilTag = new AprilTagPipelineSettings();
                                            var psList = new ArrayList<CVPipelineSettings>();
                                            psList.add(aprilTag);
                                            camConf.pipelineSettings = psList;

                                            return new FileVisionSource(camConf);
                                        } catch (Exception e) {
                                            logger.error("Couldn't load image " + p.getFileName().toString(), e);
                                            return null;
                                        }
                                    })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            ConfigManager.getInstance().unloadCameraConfigs();
            VisionModuleManager.getInstance().addSources(collectedSources).forEach(VisionModule::start);
            ConfigManager.getInstance().addCameraConfigurations(collectedSources);
        } catch (IOException e) {
            logger.error("Path does not exist!");
            System.exit(1);
        }
    }

    private static void addTestModeSources() {
        ConfigManager.getInstance().load();

        CameraConfiguration camConf2024 =
                ConfigManager.getInstance().getConfig().getCameraConfigurations().get("WPI2024");
        if (camConf2024 == null || true) {

            camConf2024 =
                    new CameraConfiguration(
                            "WPI2024",
                            TestUtils.getWPIImagePath(TestUtils.WPI2024Images.kSpeakerCenter_143in)
                                    .toAbsolutePath()
                                    .toString());

            camConf2024.FOV = TestUtils.WPI2024Images.FOV;
            // same camera as 2023
            camConf2024.calibrations.add(TestUtils.get2023LifeCamCoeffs());

            var pipeline2024 = new AprilTagPipelineSettings();
            var path_split = Path.of(camConf2024.path).getFileName().toString();
            pipeline2024.pipelineNickname = path_split.replace(".jpg", "");
            pipeline2024.targetModel = TargetModel.kAprilTag6p5in_36h11;
            pipeline2024.tagFamily = AprilTagFamily.kTag36h11;
            pipeline2024.inputShouldShow = true;
            pipeline2024.solvePNPEnabled = true;

            var psList2024 = new ArrayList<CVPipelineSettings>();
            psList2024.add(pipeline2024);
            camConf2024.pipelineSettings = psList2024;
        }

        var collectedSources = new ArrayList<VisionSource>();

        var fvs2024 = new FileVisionSource(camConf2024);

        collectedSources.add(fvs2024);

        ConfigManager.getInstance().unloadCameraConfigs();
        VisionModuleManager.getInstance().addSources(collectedSources).forEach(VisionModule::start);
        ConfigManager.getInstance().addCameraConfigurations(collectedSources);
    }

    public static void main(String[] args) {
        logger.info(
                "Starting PhotonVision version "
                        + PhotonVersion.versionString
                        + " on "
                        + Platform.getPlatformName()
                        + (Platform.isRaspberryPi() ? (" (Pi " + PiVersion.getPiVersion() + ")") : ""));

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

        if (!isTestMode) {
            logger.debug("Loading VisionSourceManager...");
            VisionSourceManager.getInstance()
                    .registerLoadedConfigs(
                            ConfigManager.getInstance().getConfig().getCameraConfigurations().values());

            VisionSourceManager.getInstance().registerTimedTask();
        } else {
            if (testModeFolder == null) {
                addTestModeSources();
            } else {
                addTestModeFromFolder();
            }
        }

        logger.info("Starting server...");
        HardwareManager.getInstance().setRunning(true);
        Server.initialize(DEFAULT_WEBPORT);
    }
}
