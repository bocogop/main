package org.bocogop.shared.util.cache;

public class CacheNames {

	/*
	 * All these cache names need to match what's in ehcache.xml for core
	 * classes - CPB
	 */

	public static final String QUERIES_COUNTRY_DAO = "queries.countryDAO";
	public static final String QUERIES_GENDER_DAO = "queries.genderDAO";
	public static final String QUERIES_GRANTABLE_ROLE_DAO = "queries.grantableRoleDAO";
	public static final String QUERIES_PARTY_DAO = "queries.partyDAO";
	public static final String QUERIES_ROLE_DAO = "queries.roleDAO";
	public static final String QUERIES_PERMISSION_DAO = "queries.permissionDAO";
	public static final String QUERIES_STATE_DAO = "queries.stateDAO";
	public static final String QUERIES_VA_PRECINCT_DAO = "queries.precinctDAO";

	/* All these cache names need to match what's in ehcache.xml - CPB */

	public static final String QUERIES_EXCLUDED_ENTITY_DAO_TOTAL_AND_FILTERED = "queries.excludedEntityDAO.totalAndFiltered";
	public static final String QUERIES_EXCLUSION_TYPE_DAO = "queries.exclusionTypeDAO";
	public static final String QUERIES_PRECINCT_DAO = "queries.precinctDAO";
	public static final String QUERIES_PRECINCT_AND_VISN_DAO = "queries.precinctAndVisnDAO";
	public static final String QUERIES_VISTA_STATION_DATA_DAO = "queries.vistaStationDataDAO";

	public static final String AJAX_VIEWS = "ajaxViews";
}
