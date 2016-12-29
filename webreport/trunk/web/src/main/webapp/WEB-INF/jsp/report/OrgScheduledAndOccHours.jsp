<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return ""
	}

	setWidgetsInitializedCallback(function() {
		var station1Widget = getWidget('stationId1')
		var station2Widget = getWidget('stationId2')
		var organizationWidget = getWidget('organizations')
		var typeWidget = getWidget('type')
		var showInactiveWidget = getWidget('showInactive')

		station1Widget.toggle(true)
		organizationWidget.toggle(true)

		organizationWidget
				.setRefreshParamProvider(function() {
					return {
						stationId : typeWidget.getParameterValue() != '3' ? station1Widget
								.getParameterValue()
								: station2Widget.getParameterValue(),
						includeNational : typeWidget.getParameterValue() != '3' ? true
								: false,
						includeLocal : typeWidget.getParameterValue() == '3' ? true
								: false,
						includeInactiveOrgs : showInactiveWidget
								.getParameterValue() == '1' ? true : false,
						nacOrgsOnly : typeWidget.getParameterValue() == '1' ? true
								: false
					}
				})

		station2Widget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			organizationWidget.toggle(true)
			organizationWidget.refresh()
		})

		typeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = typeWidget.getParameterValue()
			var showType3DependentWidgets = newVal != '3'
			station1Widget.toggle(showType3DependentWidgets)
			station2Widget.toggle(!showType3DependentWidgets)
			if (newVal == '3')
				organizationWidget.toggle(false)
			if(newVal != '3') {
				organizationWidget.toggle(true)
				organizationWidget.refresh()
			}
		})

		showInactiveWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			organizationWidget.refresh()
		})

	})

	setPreSubmitCallback(function(finalReportParams) {

		if (getWidget("type").getParameterValue() != "3") {
			finalReportParams['StationId'] = finalReportParams['StationId1']
			delete finalReportParams['StationId1']
			delete finalReportParams['StationId2']
		}

		if (getWidget("type").getParameterValue() == "3") {
			finalReportParams['StationId'] = finalReportParams['StationId2']
			delete finalReportParams['StationId2']
			delete finalReportParams['StationId1']
		}

		return null
	})
</script>

<div
	style="display: inline-block; vertical-align: top; text-align: center; min-width: 250px;">

	<tiles:insertDefinition name="widgetSelectList">
		<%-- This widgetID can be any word, but has to be unique for each widget instance
	on a given page. We use it to retrieve the widget later by calling getWidget(widgetId),
	helpful if there are custom functions that show/hide some widgets based on others. CPB --%>
		<tiles:putAttribute name="widgetId" value="type" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Type" />
		<tiles:putAttribute name="reportParamName" value="Type" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Part 1|1|checked" />
			<tiles:addAttribute value="Part 2|2" />
			<tiles:addAttribute value="Part 3|3" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetDateRange">
		<tiles:putAttribute name="widgetId" value="dateRange" cascade="true" />
		<tiles:putAttribute name="reportParamNameBegin" value="BeginDate"
			cascade="true" />
		<tiles:putAttribute name="reportParamNameEnd" value="EndDate"
			cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Date Range" />
		<tiles:putAttribute name="dateGapLimit" value="365" cascade="true" />  
		<tiles:putAttribute name="initialEndValue" cascade="true">
			<wr:localDate date="${currentDate}" pattern="${TWO_DIGIT_DATE_ONLY}" />
		</tiles:putAttribute>
	</tiles:insertDefinition>

	<c:out value="Date range cannot exceed 12 months." />

	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId1" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="StationId1" />
		<tiles:putAttribute name="displayLabel" value="Select Facility:" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId2" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="StationId2" />
		<tiles:putAttribute name="displayLabel" value="Select Facility:" />
		<tiles:putAttribute name="mode" value="single" cascade="true" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetOrganizationSelector">
		<tiles:putAttribute name="widgetId" value="organizations"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="OrganizationID" />
		<tiles:putAttribute name="displayLabel" value="Organizations:" />
		<tiles:putAttribute name="includeLocal" value="false" cascade="true" />
		<tiles:putAttribute name="includeNational" value="true" cascade="true" />
		<tiles:putAttribute name="nacOrgsOnly" value="true" cascade="true" />
		<tiles:putAttribute name="includeBranches" value="false"
			cascade="true" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetCheckbox">
		<tiles:putAttribute name="widgetId" value="showInactive"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="showInactive" />
		<tiles:putAttribute name="displayLabel" value="Show Inactive" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
	</tiles:insertDefinition>

</div>

