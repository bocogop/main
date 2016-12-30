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

<jsp:include page="/WEB-INF/jsp/shared/inc_voterSearchPopup.jsp">
	<jsp:param name="uniqueVoterSearchPopupId" value="menuAdd" />
	<jsp:param name="mode" value="add" />
	<jsp:param name="resultCallbackMethod"
		value="menuVoterSelectedCallback" />
	<jsp:param name="disclaimerText"
		value="Please search for an existing voter before adding a new one:" />
	<jsp:param name="addButtonCallbackMethod"
		value="menuVoterAddSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_organizationSearchPopup.jsp">
	<jsp:param name="uniqueOrganizationSearchPopupId" value="menuSearch" />
	<jsp:param name="resultCallbackMethod"
		value="menuOrganizationSelectedCallback" />
	<jsp:param name="includeInactiveOption" value="true" />
	<jsp:param name="mode" value="search" />
	<jsp:param name="addButtonCallbackMethod"
		value="menuOrganizationAddSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId" value="menuSearch" />
	<jsp:param name="resultCallbackMethod"
		value="menuDonorSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId" value="menuAdd" />
	<jsp:param name="mode" value="add" />
	<jsp:param name="resultCallbackMethod"
		value="menuDonorSelectedCallback" />
	<jsp:param name="disclaimerText"
		value="Please search for an existing donor before adding a new one:" />
	<jsp:param name="addButtonCallbackMethod"
		value="menuDonorAddSelectedCallback" />
</jsp:include>

<div class='sidemenucontainer' role="application">
	<div class="sidebarmenu" role="navigation">

		<ul id="navigationMenuBar" role="menubar" title="WR menu bar">

			<li id="appMenuHome" role="menuitem" aria-haspopup="false"
				tabIndex="-1"><a href="${home}" id="homeLink" title="Home"
				shortcut="Shift+h" class="hotkeyed" role="menuitem">Home</a></li>

			<sec:authorize
				access="hasAnyAuthority('${PERMISSION_VOTER_READ},
				${PERMISSION_VOTER_EDIT}, ${PERMISSION_TIME_READ}, ${PERMISSION_TIME_CREATE},
				${PERMISSION_MEALTICKET_READ}, ${PERMISSION_MEALTICKET_CREATE}')">
				<li role="menuitem" aria-haspopup="true"><a
					id="menuVoterLink" href="#menuVoter" title="Voter"
					shortcut="Shift+v" class="hotkeyed" role="menuitem">Voters</a>
					<ul id="menuVoter" role="menu" style="width: 200px">

						<c:if test="${not precinctContextIsCentralOffice}">
							<sec:authorize
								access="hasAuthority('${PERMISSION_VOTER_EDIT}')">
								<li role="menuitem"><a
									href="javascript:popupVoterSearch('menuAdd')"
									title="Add New Voter" shortcut="cv"
									class="sequencehotkeyed" role="menuitem">Add New Voter</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_VOTER_READ}')">
								<li role="menuitem"><a
									href="javascript:popupVoterSearch('menuSearch')"
									title="Existing Voter
										Records" shortcut="sv"
									class="sequencehotkeyed" role="menuitem">Existing Voter
										Records</a></li>
							</sec:authorize>
						</c:if>

						<sec:authorize
							access="hasAuthority('${PERMISSION_VOTER_READ}')">
							<li role="menuitem"><a
								href="${home}/voterDemographics.htm"
								title="Voter Demographics" shortcut="vdg"
								class="sequencehotkeyed" role="menuitem">Voter
									Demographics</a></li>
						</sec:authorize>

						<c:if test="${not precinctContextIsCentralOffice}">
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_TIME_READ}, ${PERMISSION_TIME_CREATE}')">
								<li><a href="${home}/timeEntry.htm"
									title="Post	Regular Time" shortcut="mt"
									class="sequencehotkeyed" role="menuitem">Post Regular Time</a></li>
								<li><a href="${home}/occasionalTimeEntry.htm"
									title="Post Occasional Time" shortcut="ote"
									class="sequencehotkeyed" role="menuitem">Post Occasional
										Time</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_MEALTICKET_READ},
									${PERMISSION_MEALTICKET_CREATE}')">
								<c:if
									test="${not empty precinctContextNumMeals and precinctContextNumMeals > 0}">
									<li role="menuitem"><a href="${home}/mealTicketList.htm"
										title="Daily Meal Tickets" shortcut="lmt"
										class="sequencehotkeyed" role="menuitem">Daily Meal
											Tickets</a></li>
								</c:if>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_VOTER_READ}')">
								<li role="menuitem"><a href="${home}/award.htm"
									title="Awards" shortcut="awd" class="sequencehotkeyed"
									role="menuitem">Awards</a></li>
							</sec:authorize>
						</c:if>
					</ul></li>
			</sec:authorize>

			<c:if test="${not precinctContextIsCentralOffice}">
				<sec:authorize
					access="hasAnyAuthority('${PERMISSION_DONATION_READ},
					${PERMISSION_DONATION_CREATE}, ${PERMISSION_EDONATION_MANAGE}')">
					<li role="menuitem" aria-haspopup="true"><a
						id="menuDonationLink" href="#menuDonations" title="Donation"
						shortcut="Shift+d" class="hotkeyed" role="menuitem">Donations</a>
						<ul id="menuDonation" role="menu" style="width: 220px">
							<sec:authorize
								access="hasAuthority('${PERMISSION_DONATION_CREATE}')">
								<li role="menuitem"><a
									href="javascript:popupDonorSearch('menuAdd')"
									title="Add New Donation" shortcut="ad" class="sequencehotkeyed"
									role="menuitem">Add New Donation</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_DONATION_CREATE}')">
								<li role="menuitem"><a
									href="${home}/donationCreate.htm?donorId=0"
									title="Add Anonymous Donation" shortcut="nd"
									class="sequencehotkeyed" role="menuitem">Add Anonymous
										Donation</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_DONATION_READ}')">
								<li role="menuitem"><a
									href="javascript:popupDonorSearch('menuSearch')"
									title="Existing Donor Records" shortcut="sd"
									class="sequencehotkeyed" role="menuitem">Existing Donor
										Records</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_DONATION_READ}')">
								<li role="menuitem"><a href="${home}/donationList.htm"
									title="Existing Donation Records" shortcut="ld"
									class="sequencehotkeyed" role="menuitem">Existing Donation
										Records</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_EDONATION_MANAGE}')">
								<li role="menuitem"><a href="${home}/manageDonationLog.htm"
									title="E-Donations Received" shortcut="ed"
									class="sequencehotkeyed" role="menuitem">E-Donations Received</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_DONATION_READ}')">
								<li role="menuitem"><a
									href="${home}/donationList.htm?listDonationsMode=thankyou"
									title="Print Thank You Letters" shortcut="ty"
									class="sequencehotkeyed" role="menuitem">Print Thank You
										Letters</a></li>
							</sec:authorize>
						</ul></li>
				</sec:authorize>
			</c:if>
			
			<sec:authorize
					access="hasAnyAuthority('${PERMISSION_EXPENDITURE_CREATE}')">
				<li role="menuitem" aria-haspopup="true"><a
					id="menuExpenditureLink" href="#menuExpenditures" title="Expenditure"
					shortcut="Shift+e" class="hotkeyed" role="menuitem">Expenditures</a>
					<ul id="menuExpenditure" role="menu" style="width: 220px">
						<sec:authorize
							access="hasAuthority('${PERMISSION_EXPENDITURE_CREATE}')">
							<li role="menuitem"><a
								href="${home}/ledger.htm"
								title="General Ledger" shortcut="gl" class="sequencehotkeyed"
								role="menuitem">General Ledger</a></li>
						</sec:authorize>
						<sec:authorize
							access="hasAuthority('${PERMISSION_EXPENDITURE_CREATE}')">
							<li role="menuitem"><a
								href="${home}/expenditureList.htm"
								title="Existing Expenditure Records" shortcut="el" class="sequencehotkeyed"
								role="menuitem">Existing Expenditure Records</a></li>
						</sec:authorize>
					</ul></li>
			</sec:authorize>

			<li role="menuitem" aria-haspopup="true"><a href="#menuReports"
				title="Reports" shortcut="Shift+o" class="hotkeyed" role="menuitem">Reports</a>
				<ul id="appMenuReports" role="menu" style="width: 170px">

					<li role="menuitem" aria-haspopup="true"><a
						href="#menuReportsVoters" title="Voters" shortcut="ar"
						class="sequencehotkeyed" role="menuitem">Voters</a>

						<ul id="voterReportsList" role="menu">
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_MEAL_TICKET_REPORT}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=mtf"
									title="<fmt:message key="reports.mtf.displayName" />"
									shortcut="mtf" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.mtf.displayName" /></a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_NEW_VOTERS}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=nvr"
									title="<fmt:message key="reports.nvr.displayName" />"
									shortcut="nvr" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.nvr.displayName" /></a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_EXCLUDED_ENTITY_VIEW_LOCAL}, ${EXCLUDED_ENTITY_VIEW_ALL}')">
								<li role="menuitem"><a
									href="${home}/excludedEntityList.htm"
									title="View Excluded Entities" shortcut="ee"
									class="sequencehotkeyed" role="menuitem">View Excluded
										Entities</a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_RUN_VOTERS_BY_SERVICE}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=vba"
									title="<fmt:message key="reports.vba.displayName" />"
									shortcut="vba" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.vba.displayName" /></a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_RUN_VOTERS_BY_ORG}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=vbo"
									title="<fmt:message key="reports.vbo.displayName" />"
									shortcut="vbo" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.vbo.displayName" /></a></li>
							</sec:authorize>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_SCHEDULED_OCCAS_HOURS}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=soh"
									title="<fmt:message key="reports.soh.displayName" />"
									shortcut="soh" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.soh.displayName" /></a></li>
							</sec:authorize>

							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_RUN_VOTER_ALPHA}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=vde"
									title="<fmt:message key="reports.vde.displayName" />"
									shortcut="vde" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.vde.displayName" /></a></li>
							</sec:authorize>
						</ul></li>

					<li role="menuitem" aria-haspopup="true"><a
						href="#menuReportsDonations" title="Donations" shortcut="ar"
						class="sequencehotkeyed" role="menuitem">Donations</a>

						<ul id="donationReportsList" role="menu">
							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_DONATION_CREATE}')">
								<li role="menuitem"><a
									href="${home}/letterTemplateEdit.htm"
									title="Manage Letter Templates" shortcut="elt"
									class="sequencehotkeyed" role="menuitem">Manage <c:if
											test="${precinctContextIsCentralOffice}">Default</c:if>
										Letter Templates
								</a></li>
							</sec:authorize>

							<sec:authorize
								access="hasAnyAuthority('${PERMISSION_DONATION_READ}, ${PERMISSION_RUN_GRAND_TOTAL_DONATIONS}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=gtd"
									title="<fmt:message key="reports.gtd.displayName" />"
									shortcut="gtd" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.gtd.displayName" /></a></li>
							</sec:authorize>
						</ul></li>

					<%--<li role="menuitem" aria-haspopup="true"><a
						href="#menuReportsAwards" title="Awards Reports" shortcut="ar"
						class="sequencehotkeyed" role="menuitem">Awards</a></li> --%>

					<li role="menuitem" aria-haspopup="true"><a
						href="#menuReportsAdmin" title="Administration" shortcut="ar"
						class="sequencehotkeyed" role="menuitem">Administration</a>
						<ul id="menuReportsAdmin" role="menu">
							<li role="menuitem" aria-haspopup="true"><a
								href="#menuAddressLabelReports" title="Address Label Reports"
								shortcut="ar" class="sequencehotkeyed" role="menuitem">Address
									Label Reports</a>

								<ul id="addressLabelReportsList" role="menu">

									<%-- Voter Address Labels --%>
									<sec:authorize
										access="hasAuthority('${PERMISSION_RUN_VOTER_ADDRESS_LABELS}')">
										<li role="menuitem"><a
											href="${home}/displayReportParameters.htm?reportCode=val"
											title="<fmt:message key="reports.val.displayName" />"
											shortcut="val" class="sequencehotkeyed" role="menuitem"><fmt:message
													key="reports.val.displayName" /></a></li>
									</sec:authorize>

									<%-- Voluntary Service Directory Labels --%>
									<sec:authorize
										access="hasAuthority('${PERMISSION_RUN_VOLUNTARY_SERVICE_ADDRESS_LABELS}')">
										<li role="menuitem"><a
											href="${home}/displayReportParameters.htm?reportCode=vsdl"
											title="<fmt:message key="reports.vsdl.displayName" />"
											shortcut="vsdl" class="sequencehotkeyed" role="menuitem"><fmt:message
													key="reports.vsdl.displayName" /></a></li>
									</sec:authorize>

									<%-- National Officials Label --%>
									<sec:authorize
										access="hasAuthority('${PERMISSION_RUN_NATIONAL_OFFICIAL_ADDRESS_LABELS}')">
										<li role="menuitem"><a
											href="${home}/displayReportParameters.htm?reportCode=nol"
											title="<fmt:message key="reports.nol.displayName" />"
											shortcut="nol" class="sequencehotkeyed" role="menuitem"><fmt:message
													key="reports.nol.displayName" /></a></li>
									</sec:authorize>
								</ul></li>

							<%-- Voluntary Service Directory --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_VOLUNTARY_SERVICE_DIRECTORY}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=vsd"
									title="<fmt:message key="reports.vsd.displayName" /> Report"
									shortcut="vsd" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.vsd.displayName" /></a></li>
							</sec:authorize>

							<%-- National Officials Listing --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_NATIONAL_OFFICIAL_LISTING}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=noi"
									title="<fmt:message key="reports.noi.displayName" />"
									shortcut="nol" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.noi.displayName" /></a></li>
							</sec:authorize>

							<%-- Organization Listing --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_ORGANIZATION_LISTING}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=org"
									title="<fmt:message key="reports.org.displayName" />"
									shortcut="nol" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.org.displayName" /></a></li>
							</sec:authorize>
							
								<%-- RUN_BENEFITING SERVIVE LISTING --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_BENEFITING_SERVIVE_LISTING}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=srv"
									title="<fmt:message key="reports.srv.displayName" />"
									shortcut="nol" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.srv.displayName" /></a></li>
							</sec:authorize>
							

							<%-- Committee Attendance Listing --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_COMMITTEE_ATTENDANCE_LISTING}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=cal"
									title="<fmt:message key="reports.cal.displayName" />"
									shortcut="nol" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.cal.displayName" /></a></li>
							</sec:authorize>
							
							<%-- Vol Orgs RS and Occ Hours --%>
							<sec:authorize
								access="hasAuthority('${PERMISSION_RUN_VOL_ORGS_RS_OCC_HOURS}')">
								<li role="menuitem"><a
									href="${home}/displayReportParameters.htm?reportCode=osoh"
									title="<fmt:message key="reports.osoh.displayName" />"
									shortcut="osoh" class="sequencehotkeyed" role="menuitem"><fmt:message
											key="reports.osoh.displayName" /></a></li>
							</sec:authorize>

						</ul></li>
				</ul></li>

			<li role="menuitem" aria-haspopup="true"><a
				href="#menuMaintenance" title="Maintenance" shortcut="Shift+d"
				class="hotkeyed" role="menuitem">Maintenance</a>
				<ul id="menuAdministration" role="menu" style="width: 240px">
					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_PRECINCT_CREATE}, ${PERMISSION_PRECINCT_EDIT_ALL}, ${PERMISSION_PRECINCT_EDIT_CURRENT}')">
						<li role="menuitem"><a href="${home}/precinctEdit.htm"
							title="Manage Precincts" shortcut="mf" class="sequencehotkeyed"
							role="menuitem">Manage Precincts</a></li>
					</sec:authorize>
					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_ORG_CODE_NATIONAL_READ}, 
													 ${PERMISSION_ORG_CODE_LOCAL_READ}')">
						<li role="menuitem"><a href="${home}/organizationList.htm"
							title="Manage Organizations" shortcut="mo"
							class="sequencehotkeyed" role="menuitem">Manage Organizations</a>
						</li>
						<li role="menuitem"><a
							href="javascript:popupOrganizationSearch('menuSearch')"
							title="Search Existing Organizations" shortcut="sv"
							class="sequencehotkeyed" role="menuitem">Search Existing
								Organizations</a></li>
					</sec:authorize>
					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_PERM_CODE_SERVICE_READ}, 
													 ${PERMISSION_PERM_CODE_SERVICE_CREATE}')">
						<li role="menuitem"><a
							href="${home}/editVoluntaryService.htm"
							title="Manage Voluntary Service" shortcut="evs"
							class="sequencehotkeyed" role="menuitem">Manage Voluntary
								Service</a></li>
					</sec:authorize>

					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_BENEFITING_SERVICE_CREATE}')">
						<li><a href="${home}/manageBenefitingServices.htm"
							title="Manage Benefiting Services" shortcut="mbs"
							class="sequencehotkeyed" role="menuitem">Manage Benefiting
								Services</a></li>
					</sec:authorize>
					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_BENEFITING_SERVICE_TEMPLATE_MANAGE}')">
						<li><a href="${home}/manageBenefitingServiceTemplates.htm"
							title="Manage Service Templates" shortcut="mbst"
							class="sequencehotkeyed" role="menuitem">Manage Service
								Templates</a></li>
					</sec:authorize>

					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_REQUIREMENTS_LOCAL_MANAGE},
											${PERMISSION_REQUIREMENTS_GLOBAL_MANAGE}')">

						<li><a href="${home}/manageRequirements.htm"
							title="Manage Voter Requirements" shortcut="mr"
							class="sequencehotkeyed" role="menuitem">Manage Voter
								Requirements</a></li>
					</sec:authorize>

					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_STAFF_TITLE_CREATE}')">
						<li><a href="${home}/manageStaffTitle.htm"
							title="Manage Staff Title" shortcut="mst"
							class="sequencehotkeyed" role="menuitem">Manage Staff Title</a></li>
					</sec:authorize>

					<sec:authorize
						access="hasAnyAuthority('${PERMISSION_AWARD_CODE_CREATE}')">
						<li><a href="${home}/manageAwardCodes.htm"
							title="Manage Award Codes" shortcut="maw"
							class="sequencehotkeyed" role="menuitem">Manage Award Codes</a></li>
					</sec:authorize>



				</ul></li>
			<li role="menuitem" aria-haspopup="true"><a
				href="#menuReference" title="Reference" shortcut="Shift+f"
				class="hotkeyed" id="menuReferenceAnchor" role="menuitem">Reference</a>
				<ul id="menuReference" role="menu" style="width: 165px">
					<%-- Populated in CommonReferenceDataInterceptor via a Velocity template in the DB - CPB --%>
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
