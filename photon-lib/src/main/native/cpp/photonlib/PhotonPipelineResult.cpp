/*
 * MIT License
 *
 * Copyright (c) PhotonVision
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include "photonlib/PhotonPipelineResult.h"

#include "photon_types.pb.h"

namespace photonlib {
PhotonPipelineResult::PhotonPipelineResult(
    units::second_t latency, std::span<const PhotonTrackedTarget> targets)
    : latency(latency),
      targets(targets.data(), targets.data() + targets.size()) {}

bool PhotonPipelineResult::operator==(const PhotonPipelineResult& other) const {
  return latency == other.latency && targets == other.targets;
}

bool PhotonPipelineResult::operator!=(const PhotonPipelineResult& other) const {
  return !operator==(other);
}

}  // namespace photonlib

google::protobuf::Message* wpi::Protobuf<photonlib::PhotonPipelineResult>::New(
    google::protobuf::Arena* arena) {
  return google::protobuf::Arena::CreateMessage<
      photonvision::proto::ProtobufPhotonPipelineResult>(arena);
}

photonlib::PhotonPipelineResult
wpi::Protobuf<photonlib::PhotonPipelineResult>::Unpack(
    const google::protobuf::Message& msg) {
  using namespace photonlib;
  using photonvision::proto::ProtobufPhotonPipelineResult;

  auto m = static_cast<const ProtobufPhotonPipelineResult*>(&msg);

  std::vector<PhotonTrackedTarget> targets;
  targets.reserve(m->targets_size());
  for (const auto& t : m->targets()) {
    targets.emplace_back(wpi::UnpackProtobuf<PhotonTrackedTarget>(t));
  }

  // TODO -- multi-target

  return photonlib::PhotonPipelineResult{units::millisecond_t{m->latency_ms()},
                                         targets};
}

void wpi::Protobuf<photonlib::PhotonPipelineResult>::Pack(
    google::protobuf::Message* msg,
    const photonlib::PhotonPipelineResult& value) {
  using namespace photonlib;
  using photonvision::proto::ProtobufPhotonPipelineResult;
  using photonvision::proto::ProtobufPhotonTrackedTarget;

  auto m = static_cast<ProtobufPhotonPipelineResult*>(msg);

  m->set_latency_ms(units::millisecond_t(value.GetLatency()).value());

  m->clear_targets();
  for (const auto& t : value.GetTargets()) {
    wpi::PackProtobuf(m->add_targets(), t);
  }

  // TODO -- multi-target
}
