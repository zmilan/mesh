package com.gentics.mesh.core.data.dao.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.Branch;
import com.gentics.mesh.core.data.Tag;
import com.gentics.mesh.core.data.dao.NodeDaoWrapper;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.user.HibUser;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.madl.traversal.TraversalResult;
import com.gentics.mesh.parameter.PagingParameters;

import dagger.Lazy;

@Singleton
public class NodeDaoWrapperImpl implements NodeDaoWrapper {
	private final Lazy<BootstrapInitializer> boot;

	@Inject
	public NodeDaoWrapperImpl(Lazy<BootstrapInitializer> boot) {
		this.boot = boot;
	}

	@Override
	public Node findByUuidGlobal(String uuid) {
		// TODO Probably wrong interface hierarchy. There is no need for this method
		throw new RuntimeException("Not implemented");
	}

	@Override
	public long computeGlobalCount() {
		// TODO Probably wrong interface hierarchy. There is no need for this method
		throw new RuntimeException("Not implemented");
	}

	@Override
	public NodeResponse transformToRestSync(Node element, InternalActionContext ac, int level, String... languageTags) {
		return element.transformToRestSync(ac, level, languageTags);
	}

	@Override
	public void addTag(Node node, Tag tag, Branch branch) {
		node.addTag(tag, branch);
	}

	@Override
	public void removeTag(Node node, Tag tag, Branch branch) {
		node.removeTag(tag, branch);
	}

	@Override
	public void removeAllTags(Node node, Branch branch) {
		node.removeAllTags(branch);
	}

	@Override
	public TraversalResult<Tag> getTags(Node node, Branch branch) {
		return node.getTags(branch);
	}

	@Override
	public TransformablePage<Tag> getTags(Node node, HibUser user, PagingParameters params, Branch branch) {
		return node.getTags(user, params, branch);
	}

	@Override
	public boolean hasTag(Node node, Tag tag, Branch branch) {
		return node.hasTag(tag, branch);
	}
}
