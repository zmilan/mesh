package com.gentics.mesh.search.raw;


import static com.gentics.mesh.test.TestSize.FULL;
import static com.gentics.mesh.test.context.ElasticsearchTestMode.CONTAINER_ES6;

import org.junit.Test;

import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

@MeshTestSetting(elasticsearch = CONTAINER_ES6, startServer = true, testSize = FULL)
public class MicroschemaRawSearchEndpointTest extends AbstractMeshTest {

	@Test
	public void testRawSearch() {
		// TODO Not yet implemented
	}
}
