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

#include <frc/geometry/Transform3d.h>
#include <wpi/protobuf/Protobuf.h>

namespace photon {

class PNPResult {
 public:
  // This could be wrapped in an std::optional, but chose to do it this way to
  // mirror Java
  bool isPresent;

  frc::Transform3d best;
  double bestReprojErr;

  frc::Transform3d alt;
  double altReprojErr;

  double ambiguity;

  // Apparently this can't be default-constructed? Things seem to have garbadge
  // with the defaulted empty constructor anyhow
  PNPResult()
      : isPresent{false},
        best{frc::Transform3d{}},
        bestReprojErr{0},
        alt{frc::Transform3d()},
        altReprojErr{0},
        ambiguity{0} {}

  PNPResult(frc::Transform3d best, double bestReprojErr, frc::Transform3d alt,
            double altReprojErr, double ambiguity)
      : best(best),
        bestReprojErr(bestReprojErr),
        alt(alt),
        altReprojErr(altReprojErr),
        ambiguity(ambiguity) {
    this->isPresent = true;
  }

  bool operator==(const PNPResult& other) const;
};
}  // namespace photon

template <>
struct wpi::Protobuf<photon::PNPResult> {
  static google::protobuf::Message* New(google::protobuf::Arena* arena);
  static photon::PNPResult Unpack(const google::protobuf::Message& msg);
  static void Pack(google::protobuf::Message* msg,
                   const photon::PNPResult& value);
};
