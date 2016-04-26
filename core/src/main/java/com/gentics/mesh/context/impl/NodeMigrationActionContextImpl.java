package com.gentics.mesh.context.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.gentics.mesh.context.AbstractInternalActionContext;
import com.gentics.mesh.core.data.MeshAuthUser;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Release;
import com.gentics.mesh.core.link.WebRootLinkReplacer.Type;
import com.gentics.mesh.query.impl.PagingParameter;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;

/**
 * Action context implementation which will be used within the node migration.
 */
public class NodeMigrationActionContextImpl extends AbstractInternalActionContext {

	private Map<String, Object> data;

	private String body;

	private String query;

	private List<String> languageTags;

	private Project project;

	private Release release;

	/**
	 * Set the body.
	 *
	 * @param body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Set the query.
	 *
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Set the language tags.
	 *
	 * @param languageTags
	 */
	public void setLanguageTags(List<String> languageTags) {
		this.languageTags = languageTags;
	}

	@Override
	public Map<String, Object> data() {
		if (data == null) {
			data = new HashMap<>();
		}
		return data;
	}

	@Override
	public String query() {
		return query;
	}

	@Override
	public String getBodyAsString() {
		return body;
	}

	@Override
	public List<String> getSelectedLanguageTags() {
		return languageTags;
	}

	@Override
	public PagingParameter getPagingParameter() {
		return PagingParameter.fromQuery(query());
	}

	@Override
	public boolean getExpandAllFlag() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getResolveLinksType() {
		return Type.OFF;
	}

	@Override
	public void setUser(MeshAuthUser user) {
		// TODO Auto-generated method stub

	}

	/**
	 * Set the project
	 * @param project
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public Project getProject() {
		return project;
	}

	public void setRelease(Release release) {
		this.release = release;
	}

	@Override
	public Release getRelease(Project project) {
		return release;
	}

	@Override
	public MeshAuthUser getUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<FileUpload> getFileUploads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultiMap requestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(String body, HttpResponseStatus statusCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

}
