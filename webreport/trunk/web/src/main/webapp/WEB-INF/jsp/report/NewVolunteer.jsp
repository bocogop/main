<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	function getReportPathExtension() {
		return "_Report"
	}
</script>

<div style="display: inline-block; vertical-align: top">
	<tiles:insertDefinition name="widgetStationSelector">
		<tiles:putAttribute name="widgetId" value="stationId" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="StationId" />
		<tiles:putAttribute name="displayLabel" value="Select Facilities:" />
		<tiles:putAttribute name="bypassAsReportParameters" value="false"
			cascade="true" />
		<tiles:putAttribute name="showAllFacilities" value="false"
			cascade="true" />
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

<%-- 	<tiles:insertDefinition name="widgetDateInput">
		<tiles:putAttribute name="widgetId" value="beginDate" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="BeginDate" />
		<tiles:putAttribute name="displayLabel" value="Begin Date" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='style="width:110px"' cascade="true" />
	</tiles:insertDefinition>

	<tiles:insertDefinition name="widgetDateInput">
		<tiles:putAttribute name="widgetId" value="endDate" cascade="true" />
		<tiles:putAttribute name="reportParamName" value="EndDate" />
		<tiles:putAttribute name="displayLabel" value="End Date" />
		<tiles:putAttribute name="submitAsReportParameters" value="true" />
		<tiles:putAttribute name="textInputAttributes"
			value='style="width:110px"' cascade="true" />
		<tiles:putAttribute name="initialValue" cascade="true">
			<wr:localDate date="${currentDate}" pattern="${TWO_DIGIT_DATE_ONLY}" />
		</tiles:putAttribute>
	</tiles:insertDefinition>
	
	--%>
</div>

