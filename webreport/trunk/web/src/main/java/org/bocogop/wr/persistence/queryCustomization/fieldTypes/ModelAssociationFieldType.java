package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

/**
 * Represents the common functions for all enums that provide the different
 * "association" fields in another domain object; that is, they refer to an
 * external object via a OneToMany / ManyToOne JPA relationship.
 * <p>
 * We use these enum values to help optimize some queries. CPB
 */
public interface ModelAssociationFieldType {

	String getFieldName();

	Class<?> getModelClass();

}
