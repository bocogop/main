<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return getWidget("type").getParameterValue() == "Summary" ? "_Summary" : ""
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')
		var stationWidget = getWidget('stationId')
		var visnWidget = getWidget('VISNId')
		var stateWidget = getWidget('StateId')
		var referenceWidget = getWidget('donationReference')
		var gpfWidget = getWidget('generalPostFund')
		
		$([ referenceWidget, gpfWidget ]).each(function(index, i) {
			i.setRefreshParamProvider(function() {
				return {
					stationId : stationWidget.getParameterValue()
				}
			})
		})
		
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
			referenceWidget.toggle(showStationDependentWidgets)
			referenceWidget.bypassReportParameters(!showStationDependentWidgets)
			gpfWidget.toggle(showStationDependentWidgets)
			gpfWidget.bypassReportParameters(!showStationDependentWidgets)
		})
		
		stationWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = stationWidget.getParameterValue()
			var showStationDependentWidgets = $.isArray(newVal) && newVal.length == 1
			referenceWidget.toggle(showStationDependentWidgets)
			referenceWidget.bypassReportParameters(!showStationDependentWidgets)
			gpfWidget.toggle(showStationDependentWidgets)
			gpfWidget.bypassReportParameters(!showStationDependentWidgets)
		})
	})

	setPreValidationsCallback(function() {
		/* Set any optional values here so they aren't flagged by the validator */
		var referenceWidget = getWidget('donationReference')
		var gpfVals = referenceWidget.getParameterValue()
		if (referenceWidget.length > 1) {
			for (var i = 0; i < referenceWidget.length; i++) {
				if (referenceWidget[i] == '-1') {
					return "Please uncheck the '(all)' Donation Reference option if individual values are selected."
				}
			}
		}
		
		var gpfWidget = getWidget('generalPostFund')
		var gpfVals = gpfWidget.getParameterValue()
		if (gpfVals.length > 1) {
			for (var i = 0; i < gpfVals.length; i++) {
				if (gpfVals[i] == '-1') {
					return "Please uncheck the '(all)' General Post Fund option if individual values are selected."
				}
			}
		}
	})
	
	setPreSubmitCallback(function(finalReportParams) {
		var scopeWidget = getWidget('scope')
		var referenceWidget = getWidget('donationReference')
		var gpfWidget = getWidget('generalPostFund')
		
		var theScope = scopeWidget.getParameterValue()
		if (theScope != 'S')
			finalReportParams['StateId'] = '-1'
		if (theScope != 'V')
			finalReportParams['VISNId'] = '-1'
		if (theScope != 'F') {
			finalReportParams['StationId'] = '-1'
		}
		
		if (getWidget("type").getParameterValue() == "Summary") {
			finalReportParams['StationId2'] = finalReportParams['StationId']
			delete finalReportParams['StationId']
		}
		
		if (!referenceWidget.visible)
			finalReportParams['Reference'] = '-1'
		if (!gpfWidget.visible)
			finalReportParams['GPF'] = '-1'
	})
	
</script>

<div style="display: inline-block; vertical-align: top; text-align:right; min-width:250px;">
	<tiles:insertDefinition name="widgetSelectList">
		<tiles:putAttribute name="widgetId" value="type" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Type" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Summary" />
			<tiles:addAttribute value="Detailed" />
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

<tiles:insertDefinition name="widgetDateRange">
		<tiles:putAttribute name="widgetId" value="dateRange" cascade="true" />
		<tiles:putAttribute name="reportParamNameBegin" value="BeginDate"
			cascade="true" />
		<tiles:putAttribute name="reportParamNameEnd" value="EndDate"
			cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Date Range" />
		<tiles:putAttribute name="initialEndValue" cascade="true">
		<wr:localDate date="${currentDate}" pattern="${TWO_DIGIT_DATE_ONLY}" />
		</tiles:putAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="showParams" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ShowParams" />
		<tiles:putAttribute name="displayLabel" value="Show Params" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Yes|Y" />
			<tiles:addAttribute value="No|N|checked" />
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
	
	<tiles:insertDefinition name="widgetDonorTypeSelector">
		<tiles:putAttribute name="widgetId" value="donorType" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="DonorType" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Donor Type" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition>
	
	<tiles:insertDefinition name="widgetDonationTypeSelector">
		<tiles:putAttribute name="widgetId" value="donationType" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="DonationType" />
		<tiles:putAttribute name="displayLabel" value="Donation Type" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition>
	
	<tiles:insertDefinition name="widgetDonationReferenceSelector">
		<tiles:putAttribute name="widgetId" value="donationReference" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Reference" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Donation Reference" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden"
			cascade="true" />
	</tiles:insertDefinition>
	
	<tiles:insertDefinition name="widgetGeneralPostFundSelector">
		<tiles:putAttribute name="widgetId" value="generalPostFund" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="GPF" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="General Post Fund (GPF)" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden"
			cascade="true" />
	</tiles:insertDefinition>
</div>
