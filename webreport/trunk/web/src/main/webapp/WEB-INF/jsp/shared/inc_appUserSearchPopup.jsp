<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${empty appUserSearchJSIncluded}">
	<script type="text/javascript" src="${jsHome}/appUserSearch.js"></script>
	<c:set var="appUserSearchJSIncluded" value="true" scope="request" />
</c:if>

<%--
	Expects the following params:
	
	- A "uniqueSearchPopupId" param which should be a string of letters that
		uniquely identifies this appUserSearchPopup among other appUserSearchPopups on the same page
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the popup will activate when the user selects an item. The
		required method signature looks like:
		function someMethodWithUniqueName(itemObj)
		
		The itemObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the item selected>,
			[... other attributes of the AppUser class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupAppUserSearch(uid) and pass in the same
	uniqueSearchPopupId String above.
--%>

<%-- Escaping XML here for good practice since we use the raw EL expressions later --%>
<c:set var="uid"
	value="${fn:escapeXml(param.uniqueSearchPopupId)}" />
<c:set var="callbackMethod"
	value="${fn:escapeXml(param.resultCallbackMethod)}" />
<c:set var="cancelCallbackMethod"
	value="${fn:escapeXml(param.cancelCallbackMethod)}" />
<c:set var="includeLocalDB" value="${fn:escapeXml(param.includeLocalDB)}" />
<c:set var="includeLDAP" value="${fn:escapeXml(param.includeLDAP)}" />

<script type="text/javascript">
function appUserSearchPopupItemSelected${uid}(appUserId) {
	var appUserObj = appUserSearchResults['${uid}']['' + appUserId]
	$("#appUserSearchDialog${uid}").dialog('close')
	${callbackMethod}(appUserObj)
}
function appUserSearchPopupCancel${uid}() {
	<c:if test="${not empty cancelCallbackMethod}">
		${cancelCallbackMethod}()
	</c:if>
}
$(function() {
	initAppUserSearchPopup('${uid}', '${includeLocalDB}', '${includeLDAP}')
})
</script>

<div id="appUserSearchDialog${uid}" style="display: none"
	title="Search for User">
	<table align="center">
		<tr valign="middle">
			<td align="right"><label for='appUserSearchName${uid}'>Name:</label></td>
			<td><input type="text" id="appUserSearchName${uid}" value=""
				title="Type a name in the format Last[, First]"
				placeholder="Last[, First]" /></td>
			<td width="20">&nbsp;</td>
			<td align="right"><label
				for='appUserSearchADName${uid}'>Active Directory Name:</label></td>
			<td><input type="text"
				id="appUserSearchADName${uid}" value=""
				title="Type an active directory name" /></td>
			<td><a class="buttonAnchor search"
				id="appUserSearchLink${uid}" tabIndex="0">Search</a></td>
		</tr>
		<c:if test="${includeLDAP}">
		<tr valign="middle" align="center">
			<td colspan="6"><i>(add '*' to first and/or last name for wildcard match)</i></td>
		</tr>
		</c:if>
	</table>
	<div id="appUserSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no users were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</div>
	<div id="appUserSearchResultsTable${uid}" style="display: none">
		<table summary="Format table" align="center" width="98%">
			<tr>
				<td align="center">
					<table  id="appUserSearchResultsList${uid}"
						class="stripe" summary="List of App Users" width="100%">
						<thead>
							<tr>
								<th width="25%" id="nameHeaderCol${uid}">Name</th>
								<th width="15%">Title</th>
								<th width="15%">Department</th>
								<th width="15%">Office</th>
								<th width="15%">Phone</th>
								<th width="15%">Email</th>
							</tr>
						</thead>
					</table>
				</td>
			</tr>
		</table>
	</div>
</div>
