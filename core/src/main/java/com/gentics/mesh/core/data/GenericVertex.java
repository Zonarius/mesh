package com.gentics.mesh.core.data;

import com.gentics.mesh.core.rest.common.RestModel;
import com.gentics.mesh.handler.InternalActionContext;

import rx.Observable;

public interface GenericVertex<T extends RestModel> extends MeshVertex, TransformableNode<T> {

	/**
	 * Return the type of the vertex.
	 * 
	 * @return Vertex type
	 */
	String getType();

	/**
	 * Return the creator of the vertex.
	 * 
	 * @return Creator
	 */
	User getCreator();

	/**
	 * Set the creator of the vertex.
	 * 
	 * @param user Creator
	 */
	void setCreator(User user);

	/**
	 * Return the editor of the vertex.
	 * 
	 * @return Editor
	 */
	User getEditor();

	/**
	 * Set the editor of the vertex.
	 * 
	 * @param user Editor
	 */
	void setEditor(User user);

	/**
	 * Return the timestamp on which the vertex was last updated.
	 * 
	 * @return Edit timestamp
	 */
	Long getLastEditedTimestamp();

	/**
	 * Set the timestamp on which the vertex was last updated.
	 * 
	 * @param timestamp Edit timestamp
	 */
	void setLastEditedTimestamp(long timestamp);

	/**
	 * Return the timestamp on which the vertex was created.
	 * 
	 * @return Creation timestamp
	 */
	Long getCreationTimestamp();

	/**
	 * Set the timestamp on which the vertex was created.
	 * 
	 * @param timestamp Creation timestamp
	 */
	void setCreationTimestamp(long timestamp);

	/**
	 * Update the vertex using the action context information.
	 * 
	 * @param ac
	 */
	Observable<Void> update(InternalActionContext ac);

	/**
	 * Set the editor and creator references and update the timestamps for created and edited fields.
	 * 
	 * @param user Creator
	 */
	void setCreated(User user);

}
