<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return getWidget("type").getParameterValue() == "Summary" ? "_Summary"
				: ""
	}

/*	setWidgetsInitializedCallback(function() {
		var stationWidget = getWidget('stationId')
        var servicesWidget = getWidget('services')
      
		servicesWidget.setRefreshParamProvider(function() {
			return {
				stationId : stationWidget.getParameterValue()
			}
		})

		stationWidget
				.addValueUpdatedListener(function(evt, updatedReportParams) {
					var newVal = stationWidget.getParameterValue()
					var showStationDependentWidgets = newVal && newVal != ''
						servicesWidget.toggle(showStationDependentWidgets)
					servicesWidget
							.bypassReportParameters(!showStationDependentWidgets)
				})
	})
	*/
</script>


<%-- ====================================== Fiscal Year param --%>


<div
	style="display: inline-block; vertical-align: top; min-width: 500px; text-align: right">
	<div class="clearCenter" style="text-align: center">

		<tiles:insertDefinition name="widgetSelectList">
			<tiles:putAttribute name="widgetId" value="type" cascade="true" />
			<tiles:putAttribute name="displayLabel" value="Type" />
			<tiles:putAttribute name="submitAsReportParameters" value="false" />
			<tiles:putListAttribute name="items" cascade="true">
				<tiles:addAttribute value="Summary" />
				<tiles:addAttribute value="Detailed" />
			</tiles:putListAttribute>
		</tiles:insertDefinition>
	
		<tiles:insertDefinition name="widgetRadioButtons">
			<tiles:putAttribute name="widgetId" value="sortOrder" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="SortBy" />
			<tiles:putAttribute name="displayLabel" value="Sort By" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
			<tiles:putListAttribute name="items" cascade="true">
				<tiles:addAttribute value="Facility|F" />
				<tiles:addAttribute value="Organization|O" />
			</tiles:putListAttribute>
		</tiles:insertDefinition>

	    	<tiles:insertDefinition name="widgetStationSelector">
			<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="StationId" />
			<tiles:putAttribute name="displayLabel" value="Select Facility:" />
			<tiles:putAttribute name="bypassAsReportParameters" value="false"
				cascade="true" />
			<tiles:putAttribute name="showAllFacilities" value="false"
				cascade="true" />
			<tiles:putAttribute name="mode" value="single" cascade="true" />
		</tiles:insertDefinition>
		
	<tiles:insertDefinition name="widgetFiscalYearSelector">
			<tiles:putAttribute name="widgetId" value="FiscalYear" cascade="true" />
			<tiles:putAttribute name="displayLabel" value="Select Fiscal Year:" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
			<tiles:putAttribute name="mode" value="single" cascade="true" />
			</tiles:insertDefinition>
	
	</div>
