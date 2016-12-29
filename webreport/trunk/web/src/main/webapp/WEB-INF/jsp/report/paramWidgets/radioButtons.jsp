<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="items" scope="page" />

<script type="text/javascript">
	registerWidget({
		getParameters : function() {
			return [{
				displayName: "<c:out value="${widgetLabel}" />",
				paramName: "<c:out value="${widgetParamName}" />",
				paramValue : $("input[type='radio'][name='${widgetId}ButtonGroup']:checked")
						.val()
			}]
		},
		changeEventSelectors : [ "input[type='radio'][name='${widgetId}ButtonGroup']" ]
	})
</script>

<c:out value="${widgetLabel}" />
:
<c:forEach var="item" items="${items}">
	<c:set var="tokens" value="${fn:split(item, '|')}" />
	<c:set var="name" value="${item}" />
	<c:set var="value" value="${item}" />
	<c:set var="addlAttrs" value="" />
	<c:if test="${fn:length(tokens) > 1}">
		<c:set var="name" value="${tokens[0]}" />
		<c:set var="value" value="${tokens[1]}" />
	</c:if>
	<c:if test="${fn:length(tokens) > 2}">
		<c:set var="addlAttrs" value="${tokens[2]}" />
	</c:if>

	<input type="radio" value="${value}" name="${widgetId}ButtonGroup"
		${addlAttrs}>
	<c:out value="${name}" />
</c:forEach>
