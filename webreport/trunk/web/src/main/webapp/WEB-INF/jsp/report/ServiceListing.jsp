<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return getWidget("nationalOrLocal").getParameterValue() == 'N'
				&& getWidget("templateOnly").getParameterValue() == '1' ? "_Template"
				: ""
	}

	setWidgetsInitializedCallback(function() {
		var stationWidget = getWidget('station')
		var serviceTemplateWidget = getWidget('serviceTemplates')
		var serviceWidget = getWidget('services')
		var nationalOrLocalWidget = getWidget('nationalOrLocal')
		var templateOnlyWidget = getWidget('templateOnly')
		var includeInactiveWidget = getWidget('includeInactive')
		var gamesOnlyWidget = getWidget('gamesOnly')

		stationWidget.toggle(true)

		serviceWidget
				.setRefreshParamProvider(function() {
					return {
						stationId : nationalOrLocalWidget.getParameterValue() == 'L' ? stationWidget
								.getParameterValue()
								: '-1',
						gamesRelated : gamesOnlyWidget.getParameterValue() == '1' ? true
								: false,
						includeInactive : includeInactiveWidget
								.getParameterValue() == '1' ? true : false
					}
				})

		nationalOrLocalWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			var newVal = nationalOrLocalWidget.getParameterValue()
			var showNationalOrLocalDependentWidgets = newVal == 'L'
			stationWidget.toggle(showNationalOrLocalDependentWidgets)
			serviceWidget.toggle(!showNationalOrLocalDependentWidgets)
			templateOnlyWidget.toggle(!showNationalOrLocalDependentWidgets)
			if (templateOnlyWidget.getParameterValue() == '0')
				serviceWidget.refresh()
		})

		stationWidget
				.addValueUpdatedListener(function(evt, updatedReportParams) {
					var newVal = stationWidget.getParameterValue()
					var showStationDependentWidgets = newVal && newVal != ''
					serviceWidget.toggle(showStationDependentWidgets)
					serviceWidget.refresh()
				})

		includeInactiveWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			var refreshTemplate = stationWidget.getParameterValue() == 'N'
					&& templateOnlyWidget.getParameterValue() == '1'
			if (refreshTemplate)
				serviceTemplateWidget.refresh()
			else
				serviceWidget.refresh()
		})

		gamesOnlyWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			var refreshTemplate = stationWidget.getParameterValue() == 'N'
					&& templateOnlyWidget.getParameterValue() == '1'
			if (refreshTemplate)
				serviceTemplateWidget.refresh()
			else
				serviceWidget.refresh()
		})

		templateOnlyWidget.addValueUpdatedListener(function(evt,
				updatedReportParams) {
			var newVal = templateOnlyWidget.getParameterValue()
			var showServiceTemplateWidget = newVal == '1'
			serviceTemplateWidget.toggle(showServiceTemplateWidget)
			serviceWidget.toggle(!showServiceTemplateWidget)
			serviceTemplateWidget.refresh()
		})

	})

	setPreSubmitCallback(function(finalReportParams) {

		if (getWidget("nationalOrLocal").getParameterValue() == "N"
				&& getWidget("templateOnly").getParameterValue() == "1") {
			delete finalReportParams['Scope']
			delete finalReportParams['stationId']
		}
		delete finalReportParams['GamesRelated']
		finalReportParams['Games'] = getWidget("gamesOnly").getParameterValue() == "Y"? '1' :'0'
		
		if(getWidget("includeInactive").getParameterValue() != '1') {
			finalReportParams['IsInactive'] = 0
		}
				
		return null
	})
</script>

<div
	style="display: inline-block; vertical-align: top; text-align: center; min-width: 250px;">
	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="nationalOrLocal"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Scope" />
		<tiles:putAttribute name="displayLabel" value="Facility" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="All Facilities|N" />
			<tiles:addAttribute value="Local|L|Checked" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="station" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="stationId" />
		<tiles:putAttribute name="displayLabel" value="Select Facility:" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
		<tiles:putAttribute name="mode" value="single" cascade="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<div
		style="display: inline-block; vertical-align: top; text-align: right; min-width: 250px;">
		<tiles:insertDefinition name="widgetCheckbox">
			<tiles:putAttribute name="widgetId" value="templateOnly"
				cascade="true" />
			<tiles:putAttribute name="reportParamName" value="TemplateOnly" />
			<tiles:putAttribute name="displayLabel"
				value="Service Templates Only" />
			<tiles:putAttribute name="submitAsReportParameters" value="false" />
			<tiles:putAttribute name="initialVisibility" value="hidden" />
		</tiles:insertDefinition>

		<tiles:insertDefinition name="widgetCheckbox">
			<tiles:putAttribute name="widgetId" value="includeInactive"
				cascade="true" />
			<tiles:putAttribute name="reportParamName" value="IsInactive" />
			<tiles:putAttribute name="displayLabel"
				value="Include Inactive Benefiting Services" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
		</tiles:insertDefinition>

		<tiles:insertDefinition name="widgetRadioButtons">
			<tiles:putAttribute name="widgetId" value="gamesOnly" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="GamesRelated " />
			<tiles:putAttribute name="displayLabel" value="Games" />
			<tiles:putAttribute name="submitAsReportParameters" value="false" />
			<tiles:putListAttribute name="items" cascade="true">
				<tiles:addAttribute value="Yes|Y" />
				<tiles:addAttribute value="No|N|Checked" />
			</tiles:putListAttribute>
		</tiles:insertDefinition>
	</div>

	<%-- =========================Service params --%>

	<tiles:insertDefinition name="widgetServiceSelector">
		<tiles:putAttribute name="widgetId" value="services" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ServiceID" />
		<tiles:putAttribute name="displayLabel" value="Benefiting Service" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetServiceTemplateSelector">
		<tiles:putAttribute name="widgetId" value="serviceTemplates"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="BeneServTempList" />
		<tiles:putAttribute name="displayLabel" value="Benefiting Service" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

</div>