<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return "_" + getWidget("scope").getParameterValue() + '_v1'
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')
		var stationWidget = getWidget('stationId')
		var ageRangeWidget = getWidget('ageRange')
		var birthMonthWidget = getWidget('birthMonth')
		var organizationsWidget = getWidget('organizations')
		var benefitingServicesWidget = getWidget('benefitingServices')
		var volunteersWidget = getWidget('volunteers')
		var zipCodesWidget = getWidget('zipCodes')
		
		$([ organizationsWidget, benefitingServicesWidget, volunteersWidget, zipCodesWidget ]).each(function(index, i) {
			i.setRefreshParamProvider(function() {
				return {
					stationId : stationWidget.getParameterValue()
				}
			})
		})

		scopeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = scopeWidget.getParameterValue()

			ageRangeWidget.toggle(newVal == 'Age')
			ageRangeWidget.bypassReportParameters(newVal != 'Age')

			birthMonthWidget.toggle(newVal == 'BirthMonth')
			birthMonthWidget.bypassReportParameters(newVal != 'BirthMonth')

			organizationsWidget.toggle(newVal == 'Organizations')
			organizationsWidget.bypassReportParameters(newVal != 'Organizations')

			benefitingServicesWidget.toggle(newVal == 'Services')
			benefitingServicesWidget.bypassReportParameters(newVal != 'Services')
			
			volunteersWidget.toggle(newVal == 'SpecificVolunteers')
			volunteersWidget.bypassReportParameters(newVal != 'SpecificVolunteers')
			
			zipCodesWidget.toggle(newVal == 'Zip')
			zipCodesWidget.bypassReportParameters(newVal != 'Zip')
		})

		stationWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			organizationsWidget.refresh()
			benefitingServicesWidget.refresh()
			volunteersWidget.refresh()
			zipCodesWidget.refresh()
		})
	})

	function ageValidator(inputWithValidationFailure) {
		// reference inputWithValidationFailure.validity or $(inputWithValidationFailure).attr('id')
		// if desired - CPB
		return "Please enter age values between 0 and 120."
	}

	function skipLabelValidator(inputWithValidationFailure) {
		return "Please enter a skip labels value between 0 and 27."
	}
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetSelectList">
		<tiles:putAttribute name="widgetId" value="scope" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Filter By" />
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Age" />
			<tiles:addAttribute value="Birth Month|BirthMonth" />
			<tiles:addAttribute value="Organizations" />
			<tiles:addAttribute value="Services" />
			<tiles:addAttribute value="Selected Volunteers|SpecificVolunteers" />
			<tiles:addAttribute value="All" />
			<tiles:addAttribute value="All VAVS Reps|All_VAVS" />
			<tiles:addAttribute value="Zip Code|Zip" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="StationId" />
		<tiles:putAttribute name="displayLabel" value="Select facility" />
		<tiles:putAttribute name="mode" value="single" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="true"
			cascade="true" />
	</tiles:insertDefinition>

	<%-- ====================================== Age params --%>

	<tiles:insertDefinition name="widgetAgeRange">
		<tiles:putAttribute name="widgetId" value="ageRange" cascade="true" />
		<tiles:putAttribute name="reportParamNameFrom" value="AgeFrom"
			cascade="true" />
		<tiles:putAttribute name="reportParamNameTo" value="AgeTo"
			cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Age Range" />
		<tiles:putAttribute name="htmlValidationFailureMessageProvider"
			value="ageValidator" />
	</tiles:insertDefinition>

	<%-- ====================================== Birth Month params --%>

	<tiles:insertDefinition name="widgetSelectList">
		<tiles:putAttribute name="widgetId" value="birthMonth" cascade="true" />
		<tiles:putAttribute name="displayLabel" value="Birth Month" />
		<tiles:putAttribute name="reportParamName" value="BirthMonth" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="January (1)|1" />
			<tiles:addAttribute value="February (2)|2" />
			<tiles:addAttribute value="March (3)|3" />
			<tiles:addAttribute value="April (4)|4" />
			<tiles:addAttribute value="May (5)|5" />
			<tiles:addAttribute value="June (6)|6" />
			<tiles:addAttribute value="July (7)|7" />
			<tiles:addAttribute value="August (8)|8" />
			<tiles:addAttribute value="September (9)|9" />
			<tiles:addAttribute value="October (10)|10" />
			<tiles:addAttribute value="November (11)|11" />
			<tiles:addAttribute value="December (12)|12" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<%-- ====================================== Organization params --%>

	<tiles:insertDefinition name="widgetOrganizationSelector">
		<tiles:putAttribute name="widgetId" value="organizations"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="OrganizationIds" />
		<tiles:putAttribute name="displayLabel" value="Organizations" />
		<tiles:putAttribute name="includeLocal" value="true" cascade="true" />
		<tiles:putAttribute name="includeNational" value="true" cascade="true" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<%-- ====================================== Services params --%>

	<tiles:insertDefinition name="widgetBenefitingServiceSelector">
		<tiles:putAttribute name="widgetId" value="benefitingServices"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Services" />
		<tiles:putAttribute name="displayLabel" value="Benefiting Services" />
		<tiles:putAttribute name="excludeGames" value="true" cascade="true" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<%-- ====================================== Volunteers params --%>

	<tiles:insertDefinition name="widgetVolunteerSelector">
		<tiles:putAttribute name="widgetId" value="volunteers" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="VolunteerIds" />
		<tiles:putAttribute name="displayLabel" value="Selected Volunteers" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>
	
	<%-- ====================================== Zip Code params --%>

	<tiles:insertDefinition name="widgetZipCodeSelector">
		<tiles:putAttribute name="widgetId" value="zipCodes" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ZipCodes" />
		<tiles:putAttribute name="displayLabel" value="Selected Zip Codes" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="bypassAsReportParameters" value="true" />
		<tiles:putAttribute name="initialVisibility" value="hidden" />
	</tiles:insertDefinition>

	<%-- ====================================== Common params --%>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="sortOrder" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="SortOrder" />
		<tiles:putAttribute name="displayLabel" value="Sort By" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Volunteer Name|Name" />
			<tiles:addAttribute value="Zip Code|Zip" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="emailOption" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="EmailOption" />
		<tiles:putAttribute name="displayLabel" value="Email Option" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Volunteers with No Email Address|NoEmail" />
			<tiles:addAttribute value="All Volunteers|All" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetTextInput">
		<tiles:putAttribute name="widgetId" value="skipLabels" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="SkipLabels" />
		<tiles:putAttribute name="displayLabel"
			value="Number of Labels to Skip" />
		<tiles:putAttribute name="inputType" value="number" cascade="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='value="0" min="0" max="27" style="width:50px"' cascade="true" />
		<tiles:putAttribute name="htmlValidationFailureMessageProvider"
			value="skipLabelValidator" />
	</tiles:insertDefinition>
</div>

<div style="display: inline-block; vertical-align: top"></div>
