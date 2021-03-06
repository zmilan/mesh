package com.gentics.mesh.core.data.job.impl;

import static com.gentics.mesh.core.data.perm.InternalPermission.READ_PERM;
import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_JOB;
import static com.gentics.mesh.core.rest.error.Errors.error;
import static com.gentics.mesh.core.rest.job.JobStatus.COMPLETED;
import static com.gentics.mesh.core.rest.job.JobStatus.FAILED;
import static com.gentics.mesh.core.rest.job.JobStatus.QUEUED;
import static com.gentics.mesh.core.rest.job.JobStatus.UNKNOWN;
import static com.gentics.mesh.madl.index.EdgeIndexDefinition.edgeIndex;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.NotImplementedException;

import com.gentics.madl.index.IndexHandler;
import com.gentics.madl.type.TypeHandler;
import com.gentics.mesh.context.BulkActionContext;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.MeshVertex;
import com.gentics.mesh.core.data.branch.HibBranch;
import com.gentics.mesh.core.data.generic.MeshVertexImpl;
import com.gentics.mesh.core.data.job.HibJob;
import com.gentics.mesh.core.data.job.Job;
import com.gentics.mesh.core.data.job.JobRoot;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.page.impl.DynamicTransformablePageImpl;
import com.gentics.mesh.core.data.project.HibProject;
import com.gentics.mesh.core.data.root.impl.AbstractRootVertex;
import com.gentics.mesh.core.data.schema.HibMicroschemaVersion;
import com.gentics.mesh.core.data.schema.HibSchemaVersion;
import com.gentics.mesh.core.data.user.HibUser;
import com.gentics.mesh.core.db.Tx;
import com.gentics.mesh.core.rest.job.JobStatus;
import com.gentics.mesh.core.rest.job.JobType;
import com.gentics.mesh.core.result.Result;
import com.gentics.mesh.event.EventQueueBatch;
import com.gentics.mesh.parameter.PagingParameters;
import com.syncleus.ferma.FramedGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import io.reactivex.Completable;

/**
 * @see JobRoot
 */
public class JobRootImpl extends AbstractRootVertex<Job> implements JobRoot {

	public static void init(TypeHandler type, IndexHandler index) {
		type.createVertexType(JobRootImpl.class, MeshVertexImpl.class);
		index.createIndex(edgeIndex(HAS_JOB).withInOut().withOut());
	}

	@Override
	public Class<? extends Job> getPersistanceClass() {
		return JobImpl.class;
	}

	@Override
	public String getRootLabel() {
		return HAS_JOB;
	}

	/**
	 * Find the element with the given uuid.
	 * 
	 * @param uuid
	 *            Uuid of the element to be located
	 * @return Found element or null if the element could not be located
	 */
	public Job findByUuid(String uuid) {
		FramedGraph graph = Tx.get().getGraph();
		// 1. Find the element with given uuid within the whole graph
		Iterator<Vertex> it = db().getVertices(MeshVertexImpl.class, new String[] { "uuid" }, new String[] { uuid });
		if (it.hasNext()) {
			Vertex potentialElement = it.next();
			// 2. Use the edge index to determine whether the element is part of this root vertex
			Iterable<Edge> edges = graph.getEdges("e." + getRootLabel().toLowerCase() + "_inout",
				db().createComposedIndexKey(potentialElement.getId(), id()));
			if (edges.iterator().hasNext()) {
				// Don't frame explicitly since multiple types can be returned
				return graph.frameElement(potentialElement, getPersistanceClass());
			}
		}
		return null;
	}

	@Override
	public Result<? extends Job> findAll() {
		// We need to enforce the usage of dynamic loading since the root->item yields different types of vertices.
		return super.findAllDynamic();
	}

	@Override
	public Job enqueueSchemaMigration(HibUser creator, HibBranch branch, HibSchemaVersion fromVersion, HibSchemaVersion toVersion) {
		NodeMigrationJobImpl job = getGraph().addFramedVertex(NodeMigrationJobImpl.class);
		job.setType(JobType.schema);
		job.setBranch(branch);
		job.setStatus(QUEUED);
		job.setFromSchemaVersion(fromVersion);
		job.setToSchemaVersion(toVersion);
		addItem(job);
		if (log.isDebugEnabled()) {
			log.debug("Enqueued schema migration job {" + job.getUuid() + "}");
		}
		return job;
	}

	@Override
	public Job enqueueMicroschemaMigration(HibUser creator, HibBranch branch, HibMicroschemaVersion fromVersion,
		HibMicroschemaVersion toVersion) {
		MicronodeMigrationJobImpl job = getGraph().addFramedVertex(MicronodeMigrationJobImpl.class);
		job.setType(JobType.microschema);
		job.setBranch(branch);
		job.setStatus(QUEUED);
		job.setFromMicroschemaVersion(fromVersion);
		job.setToMicroschemaVersion(toVersion);
		addItem(job);
		if (log.isDebugEnabled()) {
			log.debug("Enqueued microschema migration job {" + job.getUuid() + "} - " + toVersion.getSchemaContainer().getName() + " "
				+ fromVersion.getVersion() + " to " + toVersion.getVersion());
		}
		return job;
	}

	@Override
	public HibJob enqueueBranchMigration(HibUser creator, HibBranch branch, HibSchemaVersion fromVersion, HibSchemaVersion toVersion) {
		Job job = getGraph().addFramedVertex(BranchMigrationJobImpl.class);
		job.setType(JobType.branch);
		job.setBranch(branch);
		job.setStatus(QUEUED);
		job.setFromSchemaVersion(fromVersion);
		job.setToSchemaVersion(toVersion);
		addItem(job);
		if (log.isDebugEnabled()) {
			log.debug("Enqueued branch migration job {" + job.getUuid() + "} for branch {" + branch.getUuid() + "}");
		}
		return job;
	}

	@Override
	public Job enqueueBranchMigration(HibUser creator, HibBranch branch) {
		Job job = getGraph().addFramedVertex(BranchMigrationJobImpl.class);
		job.setType(JobType.branch);
		job.setStatus(QUEUED);
		job.setBranch(branch);
		addItem(job);
		if (log.isDebugEnabled()) {
			log.debug("Enqueued branch migration job {" + job.getUuid() + "} for branch {" + branch.getUuid() + "}");
		}
		return job;
	}

	@Override
	public Job enqueueVersionPurge(HibUser user, HibProject project, ZonedDateTime before) {
		VersionPurgeJobImpl job = getGraph().addFramedVertex(VersionPurgeJobImpl.class);
		// TODO Don't add the user to reduce contention
		// job.setCreated(user);
		job.setType(JobType.versionpurge);
		job.setStatus(QUEUED);
		job.setProject(project);
		job.setMaxAge(before);
		addItem(job);
		if (log.isDebugEnabled()) {
			log.debug("Enqueued project version purge job {" + job.getUuid() + "} for project {" + project.getName() + "}");
		}
		return job;
	}

	@Override
	public Job enqueueVersionPurge(HibUser user, HibProject project) {
		return enqueueVersionPurge(user, project, null);
	}

	@Override
	public MeshVertex resolveToElement(Stack<String> stack) {
		throw error(BAD_REQUEST, "Jobs are not accessible");
	}

	@Override
	public Job create(InternalActionContext ac, EventQueueBatch batch, String uuid) {
		throw new NotImplementedException("Jobs can be created using REST");
	}

	/**
	 * Find the visible elements and return a paged result.
	 * 
	 * @param ac
	 *            action context
	 * @param pagingInfo
	 *            Paging information object that contains page options.
	 * 
	 * @return
	 */
	public TransformablePage<? extends Job> findAll(InternalActionContext ac, PagingParameters pagingInfo) {
		return new DynamicTransformablePageImpl<>(ac.getUser(), this, pagingInfo, READ_PERM, null, false);
	}

	/**
	 * Find all elements and return a paged result. No permission check will be performed.
	 * 
	 * @param ac
	 *            action context
	 * @param pagingInfo
	 *            Paging information object that contains page options.
	 * 
	 * @return
	 */
	public TransformablePage<? extends Job> findAllNoPerm(InternalActionContext ac, PagingParameters pagingInfo) {
		return new DynamicTransformablePageImpl<>(ac.getUser(), this, pagingInfo, null, null, false);
	}

	@Override
	public Completable process() {
		List<Completable> actions = new ArrayList<>();
		Iterable<? extends Job> it = findAll();
		for (Job job : it) {
			try {
				// Don't execute failed or completed jobs again
				JobStatus jobStatus = job.getStatus();
				if (job.hasFailed() || (jobStatus == COMPLETED || jobStatus == FAILED || jobStatus == UNKNOWN)) {
					continue;
				}
				actions.add(job.process());
			} catch (Exception e) {
				job.markAsFailed(e);
				log.error("Error while processing job {" + job.getUuid() + "}");
			}
		}
		return Completable.concat(actions);
	}

	@Override
	public void purgeFailed() {
		log.info("Purging failed jobs..");
		Iterable<? extends JobImpl> it = out(HAS_JOB).hasNot("error", null).frameExplicit(JobImpl.class);
		long count = 0;
		for (Job job : it) {
			job.delete();
			count++;
		}
		log.info("Purged {" + count + "} failed jobs.");
	}

	@Override
	public void clear() {
		out(HAS_JOB).removeAll();
	}

	@Override
	public void delete(BulkActionContext bac) {
		throw new NotImplementedException("The job root can't be deleted");
	}

}
