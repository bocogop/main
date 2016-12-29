<%@ include file="../shared/inc_header.jsp"%>

<html>
<head>
<style>
@media print {
	.meal-ticket {
		page-break-after: always
	}
}
</style>
</head>

<body onload="window.opener.location.reload(false); window.print()">
<c:forEach items="${mealTickets}" var="entry" varStatus="loop">
<pre>${entry.value}</pre>
<c:if test="${loop.last}">
	<hr>
</c:if>
<c:if test="${not loop.last}">
	<hr class="meal-ticket" />
</c:if>
</c:forEach>
</body>
</html>