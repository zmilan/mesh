package com.gentics.mesh.core.data.fieldhandler;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeOperation.UPDATEFIELD;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.gentics.mesh.core.rest.schema.FieldSchemaContainer;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeModel;
import com.gentics.mesh.util.FieldUtil;

public abstract class AbstractComparatorListSchemaTest<C extends FieldSchemaContainer> extends AbstractSchemaComparatorTest<ListFieldSchema, C> {

	@Override
	public ListFieldSchema createField(String fieldName) {
		return FieldUtil.createListFieldSchema("test");
	}

	@Test
	@Override
	public void testSameField() throws IOException {
		C containerA = createContainer();
		C containerB = createContainer();

		ListFieldSchema fieldA = FieldUtil.createListFieldSchema("test");
		fieldA.setRequired(true);
		containerA.addField(fieldA);

		ListFieldSchema fieldB = FieldUtil.createListFieldSchema("test");
		fieldB.setRequired(true);
		containerB.addField(fieldB);

		List<SchemaChangeModel> list = getComparator().diff(containerA, containerB);
		assertThat(list).isEmpty();

	}

	@Test
	@Override
	public void testUpdateField() throws IOException {
		C containerA = createContainer();
		C containerB = createContainer();

		ListFieldSchema fieldA = FieldUtil.createListFieldSchema("test");
		fieldA.setRequired(true);
		containerA.addField(fieldA);

		ListFieldSchema fieldB = FieldUtil.createListFieldSchema("test");
		containerB.addField(fieldB);

		// required flag:
		fieldB.setRequired(false);
		List<SchemaChangeModel> changes = getComparator().diff(containerA, containerB);
		assertThat(changes).hasSize(1);
		assertThat(changes.get(0)).is(UPDATEFIELD).forField("test").hasProperty("required", false);
		assertThat(changes.get(0).getProperties()).hasSize(2);
		fieldB.setRequired(true);

		// list type:
		fieldB.setListType("boolean");
		changes = getComparator().diff(containerA, containerB);
		assertThat(changes).hasSize(1);
		assertThat(changes.get(0)).is(UPDATEFIELD).forField("test").hasProperty("listType", "boolean");
		assertThat(changes.get(0).getProperties()).hasSize(2);
	}
}
