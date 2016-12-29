<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return "_" + getWidget("scope").getParameterValue() + '_Report'
	}

	setWidgetsInitializedCallback(function() {
		var scopeWidget = getWidget('scope')
		var organizationsWidget = getWidget('organizations')
		
		scopeWidget.addValueUpdatedListener(function(evt, updatedReportParams) {
			var newVal = scopeWidget.getParameterValue()

			var usesOrg = (newVal == 'Representative' || newVal == 'Deputies' || newVal == 'CertifyingOfficial')
			organizationsWidget.toggle(usesOrg)
			organizationsWidget.bypassReportParameters(!usesOrg)
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
		<%-- 		<tiles:putAttribute name="reportParamName" value="SearchType" />
 --%>
		<tiles:putAttribute name="submitAsReportParameters" value="false" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Representatives|Representative" />
			<tiles:addAttribute value="Deputies" />
			<tiles:addAttribute
				value="National Certifying Officials|CertifyingOfficial" />
			<tiles:addAttribute
				value="Only Reps in Organizations defined as NAC Executive Committee Members|NACExecutiveMems" />
			<tiles:addAttribute
				value="All National Officials not Reps/Deps/NCOs|NotRepDepOrNACExecMems" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetOrganizationSelector">
		<tiles:putAttribute name="widgetId" value="organizations"
			cascade="true" />
		<tiles:putAttribute name="reportParamName"
			value="NationalOrganizationsIdParam" />
		<tiles:putAttribute name="displayLabel" value="Organizations" />
		<tiles:putAttribute name="includeLocal" value="false" cascade="true" />
		<tiles:putAttribute name="includeNational" value="true" cascade="true" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="sortOrder" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="NationalOrgsSortCol" />
		<tiles:putAttribute name="displayLabel" value="Sort By" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="Contact Name|Contact" />
			<tiles:addAttribute value="Organization Name|Name" />
		</tiles:putListAttribute>
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetRadioButtons">
		<tiles:putAttribute name="widgetId" value="emailOption" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="EmailOption" />
		<tiles:putAttribute name="displayLabel" value="Email Option" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putListAttribute name="items" cascade="true">
			<tiles:addAttribute value="All National Officials|All" />
			<tiles:addAttribute
				value="National Officials with No Email Address|NoEmail" />
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