<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="showAllOrganizations" scope="page"
	ignore="true" />

<%-- If false, we could only show local organizations for stations to which the user is assigned? CPB --%>
<c:if test="${empty showAllOrganizations}">
	<c:set var="showAllOrganizations" value="false" />
</c:if>

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				var table = $("#orgTable").DataTable(
						{
							"ajax" : ajaxHomePath + "/organization/quickSearch",
							"columns" : [
									{
										"render" : function(data, type, full, meta) {
											return $.trim(full.facility) == '' ? 'National' : full.facility
										}
									},
									{
										"render" : function(data, type, full, meta) {
											if (type == 'display')
												return '<a href=\'javascript:addOrg(' + full.id + ', "' + full.name
														+ '")\'>' + full.name + '</a>'
											return full.name
										}
									}, {
										"data" : "abbreviation"
									} ],
							"dom" : '<"top"f>rt<"bottom"l>',
							"language" : {
								zeroRecords : "", //Please search above by name or VA username.",
								search : "", // Search",
								searchPlaceholder : "Search by name or station..."
							},
							"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
							"paging" : false,
							"processing" : true,
							"scrollY" : "200px",
							"scrollCollapse" : true,
							"serverSide" : true,
						})
			},
			getParameters : function() {
				var theVal = $("#" + widgetId).val()
				return [ {
					displayName : "<c:out value="${widgetLabel}" />",
					paramName : "<c:out value="${widgetParamName}" />",
					paramValue : theVal || ''
				} ]
			},
			changeEventSelectors : [ "#" + widgetId ]
		})
	})()
</script>

<fieldset style="text-align:left">
	<legend>
		<c:out value="${widgetLabel}" />
	</legend>
	<div class="dataTableCenterFilter orgTableWrapper">
		<table id="orgTable" class="display" cellspacing="0" width="100%">
			<thead>
				<%--
				<tr>
					<td class="noborder" id="facilityFilter"
						title="Filter facility by selecting one of these options"></td>
					<td class="noborder"></td>
					<td class="noborder"></td>
				</tr>
				 --%>
				<tr>
					<th width="30%" class="select-filter">Facility</th>
					<th width="50%">Name</th>
					<th width="10%">Abbrev</th>
				</tr>
			</thead>
		</table>
	</div>
	<div>Selected Organizations: <</div>
</fieldset>