package com.gentics.mesh.core.data.job;

import com.gentics.mesh.core.data.HibCoreElement;
import com.gentics.mesh.core.data.branch.HibBranch;
import com.gentics.mesh.core.data.user.HibUser;
import com.gentics.mesh.core.rest.job.JobType;

public interface HibJob extends HibCoreElement {

	void setBranch(HibBranch branch);

	void markAsFailed(Exception ex);

	String getErrorDetail();

	HibBranch getBranch();

	HibUser getCreator();

	JobType getType();

	String getErrorMessage();

	void remove();
}
