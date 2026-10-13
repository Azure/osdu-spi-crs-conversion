package org.opengroup.osdu.crs.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.v4.TrajectoryStationInV4;

import java.util.List;
public interface ITrajectoryConverter {
	ConvertTrajectoryResponse convertTrajectory(DpsHeaders headers, ConvertTrajectoryRequest request);
	ConvertTrajectoryResponseV4 convertTrajectoryV4(DpsHeaders headers, ConvertTrajectoryRequestV4 request, boolean flag_check_projected, boolean flag_check_scaleFactor, boolean isInclOnly);
	List<TrajectoryStationInV4> populateMdInclAziFromRequestV4ForInverseMinimumCurvature(ConvertTrajectoryRequestV4 request);

}
