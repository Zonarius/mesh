package com.gentics.mesh.changelog.changes;

import com.gentics.mesh.changelog.AbstractChange;
import com.tinkerpop.blueprints.Vertex;

/**
 * Change which adds fancy moped.
 */
public class Change_424FA7436B6541269E6CE90C8C3D812D3 extends AbstractChange {

	@Override
	public String getName() {
		return "Add fancy moped2";
	}

	@Override
	public void apply() {
		Vertex meshRootVertex = getMeshRootVertex();
		Vertex mopedVertex = getGraph().addVertex("TheMoped2");
		mopedVertex.setProperty("name", "moped2");
		meshRootVertex.addEdge("HAS_MOPED2", mopedVertex);
		log.info("Added moped2");
	}

	@Override
	public String getDescription() {
		return "Some changes to the graph which add an edge and a vertex";
	}

}