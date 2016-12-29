<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return ""
	}
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="facilityid" />
		<tiles:putAttribute name="displayLabel" value="Select Facilities:" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
	</tiles:insertDefinition>

</div>

