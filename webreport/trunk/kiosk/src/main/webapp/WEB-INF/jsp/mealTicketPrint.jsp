<%@ include file="shared/inc_header.jsp"%>

<style>
</style>
<a class="buttonAnchor" href="${home}/postTime.htm"
	style="margin-left: 20px"><img src="${imgHome}/big-left-arrow.png"
	hspace="5" align="absmiddle" /> <spring:message code="timePosting" /></a>

<div class="clearCenter mealTicketContainer" style="margin-top: 100px">
	<spring:message code="printMealTicketQuestion"
		arguments="${numMealTickets}" />
	<div style="margin-top: 50px" class="clearCenter">
		<a href="${home}/printTickets.htm" class="buttonAnchor"><spring:message
				code="printMealTickets" /></a> <a style="margin-left: 40px"
			href="${home}/logout.htm?thankYou=true&locale=${locale}" class="buttonAnchor"><spring:message code="doNotPrintMealTickets" /></a>
	</div>
</div>