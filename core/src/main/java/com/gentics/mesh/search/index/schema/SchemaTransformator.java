package com.gentics.mesh.search.index.schema;

import static com.gentics.mesh.search.index.MappingHelper.ANALYZED;
import static com.gentics.mesh.search.index.MappingHelper.DESCRIPTION_KEY;
import static com.gentics.mesh.search.index.MappingHelper.NAME_KEY;
import static com.gentics.mesh.search.index.MappingHelper.NOT_ANALYZED;
import static com.gentics.mesh.search.index.MappingHelper.STRING;
import static com.gentics.mesh.search.index.MappingHelper.fieldType;

import org.springframework.stereotype.Component;

import com.gentics.mesh.core.data.schema.SchemaContainer;
import com.gentics.mesh.search.index.AbstractTransformator;

import io.vertx.core.json.JsonObject;

public class SchemaTransformator extends AbstractTransformator<SchemaContainer> {

	@Override
	public JsonObject toDocument(SchemaContainer container) {
		JsonObject document = new JsonObject();
		document.put(NAME_KEY, container.getName());
		document.put(DESCRIPTION_KEY, container.getLatestVersion().getSchema().getDescription());
		addBasicReferences(document, container);
		return document;
	}

	@Override
	public JsonObject getMappingProperties() {
		JsonObject props = new JsonObject();
		props.put(NAME_KEY, fieldType(STRING, NOT_ANALYZED));
		props.put(DESCRIPTION_KEY, fieldType(STRING, ANALYZED));
		return props;
	}

}