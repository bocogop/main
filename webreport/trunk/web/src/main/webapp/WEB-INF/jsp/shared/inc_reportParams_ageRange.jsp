<%@ include file="../shared/inc_header.jsp"%>

<%-- Escaping XML here for good practice since we use the raw EL expressions later --%>
<c:set var="reportParamFromId" value="${fn:escapeXml(param.reportParamFromId)}" />
<c:set var="reportParamFromPrompt"
	value="${fn:escapeXml(param.reportParamFromPrompt)}" />
<c:set var="reportParamToId" value="${fn:escapeXml(param.reportParamToId)}" />
<c:set var="reportParamToPrompt"
	value="${fn:escapeXml(param.reportParamToPrompt)}" />
<c:set var="numFromMin"
	value="${fn:escapeXml(param.numFromMin)}" />
<c:set var="numFromMax"
value="${fn:escapeXml(param.numFromMax)}" />
<c:set var="numToMin"
value="${fn:escapeXml(param.numToMin)}" />
<c:set var="numToMax"
value="${fn:escapeXml(param.numToMax)}" />
	
	
 

<div id='${reportParamFromId}_container_div' class="ssrsParametersContainerDiv">    
    <label for='${reportParamFromId}'>${reportParamFromPrompt}:</label>    
    <select class="ssrsParameters" id="${reportParamFromId}" name="${reportParamFromId}">  
    	<c:forEach begin="${numFromMin}" end="${numFromMax}" varStatus="loop">
   			 <option value="${loop.index}">${loop.index}</option>
		</c:forEach>    
    </select>
    <label for='${reportParamToId}'>${reportParamToPrompt}:</label>    
    <select class="ssrsParameters" id="${reportParamToId}" name="${reportParamToId}">
    	<c:forEach begin="${numToMin}" end="${numToMax}" varStatus="loop">
   			 <option value="${loop.index}">${loop.index}</option>
		</c:forEach>      
    </select>
 </div> 



 <script type="text/javascript">
	
	$(function() {
		
		$('#${reportParamFromId}').selectmenu();
		$( "#${reportParamFromId}" ).selectmenu( "option", "width", 100 );
		$('#${reportParamToId}').selectmenu();
		$( "#${reportParamToId}" ).selectmenu( "option", "width", 100 );
		
	});
	
</script>