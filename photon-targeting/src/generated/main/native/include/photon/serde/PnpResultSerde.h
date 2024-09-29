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

#pragma once

// THIS std::FILE WAS AUTO-GENERATED BY ./photon-serde/generate_messages.py. DO
// NOT MODIFY

#include <wpi/SymbolExports.h>

// Include myself
#include "photon/dataflow/structures/Packet.h"
#include "photon/targeting/PnpResult.h"

// Includes for dependant types
#include <frc/geometry/Transform3d.h>
#include <stdint.h>

namespace photon {

template <>
struct WPILIB_DLLEXPORT SerdeType<PnpResult> {
  static constexpr std::string_view GetSchemaHash() {
    return "ae4d655c0a3104d88df4f5db144c1e86";
  }

  static constexpr std::string_view GetSchema() {
    return "Transform3d best;Transform3d alt;float64 bestReprojErr;float64 "
           "altReprojErr;float64 ambiguity;";
  }

  static photon::PnpResult Unpack(photon::Packet& packet);
  static void Pack(photon::Packet& packet, const photon::PnpResult& value);
};

static_assert(photon::PhotonStructSerializable<photon::PnpResult>);

}  // namespace photon
