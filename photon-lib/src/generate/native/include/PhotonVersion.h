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

#include <string.h>
#include <regex>

/*
 * Autogenerated file! Do not manually edit this file. This version is
 * regenerated any time the publish task is run, or when this file is deleted.
 */

namespace photonlib {
  const std::string versionString = "dev-v2022.1.4-1-g68abc162";
  const std::string buildDate = "2022-1-19 05:06:34";
  const bool isRelease = !versionString.rfind("dev", 0) == 0;

  bool VersionMatches(const std::string& other) {
    std::smatch match;
    std::regex versionPattern{"v[0-9]+.[0-9]+.[0-9]+"};
    // Check that both versions are in the right format
    if (std::regex_search(versionString, match, versionPattern) &&
        std::regex_search(other, match, versionPattern)) {
      // If they are, check string equality
      return (versionString == other);
    } else {
        return false;
    }
  }
}