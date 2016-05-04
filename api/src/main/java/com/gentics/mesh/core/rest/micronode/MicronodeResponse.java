package com.gentics.mesh.core.rest.micronode;

import com.gentics.mesh.core.rest.common.AbstractResponse;
import com.gentics.mesh.core.rest.common.FieldContainer;
import com.gentics.mesh.core.rest.common.FieldTypes;
import com.gentics.mesh.core.rest.node.FieldMap;
import com.gentics.mesh.core.rest.node.FieldMapImpl;
import com.gentics.mesh.core.rest.node.field.MicronodeField;
import com.gentics.mesh.core.rest.schema.MicroschemaReference;

/**
 * POJO for the micronode rest response model.
 */
public class MicronodeResponse extends AbstractResponse implements MicronodeField, FieldContainer {

	private MicroschemaReference microschema;

	private FieldMap fields = new FieldMapImpl();

	/**
	 * Get the microschema reference of the micronode
	 * 
	 * @return microschema reference
	 */
	public MicroschemaReference getMicroschema() {
		return microschema;
	}

	/**
	 * Set the microschema reference to the micronode
	 * 
	 * @param microschema microschema reference
	 */
	public void setMicroschema(MicroschemaReference microschema) {
		this.microschema = microschema;
	}

	@Override
	public FieldMap getFields() {
		return fields;
	}

	@Override
	public String getType() {
		return FieldTypes.MICRONODE.toString();
	}
}