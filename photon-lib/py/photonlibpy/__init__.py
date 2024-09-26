###############################################################################
## Copyright (C) Photon Vision.
###############################################################################
## This program is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with this program.  If not, see <https://www.gnu.org/licenses/>.
###############################################################################

# from .packet import Packet  # noqa
# from .estimatedRobotPose import EstimatedRobotPose  # noqa
# from .photonPoseEstimator import PhotonPoseEstimator, PoseStrategy  # noqa
# from .photonCamera import PhotonCamera  # noqa

# force-load native libraries
import ntcore
import wpiutil
import wpinet
import wpimath
import wpilib
import hal
import wpilib.cameraserver
import robotpy_apriltag

import platform
if platform.system().lower() == "Windows":
    import os
    os.add_dll_directory(os.path.dirname(os.path.realpath(__file__)))

from ._photonlibpy import *
