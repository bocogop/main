package org.bocogop.wr.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bocogop.wr.model.Permission.PermissionType;
import org.bocogop.wr.model.lookup.AbstractLookup;
import org.bocogop.wr.model.lookup.LookupType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "PERMISSION", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "PERMISSION_ID")) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Permission extends AbstractLookup<Permission, PermissionType> implements GrantedAuthority {
	private static final long serialVersionUID = -3175355751589521985L;

	// --------------------------------------------- Static Fields

	public static final String LOGIN_APPLICATION = "Login Application";
	public static final String PRECINCT_ASSIGN = "Assign Precincts";
	public static final String PRECINCT_EDIT = "Edit Precincts";
	public static final String USER_MANAGER = "Manage Users";
	public static final String VOTER_EDIT = "Edit Voters";

	// --------------------------------------------- Fields

	private Set<RolePermission> roles;

	// --------------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getRoles());
	}

	// --------------------------------------------- Accessor Methods

	@Transient
	@Override
	public String getAuthority() {
		return getName();
	}

	@OneToMany(mappedBy = "permission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchSize(size = 500)
	public Set<RolePermission> getRoles() {
		if (roles == null)
			roles = new HashSet<>();
		return roles;
	}

	public void setRoles(Set<RolePermission> roles) {
		this.roles = roles;
	}

	public static enum PermissionType implements LookupType {
		LOGIN_APPLICATION(Permission.LOGIN_APPLICATION, 1), //
		USER_MANAGER(Permission.USER_MANAGER, 2), //
		ASSIGN_PRECINCTS(Permission.PRECINCT_ASSIGN, 3), //
		PRECINCT_EDIT(Permission.PRECINCT_EDIT, 4), //
		VOTER_EDIT(Permission.VOTER_EDIT, 5), //
		;

		private String name;
		private long id;

		private PermissionType(String name, long id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		@Override
		public long getId() {
			return id;
		}

		public static Collection<GrantedAuthority> getAllAsGrantedAuthorities() {
			return Arrays.asList(PermissionType.values()).stream().map(p -> new SimpleGrantedAuthority(p.getName()))
					.collect(Collectors.toList());
		}
	}

}
