<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">

	function getReportPathExtension() {
		return ""
	}

	setWidgetsInitializedCallback(function() {
		var natTitleWidget = getWidget('VAVSTitle')
		var executiveCommitteeWidget = getWidget('executiveCommittee')
		var natCertifyingOfficialWidget = getWidget('certifyingOfficial')
		var organizationsWidget = getWidget('organizations')
	})
	
</script>

<div
	style="display: inline-block; vertical-align: top; text-align: right; min-width: 300px;">
	
		<%-- ====================================== NAC Vavs title param 	--%>

		<tiles:insertDefinition name="widgetVAVSTitleSelector">
			<tiles:putAttribute name="widgetId" value="VAVSTitle" cascade="true" />
			<tiles:putAttribute name="reportParamName" value="NACTitle" />
			<tiles:putAttribute name="displayLabel" value="NAC Title" />
			<tiles:putAttribute name="mode" value="multiple" cascade="true" />
			<tiles:putAttribute name="submitAsReportParameters" value="true" />
		</tiles:insertDefinition>

	
	<%-- ====================================== checkbox params--%>

	<tiles:insertDefinition name="widgetCheckbox">
		<tiles:putAttribute name="widgetId" value="executiveCommittee" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="ExecMember" />
		<tiles:putAttribute name="displayLabel" value="NAC Executive Committee Members?" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition> 

	<tiles:insertDefinition name="widgetCheckbox">
		<tiles:putAttribute name="widgetId" value="certifyingOfficial" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="NCO" />
		<tiles:putAttribute name="displayLabel" value="National Certifying Officials?" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition> 
</div>

<div text-align: center">

		<%-- ====================================== Organization params --%>

		
		<tiles:insertDefinition name="widgetOrganizationSelector">
		<tiles:putAttribute name="widgetId" value="organizations"
			cascade="true" />
		<tiles:putAttribute name="reportParamName" value="OrgName" />
		<tiles:putAttribute name="displayLabel" value="NAC Organizations" />
		<tiles:putAttribute name="includeLocal" value="false" cascade="true" />
		<tiles:putAttribute name="includeNational" value="true" cascade="true" />
	 	<tiles:putAttribute name="nacOrgsOnly" value="true" cascade="true" /> 
		<tiles:putAttribute name="preselectAll" value="true" cascade="true" />
		<tiles:putAttribute name="mode" value="multiple" cascade="true" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
	</tiles:insertDefinition>
		


</div>
