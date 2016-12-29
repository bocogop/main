<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return getWidget("type").getParameterValue() + '2'
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')

		scopeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = scopeWidget.getParameterValue()

			var stationWidget = getWidget('StationId')
			stationWidget.toggle(newVal == 'F')
			stationWidget.bypassReportParameters(newVal != 'F')

			var visnWidget = getWidget('VISNId')
			visnWidget.toggle(newVal == 'V')
			visnWidget.bypassReportParameters(newVal != 'V')

			var stateWidget = getWidget('StateId')
			stateWidget.toggle(newVal == 'S')
			stateWidget.bypassReportParameters(newVal != 'S')
		})
	})

	setPreSubmitCallback(function(finalReportParams) {
		var theScope = getWidget("scope").getParameterValue()
		if (theScope != 'S')
			finalReportParams['StateId'] = '-1'
		if (theScope != 'V')
			finalReportParams['VISNId'] = '-1'
		if (theScope != 'F')
			finalReportParams['StationId'] = '-1'
	})
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetSelectList">
		<%-- This widgetID can be any word, but has to be unique for each widget instance
	on a given page. We use it to retrieve the widget later by calling getWidget(widgetId),
	helpful if there are custom functions that show/hide some widgets based on others. CPB --%>
		<tiles:putAttribute name="widgetId" value="scope" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Filter Report By" />
		<tiles:putAttribute name="reportParamName" value="SearchType" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="All|A" />
			<tiles:addAttribute value="Facility|F" />
			<tiles:addAttribute value="State|S" />
			<tiles:addAttribute value="VISN|V" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="type" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Type" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Summary" />
			<tiles:addAttribute value="Detail" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="sortOrder" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="SortOrder" />
		<tiles:putAttribute name="displayLabel" value="Sort By" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Alphabetical|NameOfInstitution" />
			<tiles:addAttribute value="State|StateName" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>
</div>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="StationId" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Select facilities" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true"
			cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="true"
			cascade="true" />			
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetVISNSelector">
		<tiles:putAttribute name="widgetId" value="VISNId" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Select VISNs" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true"
			cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden"
			cascade="true" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetStateSelector">
		<tiles:putAttribute name="widgetId" value="StateId" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Select States" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true"
			cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden"
			cascade="true" />
	</tiles:insertDefinition>
</div>
