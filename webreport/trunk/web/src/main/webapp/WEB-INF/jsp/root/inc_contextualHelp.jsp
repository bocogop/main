<%@ include file="../shared/inc_header.jsp"%>

<script src="${jsHome}/RoboHelp_CSH.js" type="text/javascript"></script>

<a id='helpLink' href="#help"><img
	style="border: 0px; margin-bottom: 15px" alt="Access Help for this Page"
	src="${imgHome}/context-sensitive-help-icon.jpg">
</a>

<tiles:importAttribute name="helpContextID" scope="page" ignore="true" />

<script type="text/javascript">
	$(function() {
		$('#helpLink').click(function(event) {
			<c:if test="${not empty pageScope.helpContextID}">
				RH_ShowMultiscreenHelpWithMapNo('/WebHelp/index.htm', '', ${pageScope.helpContextID})
			</c:if>
			<c:if test="${empty pageScope.helpContextID}">
				RH_ShowMultiscreenHelpWithMapNo('/WebHelp/index.htm', '', 0)
			</c:if>

			event.preventDefault()
		})
	})
</script>