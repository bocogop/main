package org.bocogop.wr.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExceptionUtils;
import org.apache.velocity.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VelocityDataSourceResourceLoader extends ResourceLoader {

	@Autowired
	private DataSource dataSource;

	private String dataSourceName;
	private String tableName;
	private String keyColumn;
	private String templateColumn;
	private String timestampColumn;

	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
	 */
	public void init(ExtendedProperties configuration) {
		dataSourceName = StringUtils.nullTrim(configuration.getString("resource.datasource"));
		tableName = StringUtils.nullTrim(configuration.getString("resource.table"));
		keyColumn = StringUtils.nullTrim(configuration.getString("resource.keycolumn"));
		templateColumn = StringUtils.nullTrim(configuration.getString("resource.templatecolumn"));
		timestampColumn = StringUtils.nullTrim(configuration.getString("resource.timestampcolumn"));

		if (dataSource != null) {
			if (log.isDebugEnabled()) {
				log.debug("DataSourceResourceLoader: using dataSource instance with table \"" + tableName + "\"");
				log.debug("DataSourceResourceLoader: using columns \"" + keyColumn + "\", \"" + templateColumn
						+ "\" and \"" + timestampColumn + "\"");
			}

			log.trace("DataSourceResourceLoader initialized.");
		} else if (dataSourceName != null) {
			if (log.isDebugEnabled()) {
				log.debug("DataSourceResourceLoader: using \"" + dataSourceName + "\" datasource with table \""
						+ tableName + "\"");
				log.debug("DataSourceResourceLoader: using columns \"" + keyColumn + "\", \"" + templateColumn
						+ "\" and \"" + timestampColumn + "\"");
			}

			log.trace("DataSourceResourceLoader initialized.");
		} else {
			String msg = "DataSourceResourceLoader not properly initialized. No DataSource was identified.";
			log.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public boolean isSourceModified(final Resource resource) {
		return (resource.getLastModified() != readLastModified(resource, "checking timestamp"));
	}

	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public long getLastModified(final Resource resource) {
		return readLastModified(resource, "getting timestamp");
	}

	/**
	 * Get an InputStream so that the Runtime can build a template with it.
	 * 
	 * @param name
	 *            name of template
	 * @return InputStream containing template
	 * @throws ResourceNotFoundException
	 */
	public synchronized InputStream getResourceStream(final String name) throws ResourceNotFoundException {
		if (org.apache.commons.lang.StringUtils.isEmpty(name)) {
			throw new ResourceNotFoundException("DataSourceResourceLoader: Template name was empty or null");
		}

		try (Connection conn = openDbConnection();
				PreparedStatement ps = getStatement(conn, templateColumn, name);
				ResultSet rs = ps.executeQuery();) {
			if (rs.next()) {
				/*
				 * Patch for SQL server driver not immediately streaming all
				 * bytes into memory - CPB
				 */
				// InputStream stream = rs.getBinaryStream(templateColumn);
				Blob blob = rs.getBlob(templateColumn);
				if (blob == null) {
					throw new ResourceNotFoundException(
							"DataSourceResourceLoader: " + "template column for '" + name + "' is null");
				}
				try (InputStream stream = blob.getBinaryStream()) {
					return IOUtils.toBufferedInputStream(stream);
				} catch (IOException e) {
					throw new RuntimeException("Couldn't read velocity data stream", e);
				}
			} else {
				throw new ResourceNotFoundException(
						"DataSourceResourceLoader: " + "could not find resource '" + name + "'");
			}
		} catch (SQLException sqle) {
			String msg = "DataSourceResourceLoader: database problem while getting resource '" + name + "': ";

			log.error(msg, sqle);
			throw new ResourceNotFoundException(msg);
		} catch (NamingException ne) {
			String msg = "DataSourceResourceLoader: database problem while getting resource '" + name + "': ";
			log.error(msg, ne);
			throw new ResourceNotFoundException(msg);
		}
	}

	/**
	 * Fetches the last modification time of the resource
	 * 
	 * @param resource
	 *            Resource object we are finding timestamp of
	 * @param operation
	 *            string for logging, indicating
	 *            dummyPatientEncounterGenerator's intention
	 * 
	 * @return timestamp as long
	 */
	private long readLastModified(final Resource resource, final String operation) {
		long timeStamp = 0;

		/* get the template name from the resource */
		String name = resource.getName();
		if (name == null || name.length() == 0) {
			String msg = "DataSourceResourceLoader: Template name was empty or null";
			log.error(msg);
			throw new NullPointerException(msg);
		} else {
			try (Connection conn = openDbConnection();
					PreparedStatement ps = getStatement(conn, timestampColumn, name);
					ResultSet rs = ps.executeQuery();) {

				if (rs.next()) {
					Timestamp ts = rs.getTimestamp(timestampColumn);
					timeStamp = ts != null ? ts.getTime() : 0;
				} else {
					String msg = "DataSourceResourceLoader: could not find resource " + name + " while " + operation;
					log.error(msg);
					throw new ResourceNotFoundException(msg);
				}
			} catch (SQLException sqle) {
				String msg = "DataSourceResourceLoader: database problem while " + operation + " of '" + name + "': ";

				log.error(msg, sqle);
				throw ExceptionUtils.createRuntimeException(msg, sqle);
			} catch (NamingException ne) {
				String msg = "DataSourceResourceLoader: database problem while " + operation + " of '" + name + "': ";

				log.error(msg, ne);
				throw ExceptionUtils.createRuntimeException(msg, ne);
			}
		}
		return timeStamp;
	}

	/**
	 * Gets connection to the datasource specified through the configuration
	 * parameters.
	 * 
	 * @return connection
	 */
	private Connection openDbConnection() throws NamingException, SQLException {
		return dataSource.getConnection();
	}

	/**
	 * Creates the following PreparedStatement query : <br>
	 * SELECT <i>columnNames</i> FROM <i>tableName</i> WHERE <i>keyColumn</i> =
	 * '<i>templateName</i>' <br>
	 * where <i>keyColumn</i> is a class member set in init()
	 * 
	 * @param conn
	 *            connection to datasource
	 * @param columnNames
	 *            columns to fetch from datasource
	 * @param templateName
	 *            name of template to fetch
	 * @return PreparedStatement
	 */
	private PreparedStatement getStatement(final Connection conn, final String columnNames, final String templateName)
			throws SQLException {
		PreparedStatement ps = conn
				.prepareStatement("SELECT " + columnNames + " FROM " + tableName + " WHERE " + keyColumn + " = ?");
		ps.setString(1, templateName);
		return ps;
	}

}