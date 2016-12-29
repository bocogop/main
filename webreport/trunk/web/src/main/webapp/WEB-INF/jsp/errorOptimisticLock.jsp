<%@ include file="shared/inc_header.jsp"%>

<div align="center">
	Sorry, another user has just made changes to the same item that you are
	editing.
	<p>To proceed, please click &quot;Refresh Screen&quot; below to
		display the latest data before making further changes.</p>
</div>
<p align="center">
	<c:set var="url" value="${sessionScope.last_get_request_url}" />
	<c:if test="${empty url}">
		<c:set var="url" value="${home}/index.htm" />
	</c:if>
	<a class="backLink" href="${url}"> Refresh
		Screen</a> <img alt="Arrow signifying refresh screen" src="${imgHome}/right.gif"
		border="0" align="absmiddle" />
</p>