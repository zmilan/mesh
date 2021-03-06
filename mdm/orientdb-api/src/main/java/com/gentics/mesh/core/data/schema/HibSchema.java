package com.gentics.mesh.core.data.schema;

import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.core.rest.schema.SchemaVersionModel;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;

public interface HibSchema extends HibFieldSchemaElement<SchemaResponse, SchemaVersionModel, HibSchema, HibSchemaVersion> {

	SchemaReference transformToReference();

	void deleteElement();

}
