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

###############################################################################
## THIS FILE WAS AUTO-GENERATED BY ./photon-serde/generate_messages.py.
##                        --> DO NOT MODIFY <--
###############################################################################

from ..targeting import *


class PhotonTrackedTargetSerde:

    # Message definition md5sum. See photon_packet.adoc for details
    MESSAGE_VERSION = "40a7416333cff2b68557a9248dd6e062"
    MESSAGE_FORMAT = "float64 yaw;float64 pitch;float64 area;float64 skew;int32 fiducialId;int32 objDetectId;float32 objDetectConf;Transform3d:d41d8cd98f00b204e9800998ecf8427e bestCameraToTarget;Transform3d:d41d8cd98f00b204e9800998ecf8427e altCameraToTarget;float64 poseAmbiguity;TargetCorner:16f6ac0dedc8eaccb951f4895d9e18b6[?] minAreaRectCorners;TargetCorner:16f6ac0dedc8eaccb951f4895d9e18b6[?] detectedCorners;"

    @staticmethod
    def unpack(packet: "Packet") -> "PhotonTrackedTarget":
        ret = PhotonTrackedTarget()

        # yaw is of intrinsic type float64
        ret.yaw = packet.decodeDouble()

        # pitch is of intrinsic type float64
        ret.pitch = packet.decodeDouble()

        # area is of intrinsic type float64
        ret.area = packet.decodeDouble()

        # skew is of intrinsic type float64
        ret.skew = packet.decodeDouble()

        # fiducialId is of intrinsic type int32
        ret.fiducialId = packet.decodeInt()

        # objDetectId is of intrinsic type int32
        ret.objDetectId = packet.decodeInt()

        # objDetectConf is of intrinsic type float32
        ret.objDetectConf = packet.decodeFloat()

        # field is shimmed!
        ret.bestCameraToTarget = packet.decodeTransform()

        # field is shimmed!
        ret.altCameraToTarget = packet.decodeTransform()

        # poseAmbiguity is of intrinsic type float64
        ret.poseAmbiguity = packet.decodeDouble()

        # minAreaRectCorners is a custom VLA!
        ret.minAreaRectCorners = packet.decodeList(TargetCorner.photonStruct)

        # detectedCorners is a custom VLA!
        ret.detectedCorners = packet.decodeList(TargetCorner.photonStruct)

        return ret


# Hack ourselves into the base class
PhotonTrackedTarget.photonStruct = PhotonTrackedTargetSerde()
