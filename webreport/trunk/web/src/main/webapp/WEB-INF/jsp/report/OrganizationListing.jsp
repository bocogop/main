<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return ""
	}

	setWidgetsInitializedCallback(function() {
		var stationWidget = getWidget('stationId')
		var organizationsWidget = getWidget('organizations')
		var nationalOrLocalWidget = getWidget('nationalOrLocalOrg')
		var includeBranchesWidget = getWidget('includeBranches')
		var includeInactiveWidget = getWidget('includeInactive')

		organizationsWidget.setRefreshParamProvider(function() {
			var nationOrLocal = nationalOrLocalWidget.getParameterValue()
			if (nationOrLocal == 'N') {
				return {
					includeLocal : "false",
					includeNational : "true",
					includeInactiveOrgs : includeInactiveWidget
							.getParameterValue() == '1' ? true : false,
				}
			} else {
				return {
					includeLocal : "true",
					includeNational : "false",
					stationId : stationWidget.getParameterValue(),
					includeInactiveOrgs : includeInactiveWidget
							.getParameterValue() == '1' ? true : false,
				}
			}
		})

		nationalOrLocalWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			var newVal = nationalOrLocalWidget.getParameterValue()

			var showNationalOrLocalDependentWidgets = newVal == 'L'
			stationWidget.toggle(showNationalOrLocalDependentWidgets)
			organizationsWidget.toggle(!showNationalOrLocalDependentWidgets)
			organizationsWidget.refresh()
		})

		stationWidget
				.addValueUpdatedListener(function(evt, updatedReportParams) {
					var newVal = stationWidget.getParameterValue()
					var showStationDependentWidgets = newVal && newVal != ''
					organizationsWidget.toggle(showStationDependentWidgets)
					organizationsWidget.refresh()
				})

		includeInactiveWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			organizationsWidget.refresh()
		})

	})

	setPreSubmitCallback(function(finalReportParams) {

		if (getWidget("includeBranches").getParameterValue() == '1') {
			finalReportParams['Type'] = [ 'O', 'B' ]
		}
		if (getWidget("includeBranches").getParameterValue() != '1') {
			finalReportParams['Type'] = 'O'
		}
		if (getWidget("includeInactive").getParameterValue() != '1') {
			finalReportParams['Inactive'] = '0'
		}

		if (getWidget("includeInactive").getParameterValue() == '1') {
			finalReportParams['Inactive'] = [ '0', '1' ]
		}

		if (getWidget('nationalOrLocalOrg').getParameterValue() == 'N') {
			finalReportParams['Facility'] = '-1'
		}

	})
</script>



<div
	style="display: inline-block; vertical-align: top; text-align: center; min-width: 250px;">
	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="nationalOrLocalOrg"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Scope" />
		<tiles:putAttribute name="displayLabel" value="Facility" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="National|N" />
			<tiles:addAttribute value="Local|L|Checked" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Facility" />
		<tiles:putAttribute name="displayLabel" value="Select Facility:" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
		<tiles:putAttribute name="mode" value="single" cascade="true" />
	</tiles:insertDefinition>

	<div
		style="display: inline-block; vertical-align: top; text-align: right; min-width: 250px;">
		<tiles:insertDefinition name="widgetCheckbox">
			<tiles:putAttribute name="widgetId" value="includeBranches"
				cascade="true" />
			<tiles:putAttribute name="reportParamName" value="Type" />
			<tiles:putAttribute name="displayLabel" value="Include Branches" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
		</tiles:insertDefinition>

		<tiles:insertDefinition name="widgetCheckbox">
			<tiles:putAttribute name="widgetId" value="includeInactive"
				cascade="true" />
			<tiles:putAttribute name="reportParamName" value="Inactive" />
			<tiles:putAttribute name="displayLabel"
				value="Include Inactive Orgs/Branches" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
		</tiles:insertDefinition>
	</div>

	<%-- ====================================== Organization params --%>

	<tiles:insertDefinition name="widgetOrganizationSelector">
		<tiles:putAttribute name="widgetId" value="organizations"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Organizations" />
		<tiles:putAttribute name="displayLabel" value="Organizations:" />
		<tiles:putAttribute name="includeLocal" value="true" cascade="true" />
		<tiles:putAttribute name="includeNational" value="false"
			cascade="true" />
		<tiles:putAttribute name="includeBranches" value="false"
			cascade="true" />

		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<%-- ====================================== Organization Types --%>

	<tiles:insertDefinition name="widgetOrganizationTypeSelector">
		<tiles:putAttribute name="widgetId" value="organizationType"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="OrgTypes" />
		<tiles:putAttribute name="displayLabel" value="Organization Type" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false" />
	</tiles:insertDefinition>


</div>