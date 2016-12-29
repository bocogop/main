<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="items" scope="page" />

<script type="text/javascript">
	registerWidget({
		getParameters : function() {
			return [ {
				displayName : "<c:out value="${widgetLabel}" />",
				paramName : "<c:out value="${widgetParamName}" />",
				paramValue : $("#${widgetId}").val()
			} ]
		},
		changeEventSelectors : [ "#${widgetId}" ]
	})
</script>

<c:out value="${widgetLabel}" />:
<select id="${widgetId}">
	<c:forEach var="item" items="${items}">

		<c:set var="tokens" value="${fn:split(item, '|')}" />
		<c:set var="name" value="${item}" />
		<c:set var="value" value="${item}" />
		<c:if test="${fn:length(tokens) > 1}">
			<c:set var="name" value="${tokens[0]}" />
			<c:set var="value" value="${tokens[1]}" />
		</c:if>

		<option value="${value}"><c:out value="${name}" /></option>
	</c:forEach>
</select>