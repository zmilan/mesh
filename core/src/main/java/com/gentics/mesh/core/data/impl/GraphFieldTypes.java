package com.gentics.mesh.core.data.impl;

import java.util.List;

import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.GraphFieldContainer;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.field.BinaryGraphField;
import com.gentics.mesh.core.data.node.field.BooleanGraphField;
import com.gentics.mesh.core.data.node.field.DateGraphField;
import com.gentics.mesh.core.data.node.field.FieldTransformator;
import com.gentics.mesh.core.data.node.field.FieldUpdater;
import com.gentics.mesh.core.data.node.field.HtmlGraphField;
import com.gentics.mesh.core.data.node.field.NumberGraphField;
import com.gentics.mesh.core.data.node.field.StringGraphField;
import com.gentics.mesh.core.data.node.field.list.BooleanGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.DateGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.HtmlGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.MicronodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NumberGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.StringGraphFieldList;
import com.gentics.mesh.core.data.node.field.nesting.MicronodeGraphField;
import com.gentics.mesh.core.data.node.field.nesting.NodeGraphField;
import com.gentics.mesh.core.rest.node.field.Field;
import com.gentics.mesh.core.rest.schema.FieldSchema;
import com.gentics.mesh.core.rest.schema.FieldSchemaContainer;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;

import rx.Observable;

/**
 * List of all graph field types.
 */
public enum GraphFieldTypes {

	STRING("string", StringGraphField.STRING_TRANSFORMATOR, StringGraphField.STRING_UPDATER),

	STRING_LIST("list.string", StringGraphFieldList.STRING_LIST_TRANSFORMATOR, StringGraphFieldList.STRING_LIST_UPDATER),

	NUMBER("number", NumberGraphField.NUMBER_TRANSFORMATOR, NumberGraphField.NUMBER_UPDATER),

	NUMBER_LIST("list.number", NumberGraphFieldList.NUMBER_LIST_TRANSFORMATOR, NumberGraphFieldList.NUMBER_UPDATER),

	DATE("date", DateGraphField.DATE_TRANSFORMATOR, DateGraphField.DATE_UPDATER),

	DATE_LIST("list.date", DateGraphFieldList.DATE_LIST_TRANSFORMATOR, DateGraphFieldList.DATE_LIST_UPDATER),

	BOOLEAN("boolean", BooleanGraphField.BOOLEAN_TRANSFORMATOR, BooleanGraphField.BOOLEAN_UPDATER),

	BOOLEAN_LIST("list.boolean", BooleanGraphFieldList.BOOLEAN_LIST_TRANSFORMATOR, BooleanGraphFieldList.BOOLEAN_LIST_UPDATER),

	HTML("html", HtmlGraphField.HTML_TRANSFORMATOR, HtmlGraphField.HTML_UPDATER),

	HTML_LIST("list.html", HtmlGraphFieldList.HTML_LIST_TRANSFORMATOR, HtmlGraphFieldList.HTML_LIST_UPDATER),

	MICRONODE("micronode", MicronodeGraphField.MICRONODE_TRANSFORMATOR, MicronodeGraphField.MICRONODE_UPDATER),

	MICRONODE_LIST("list.micronode", MicronodeGraphFieldList.MICRONODE_LIST_TRANSFORMATOR, MicronodeGraphFieldList.MICRONODE_LIST_UPDATER),

	NODE("node", NodeGraphField.NODE_TRANSFORMATOR, NodeGraphField.NODE_UPDATER),

	NODE_LIST("list.node", NodeGraphFieldList.NODE_LIST_TRANSFORMATOR, NodeGraphFieldList.NODE_LIST_UPDATER),

	BINARY("binary", BinaryGraphField.BINARY_TRANSFORMATOR, BinaryGraphField.BINARY_UPDATER);

	private String combinedType;
	private FieldTransformator transformator;
	private FieldUpdater updater;

	private GraphFieldTypes(String combinedType, FieldTransformator transformator, FieldUpdater updater) {
		this.combinedType = combinedType;
		this.transformator = transformator;
		this.updater = updater;
	}

	/**
	 * Return the combined field type.
	 * 
	 * @return
	 */
	public String getCombinedType() {
		return combinedType;
	}

	/**
	 * Return the field specific transformator.
	 * 
	 * @return
	 */
	public FieldTransformator getTransformator() {
		return transformator;
	}

	public static GraphFieldTypes valueByFieldSchema(FieldSchema schema) {
		String combinedType = schema.getType();
		if (schema instanceof ListFieldSchema) {
			combinedType += "." + ((ListFieldSchema) schema).getListType();
		}
		for (GraphFieldTypes type : values()) {
			if (type.getCombinedType().equals(combinedType)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Convert the given field type name to a field type object.
	 * 
	 * @param name
	 * @return
	 */
	public static GraphFieldTypes valueByName(String name) {
		for (GraphFieldTypes type : values()) {
			if (type.toString().equals(name)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Invoke the type specific transformator using the provided information.
	 * 
	 * @param container
	 *            Field container which will be used to load the fields
	 * @param ac
	 * @param fieldKey
	 *            Field key
	 * @param fieldSchema
	 * @param languageTags
	 * @param level
	 *            Current level of transformation
	 * @param parentNode
	 * @return
	 */
	public Observable<? extends Field> getRestFieldFromGraph(GraphFieldContainer container, InternalActionContext ac, String fieldKey,
			FieldSchema fieldSchema, List<String> languageTags, int level, Node parentNode) {
		return getTransformator().transform(container, ac, fieldKey, fieldSchema, languageTags, level, parentNode);
	}

	public void updateField(GraphFieldContainer container, InternalActionContext ac, String fieldKey, Field restField, FieldSchema fieldSchema,
			FieldSchemaContainer schema) {
		updater.update(container, ac, fieldKey, restField, fieldSchema, schema);
	}
}
