<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript"
	src="${pkgHome}/accessible-dropdown-menu/accessible-dropdown-menu.js"></script>
<link type="text/css"
	href="${pkgHome}/accessible-dropdown-menu/accessible-dropdown-menu.css"
	rel="Stylesheet" />

<jsp:include page="/WEB-INF/jsp/shared/inc_voterSearchPopup.jsp">
	<jsp:param name="uniqueVoterSearchPopupId" value="menuSearch" />
	<jsp:param name="resultCallbackMethod"
		value="menuVoterSelectedCallback" />
</jsp:include>

<div class='sidemenucontainer' role="application">
	<div class="sidebarmenu" role="navigation">

		<ul id="navigationMenuBar" role="menubar" title="WR menu bar">

			<li id="appMenuHome" role="menuitem" aria-haspopup="false"
				tabIndex="-1"><a href="${home}" id="homeLink" title="Home"
				shortcut="Shift+h" class="hotkeyed" role="menuitem">Home</a></li>

			<li role="menuitem" aria-haspopup="true"><a id="menuVoterLink"
				href="#menuVoter" title="Voter" shortcut="Shift+v" class="hotkeyed"
				role="menuitem">Voters</a>
				<ul id="menuVoter" role="menu" style="width: 200px">
					<sec:authorize access="hasAuthority('${PERMISSION_VOTER_EDIT}')">
						<li role="menuitem"><a
							href="javascript:popupVoterSearch('menuAdd')"
							title="Add New Voter" shortcut="cv" class="sequencehotkeyed"
							role="menuitem">Add New Voter</a></li>
					</sec:authorize>
					<li role="menuitem"><a
						href="javascript:popupVoterSearch('menuSearch')"
						title="Existing Voter
										Records" shortcut="sv"
						class="sequencehotkeyed" role="menuitem">Existing Voter
							Records</a></li>
					<li role="menuitem"><a href="${home}/voterDemographics.htm"
						title="Voter Demographics" shortcut="vdg" class="sequencehotkeyed"
						role="menuitem">Voter Demographics</a></li>
				</ul></li>


			<li role="menuitem" aria-haspopup="true"><a
				href="#menuMaintenance" title="Maintenance" shortcut="Shift+d"
				class="hotkeyed" role="menuitem">Maintenance</a>
				<ul id="menuAdministration" role="menu" style="width: 240px">
					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_PRECINCT_EDIT}')">
						<li role="menuitem"><a href="${home}/precinctEdit.htm"
							title="Manage Precincts" shortcut="mf" class="sequencehotkeyed"
							role="menuitem">Manage Precincts</a></li>
					</sec:authorize>

				</ul></li>
			<li role="menuitem" aria-haspopup="true"><a
				href="#menuReference" title="Reference" shortcut="Shift+f"
				class="hotkeyed" id="menuReferenceAnchor" role="menuitem">Reference</a>
				<ul id="menuReference" role="menu" style="width: 165px">
					${referenceDataLinks}
				</ul></li>

			<c:set var="isRoleManager" value="false" />
			<sec:authorize access="hasAuthority('${PERMISSION_USER_MANAGER}')">
				<c:set var="isRoleManager" value="true" />
			</sec:authorize>
			<li role="menuitem"><a href="${home}/userAdmin.htm"
				tit`le="Manage User Access" shortcut="cs" class="sequencehotkeyed"
				role="menuitem"><c:if test="${isRoleManager}">Manage User Access</c:if>
					<c:if test="${not isRoleManager}">Manage My User</c:if></a></li>
		</ul>
	</div>
</div>

<script type="text/javascript">
	$(function() {
		accessibleDropdownMenu('#navigationMenuBar', 100);

		// Adding a span with empty title to all anchors with role=menuitem so as to avoid
		// mouseover display of title on anchor tags. These titles
		// currently hold the shortcut key sequence to invoke the specific menu option.
		$("a[role='menuitem']").each(function() {
			var currentContent = $(this).html()
			$(this).html("<span title=''>" + currentContent + "</span>")
		})
	})
</script>
