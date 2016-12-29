<%@ include file="../shared/inc_header.jsp"%>

<table cellspacing="6">
	<tr>
		<c:forEach items="${app_breadcrumbs.breadcrumbs}"
			var="breadcrumb" varStatus="loop">
			<td><img alt="right arrow" src="${imgHome}/right.gif" alt="breadcrumb indicator"/></td>
			<td nowrap>
			<%--
			<c:if
				test="${not empty breadcrumb.link.href and not loop.last}">
				<a href="${breadcrumb.link.href}"><c:out
					value="${breadcrumb.link.text}" /> </a>
			</c:if> 
			<c:if test="${empty breadcrumb.link.href or loop.last}">
				<c:out value="${breadcrumb.link.text}" />
			</c:if> --%>
			<a class="appLink" href="<c:out value="${breadcrumb.link.href}" />"><c:out
					value="${breadcrumb.link.text}" /> </a>
			</td>
		</c:forEach>
	</tr>
</table>
