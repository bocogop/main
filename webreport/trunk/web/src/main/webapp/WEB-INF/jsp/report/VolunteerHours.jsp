<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return ""
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')
		var stationWidget = getWidget('stationId')
		var visnWidget = getWidget('VISNId')
		var stateWidget = getWidget('StateId')
			
		scopeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = scopeWidget.getParameterValue()

			stationWidget.toggle(newVal == 'F')
			stationWidget.bypassReportParameters(newVal != 'F')
			visnWidget.toggle(newVal == 'V')
			visnWidget.bypassReportParameters(newVal != 'V')
			stateWidget.toggle(newVal == 'S')
			stateWidget.bypassReportParameters(newVal != 'S')
			
			var stationVal = stationWidget.getParameterValue()
			var showStationDependentWidgets = newVal == 'F' && $.isArray(stationVal) && stationVal.length == 1
		})
		
		stationWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = stationWidget.getParameterValue()
			var showStationDependentWidgets = $.isArray(newVal) && newVal.length == 1
			})
	})

	
	setPreSubmitCallback(function(finalReportParams) {
		var scopeWidget = getWidget('scope')
			
		var theScope = scopeWidget.getParameterValue()
		if (theScope != 'S')
			finalReportParams['StateId'] = '-1'
		if (theScope != 'V')
			finalReportParams['VISNId'] = '-1'
		if (theScope != 'F') {
			finalReportParams['StationId'] = '-1'
		}
		
	})
	
</script>

<div style="display: inline-block; vertical-align: top; text-align:right; min-width:250px;">
	<tiles:insertDefinition name="widgetSelectList">
		<tiles:putAttribute name="widgetId" value="type" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Type" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="reportParamName" value="Type" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Summary" />
			<tiles:addAttribute value="Detail" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetSelectList">
		<%-- This widgetID can be any word, but has to be unique for each widget instance
	on a given page. We use it to retrieve the widget later by calling getWidget(widgetId),
	helpful if there are custom functions that show/hide some widgets based on others. CPB --%>
		<tiles:putAttribute name="widgetId" value="scope" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Filter Report By" />
		<tiles:putAttribute name="reportParamName" value="FilterBy" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Facility|F" />
			<tiles:addAttribute value="State|S" />
			<tiles:addAttribute value="VISN|V" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetFiscalYearSelector">
			<tiles:putAttribute name="widgetId" value="FiscalYear" cascade="true" />
			<tiles:putAttribute name="displayLabel" value="Select Fiscal Year:" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
			<tiles:putAttribute name="mode" value="single" cascade="true" />
			</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="monthsRadio" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ReportMonths" />
		<tiles:putAttribute name="displayLabel" value="Months" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="3|3" />
			<tiles:addAttribute value="6|6" />
			<tiles:addAttribute value="9|9" />
			<tiles:addAttribute value="12|12" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

</div>

<div style="display: inline-block; vertical-align: top; min-width:500px; text-align:right">
	<div class="clearCenter" style="text-align:center">
		<tiles:insertDefinition name="widgetStationSelector">
			<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="StationId" />
			<tiles:putAttribute name="displayLabel" value="Select Facilities:" />
			<tiles:putAttribute name="bypassAsReportParameters" value="false"
				cascade="true" />
			<tiles:putAttribute name="showAllFacilities" value="false"
				cascade="true" />
		</tiles:insertDefinition>
	
		<tiles:insertDefinition name="widgetVISNSelector">
			<tiles:putAttribute name="widgetId" value="VISNId" cascade="true" />
			<tiles:putAttribute name="displayLabel" value="Select VISNs:" />
			<tiles:putAttribute name="bypassAsReportParameters" value="true"
				cascade="true" />
			<tiles:putAttribute name="initialVisibility" value="hidden"
				cascade="true" />
		
		</tiles:insertDefinition>
	
		<tiles:insertDefinition name="widgetStateSelector">
			<tiles:putAttribute name="widgetId" value="StateId" cascade="true" />
			<tiles:putAttribute name="displayLabel" value="Select States:" />
			<tiles:putAttribute name="bypassAsReportParameters" value="true"
				cascade="true" />
			<tiles:putAttribute name="initialVisibility" value="hidden"
				cascade="true" />
		</tiles:insertDefinition>
	</div>
	
</div>
