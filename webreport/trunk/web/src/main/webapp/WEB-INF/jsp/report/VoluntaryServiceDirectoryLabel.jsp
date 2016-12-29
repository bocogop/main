<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return "_" + getWidget("scope").getParameterValue()
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')

		scopeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = scopeWidget.getParameterValue()

			var stationWidget = getWidget('StationId')
			stationWidget.toggle(newVal == 'Facility')
			stationWidget.bypassReportParameters(newVal != 'Facility')

			var visnWidget = getWidget('VISNId')
			visnWidget.toggle(newVal == 'VISN')
			visnWidget.bypassReportParameters(newVal != 'VISN')

			var stateWidget = getWidget('StateId')
			stateWidget.toggle(newVal == 'State')
			stateWidget.bypassReportParameters(newVal != 'State')
		})
	})

	function skipLabelValidator(inputWithValidationFailure) {
		// reference inputWithValidationFailure.validity or $(inputWithValidationFailure).attr('id')
		// if desired - CPB
		return "Please enter a skip labels value between 0 and 27."
	}
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetSelectList">
		<%-- This widgetID can be any word, but has to be unique for each widget instance
	on a given page. We use it to retrieve the widget later by calling getWidget(widgetId),
	helpful if there are custom functions that show/hide some widgets based on others. CPB --%>
		<tiles:putAttribute name="widgetId" value="scope" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Filter Report By" />
		<tiles:putAttribute name="reportParamName" value="SearchType" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="All" />
			<tiles:addAttribute value="Facility" />
			<tiles:addAttribute value="State" />
			<tiles:addAttribute value="VISN" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="sortOrder" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="SortOrder" />
		<tiles:putAttribute name="displayLabel" value="Sort By" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Facility|NameOfInstitution" />
			<tiles:addAttribute value="State|StateName" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetTextInput">
		<tiles:putAttribute name="widgetId" value="skipLabels" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="SkipLabels" />
		<tiles:putAttribute name="displayLabel"
			value="Number of Labels to Skip" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="inputType" value="number" cascade="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='value="0" min="0" max="27" style="width:50px"' cascade="true" />
		<tiles:putAttribute name="htmlValidationFailureMessageProvider"
			value="skipLabelValidator" />
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
