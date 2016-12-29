<%@ include file="../../shared/inc_header.jsp"%>

<c:if test="${not empty oneTimeUserNotification}">
	<div align="center" style="margin-bottom:10px;">
		<span class="oneTimeUserNotification"> <c:out
				value="${oneTimeUserNotification}" />
		</span>
	</div>
</c:if>
