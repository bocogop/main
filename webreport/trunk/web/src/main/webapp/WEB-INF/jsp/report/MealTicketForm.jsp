<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return ""
	}
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetDateInput">
		<tiles:putAttribute name="widgetId" value="date" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="Date" />
		<tiles:putAttribute name="displayLabel"
			value="Date" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='style="width:110px"' cascade="true" />
		<tiles:putAttribute name="initialValue" cascade="true"><wr:localDate date="${currentDate}" pattern="${TWO_DIGIT_DATE_ONLY}" /></tiles:putAttribute>
	</tiles:insertDefinition>
	
	<tiles:insertDefinition name="widgetHiddenInput">
		<tiles:putAttribute name="widgetId" value="facilityId" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="FacilityID" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="initialValue" value="${facilityContextId}" cascade="true" />
	</tiles:insertDefinition>
</div>