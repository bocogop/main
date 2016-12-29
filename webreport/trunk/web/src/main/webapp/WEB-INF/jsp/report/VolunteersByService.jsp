<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return getWidget("type").getParameterValue() == "Summary" ? "_Summary"
				: ""
	}

	setWidgetsInitializedCallback(function() {
		var stationWidget = getWidget('stationId')
		var servicesWidget = getWidget('services')
		var typeWidget = getWidget('type')
		var showVisitWidget = getWidget('showVisits')

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
				
				
		typeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = typeWidget.getParameterValue()
			var showTypeDependentWidgets = newVal == "Detailed" && newVal != ''
			showVisitWidget.toggle(showTypeDependentWidgets)
			showVisitWidget.bypassReportParameters(!showTypeDependentWidgets)
		})
	})

	setPreSubmitCallback(function(finalReportParams) {
		
		if(getWidget("type").getParameterValue() == 'Summary'){
			finalReportParams['ShowVisits'] = "N"
		}
		var beginDate = getWidget("beginDate").getParameterValue()
		var endDate = getWidget("endDate").getParameterValue()

		var tokens = beginDate.split("/");
		var beginMonth = tokens[0];
		var beginYear = tokens[1];
		tokens = endDate.split("/");
		var endMonth = tokens[0];
		var endYear = tokens[1];
		finalReportParams['BeginDate'] = beginMonth + '/1/' + beginYear;
		if (parseInt(endMonth) != 12)
			finalReportParams['EndDate'] = (parseInt(endMonth) + 1) + '/1/'
					+ endYear;
		else
			finalReportParams['EndDate'] = '1/1/' + (parseInt(endYear) + 1);

		var startMonths = beginYear * 12 + parseInt(beginMonth);
		var endMonths = endYear * 12 + parseInt(endMonth);
		var gap = endMonths - startMonths + 1;
		if (startMonths > endMonths)
			return 'Start Date cannot be greater than End Date.'
		if (gap > 12)
			return 'Begin and End date cannot span more than 12 months'

		return null
	})
</script>

<div
	style="display: inline-block; vertical-align: top; text-align: right; min-width: 250px;">
	<tiles:insertDefinition name="widgetSelectList">
		<tiles:putAttribute name="widgetId" value="type" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Type" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Summary" />
			<tiles:addAttribute value="Detailed" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<%-- ====================================== Date params --%>

	<tiles:insertDefinition name="widgetMonthYearInput">
		<tiles:putAttribute name="widgetId" value="beginDate" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="BeginDate" />
		<tiles:putAttribute name="displayLabel" value="Begin Date (MM/YYYY)" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='style="width:110px"' cascade="true" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetMonthYearInput">
		<tiles:putAttribute name="widgetId" value="endDate" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="EndDate" />
		<tiles:putAttribute name="displayLabel" value="End Date (MM/YYYY)" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='style="width:110px"' cascade="true" />
	</tiles:insertDefinition>

	<c:out value="Date range cannot exceed 12 months." />

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="showVisits" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ShowVisits" />
		<tiles:putAttribute name="displayLabel" value="Show Visits" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Yes|Y" />
			<tiles:addAttribute value="No|N" />
		</tiles:putListAttribute>
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

</div>

<div
	style="display: inline-block; vertical-align: top; min-width: 500px; text-align: right">
	<div class="clearCenter" style="text-align: center">
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

		<%-- =================== Assignment params --%>

		<tiles:insertDefinition name="widgetServiceSelector">
			<tiles:putAttribute name="widgetId" value="services" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="ServiceID" />
			<tiles:putAttribute name="displayLabel" value="Services" />
			<tiles:putAttribute name="mode" value="multiple" cascade="true" />
			<tiles:putAttribute name="bypassAsReportParameters" value="true" />
			<tiles:putAttribute name="initialVisibility" value="hidden" />
		</tiles:insertDefinition>
	</div>

</div>

