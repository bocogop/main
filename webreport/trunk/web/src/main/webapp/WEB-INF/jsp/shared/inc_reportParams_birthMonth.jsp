<%@ include file="../shared/inc_header.jsp"%>

<%-- Escaping XML here for good practice since we use the raw EL expressions later --%>
<c:set var="reportParamId" value="${fn:escapeXml(param.reportParamId)}" />
<c:set var="reportParamPrompt"
	value="${fn:escapeXml(param.reportParamPrompt)}" />

 

<div id='${reportParamId}_container_div' class="ssrsParametersContainerDiv">    
    <label for='${reportParamId}'>${reportParamPrompt}:</label>    
    <select class="ssrsParameters" multiple="multiple" id="${reportParamId}" name="${reportParamId}">    	
   			 <option value="1">Jan</option>
   			 <option value="2">Feb</option>
   			 <option value="3">Mar</option>
   			 <option value="4">Apr</option>
   			 <option value="5">May</option>
   			 <option value="6">Jun</option>
   			 <option value="7">Jul</option>
   			 <option value="8">Aug</option>
   			 <option value="9">Sep</option>
   			 <option value="10">Oct</option>
   			 <option value="11">Nov</option>
   			 <option value="12">Dec</option>
	</select>    
 </div> 



 <script type="text/javascript">
	
	$(function() {
		
		$('#${reportParamId}').selectmenu();
		$( "#${reportParamId}" ).selectmenu( "option", "width", 100 );		
		
	});
	
</script>