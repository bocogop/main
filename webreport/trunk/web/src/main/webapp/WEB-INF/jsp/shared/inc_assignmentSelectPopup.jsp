<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uniqueId" param which should be a string of letters that
		uniquely identifies this assignmentSelectPopup among other assignmentSelectPopups on the same page
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which this popup will activate when the user selects a facility assignment. The
		required method signature looks like:
		function someMethodWithUniqueName(assignmentObj)
		
		The assignmentObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <a unique number for this available assignment>,
			[... other attributes of the Assignment class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupAssignmentSelect(uid) and pass in the same
	uniqueId String above.
--%>

<c:set var="uid" value="${fn:escapeXml(param.uniqueId)}" />

<script type="text/javascript">
	$(function() {
		initAssignmentSelectPopup({
			uid : "${uid}",
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />,
			volunteerEditPermission: "<c:out value="${PERMISSION_TYPE_VOLUNTEER_CREATE}" />"
		})
	})
</script>

<div id="assignmentSelectDialog${uid}"
	title="Select Assignment">
	<table>
		<tr>
			<td class='appFieldLabel'>Facility:<span
				class="invisibleRequiredFor508">*</span></td>
			<td><span class='requdIndicator'>*</span></td>
			<td><div id="assignmentFacilityWrapper">
					<select id="assignmentFacilityId">
						<c:if test="${not empty facilityContextId}">
							<option value="${facilityContextId}" selected="selected"><c:out
									value="${facilityContextName}" /></option>
						</c:if>
					</select>
				</div></td>
		</tr>
	</table>
	
	<div id="assignmentSelectTable${uid}">

		<table id="assignmentSelectList${uid}" class="stripe"
			summary="List of Facility Assignments">
			<thead>
				<tr id="assignmentSelect${uid}FilterRow">
					<td width="33%" class="noborder" title="Filter by Service Name"></td>
					<td width="34%" class="noborder" title="Filter by Service Role"></td>
					<td width="33%" class="noborder" title="Filter by Physical Location"></td>
				</tr>
				<tr>
					<th width="33%" class="select-filter">Service Name</th>
					<th width="34%" class="select-filter">Service Role</th>
					<th width="33%" id="facilityHeaderCol${uid}" class="select-filter">Physical Location</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
