<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var changesMade = false
	
	var letterData = {}
	<c:forEach items="${letterMap}" var="entry">
		letterData["${entry.key}"] = {
			showHeader : ${entry.value.showHeader},
			showFooter : ${entry.value.showFooter},
		}
	</c:forEach>
	
	var defaultLetterData = {}
	<c:forEach items="${defaultLetterMap}" var="entry">
		defaultLetterData["${entry.key}"] = {
			showHeader : ${entry.value.showHeader},
			showFooter : ${entry.value.showFooter},
			placeholders : [
			<c:forEach items="${entry.value.placeholders}" var="placeholder">
				{
					name : "<c:out value="${placeholder.name}" />",
					description : "<c:out value="${placeholder.description}" />",
				},
			</c:forEach>
   			]
		}
	</c:forEach>
			
	function updateBody() {
		var selectedType = $("#type").val()
		var typeSelected = selectedType != ''
		$(".updateFields").toggle(typeSelected)
		
		if (typeSelected) {
			var letterTemplate = letterData[selectedType]
			
			$(".updateFields textarea").hide()
			$(".updateFields .placeholderTable").hide()
			$(".overrideDiv").hide()
			
			$("#body_" + selectedType).show()
			$("#overrideDiv_" + selectedType).show()
			$("#placeholders_" + selectedType).show()
			
			updateInputs()
		}
	}
	
	function updateInputs() {
		var selectedType = $("#type").val()
		if (selectedType == '') return
		
		var isOverride = $("input[name='override_" + selectedType + "']:checked").val() == 'true'
		$("#body_" + selectedType).toggle(isOverride)
		$("#body_" + selectedType + "_default").toggle(!isOverride)
		
		$("#headerFooterCheckboxes_" + selectedType).toggle(isOverride)
		$("#headerFooterCheckboxes_" + selectedType + "_default").toggle(!isOverride)
		
	}
	
	function submitForm() {
		var type = $('#type').val()
		var isOverride = $("input[name='override_" + type + "']:checked").val() == 'true'
		var s = $("#body_" + type + (isOverride ? "" : "_default")).val() 
		
		var matches = []
		var pattern = /(\[.*?\])/g
		var match;
		while ((match = pattern.exec(s)) != null) {
		  matches.push(match[1])
		}
		
		var placeholders = defaultLetterData[type].placeholders
		var unknownPlaceholders = []
		
		outer:
		for (var matchIndex = 0; matchIndex < matches.length; matchIndex++) {
			for (var pIndex = 0; pIndex < placeholders.length; pIndex++) {
				if (matches[matchIndex] == placeholders[pIndex].name)
					continue outer;
			}
			unknownPlaceholders.push(matches[matchIndex])
		}
		
		if (unknownPlaceholders.length > 0) {
			var str = 'The following placeholders were entered but are not valid for this letter type.'
				+ ' Please check spelling & ensure no spaces exist inside the brackets.'
				+ ' <ul>'
			for (var i = 0; i < unknownPlaceholders.length; i++)
				str += '<li>' + unknownPlaceholders[i] + '</li>'
			str += '</ul>'
			displayAttentionDialog(str)
			return false
		}
		
		return true
	}
	
	var previousType;

	$(function() {
		$(".overrideCheckbox").change(updateInputs)
		
		$(".allInputs").change(function() {
			changesMade = true
		})
		
		$(".overrideFalse").prop('checked', true)
		<c:forEach items="${letterMap}" var="entry">
			$("#override_${entry.key}True").prop('checked', true)
		</c:forEach>
		<c:if test="${facilityContextIsCentralOffice}">
			$(".overrideTrue").prop('checked', true)
			$(".overrideFalse").prop('disabled', true)
		</c:if>
		
		$("#cancelFormButton").click(function() {
			 if (changesMade) {
				 confirmDialog("Save changes to this template?", function() {
					 $("#templateForm").submit()
				 },{
					cancelCallback : function() {
						document.location.href="${current_breadcrumb}"
				 	}
			 	})
			 } else {
				 document.location.href="${current_breadcrumb}"	
			 }
		})
		
		updateBody()
		
		$('#type').focus(function() {
			previousType = $(this).val()
		}).change(function() {
		    if (changesMade) {
		    	var newVal = $(this).val()
			    $(this).blur() // Firefox fix
		    	var that = $(this)
		    	
				confirmDialog('Save changes to this template?', function() {
					that.val(previousType)
					updateBody()
					$("#templateForm").submit()
				}, {
					cancelCallback : function() {
						updateBody()
						changesMade = false
					}
				})
				return false
			} else {
				updateBody()
			}
		})
	})
</script>

<form id="templateForm" method="post" action="${home}/letterTemplateSubmit.htm" onsubmit="return submitForm();">
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

	<div class="clearCenter">
		<fieldset>
			<table>
				<tr>
					<td class='appFieldLabel'><label for='type'>Letter
							Template Type:</label></td>
					<td><select name="type" id="type">
							<option value="">--Please Select--</option>
							<c:forEach items="${allLetterTypes}" var="letterType">
								<c:set var="selectedText" value="" />
								<c:if test="${selectedLetterType == letterType}">
									<c:set var="selectedText" value=" selected" />
								</c:if>
								<option value="${letterType.code}" ${selectedText}><c:out
										value="${letterType.name}" /></option>
							</c:forEach>
					</select></td>
				</tr>
			</table>
		</fieldset>
	</div>

	<div class="clearCenter updateFields" style="display: none">
		<div class="leftHalf">
			<fieldset>
				<legend>Template Details</legend>

				<c:forEach items="${allLetterTypes}" var="letterType">
					<c:set var="defaultHeaderChecked"
						value="${not empty defaultLetterMap[letterType.code] and defaultLetterMap[letterType.code].showHeader ? ' checked' : ''}" />
					<c:set var="defaultFooterChecked"
						value="${not empty defaultLetterMap[letterType.code] and defaultLetterMap[letterType.code].showFooter ? ' checked' : ''}" />
					<c:if test="${not empty letterMap[letterType.code]}">
						<c:set var="headerChecked"
							value="${letterMap[letterType.code].showHeader ? ' checked' : ''}" />
						<c:set var="footerChecked"
							value="${letterMap[letterType.code].showFooter ? ' checked' : ''}" />
					</c:if>
					<c:if test="${empty letterMap[letterType.code]}">
						<c:set var="headerChecked" value="${defaultHeaderChecked}" />
						<c:set var="footerChecked" value="${defaultFooterChecked}" />
					</c:if>

					<div align="center" class="overrideDiv"
						id="overrideDiv_${letterType.code}" style="display: none">
						<table>
							<tr>
								<td><input type="radio"
									class="overrideCheckbox overrideFalse allInputs"
									name="override_${letterType.code}"
									id="override_${letterType.code}False" value="false" />Use
									Default <input type="radio"
									class="overrideCheckbox overrideTrue allInputs"
									name="override_${letterType.code}"
									id="override_${letterType.code}True" value="true" />Customize</td>
								<td width="400"><img alt="" src="${imgHome}/spacer.gif" height="1"
									width="25" /></td>
								<td>
									<div id="headerFooterCheckboxes_${letterType.code}"
										style="visibility: none">
										<input type="checkbox" class="showHeaderCheckbox allInputs"
											name="showHeader_${letterType.code}"
											id="showHeader_${letterType.code}" value="true"
											${headerChecked} />Show Header<br /> <input type="checkbox"
											class="showFooterCheckbox allInputs"
											name="showFooter_${letterType.code}"
											id="showFooter_${letterType.code}" value="true"
											${footerChecked} />Show Footer
									</div>
									<div id="headerFooterCheckboxes_${letterType.code}_default"
										style="visibility: none">
										<input type="checkbox" class="showHeaderCheckbox"
											name="showHeader_${letterType.code}_default"
											id="showHeader_${letterType.code}_default" value="true" disabled ${defaultHeaderChecked} />Show
										Header<br /> <input type="checkbox"
											class="showFooterCheckbox"
											name="showFooter_${letterType.code}_default"
											id="showFooter_${letterType.code}_default" value="true" disabled ${defaultFooterChecked} />Show
										Footer
									</div>
								</td>
							</tr>
						</table>

					</div>
				</c:forEach>
				<p />
				<div align="left">Body</div>
				<p />
				<c:forEach items="${allLetterTypes}" var="letterType">
					<c:set var="localLetter"
						value="${defaultLetterMap[letterType.code]}" />
					<c:if test="${not empty letterMap[letterType.code]}">
						<c:set var="localLetter" value="${letterMap[letterType.code]}" />
					</c:if>

					<textarea id="body_${letterType.code}" class="allInputs" style="display: none"
						name="body_${letterType.code}" rows="25" cols="120"><c:out
							value="${localLetter.body}" /></textarea>

					<textarea id="body_${letterType.code}_default"
						style="display: none" disabled
						name="body_${letterType.code}_default" rows="25" cols="120"><c:out
							value="${defaultLetterMap[letterType.code].body}" /></textarea>
				</c:forEach>
		</div>
		<div class="rightHalf">
			<fieldset>
				<legend>Available Placeholders</legend>
				<c:forEach items="${defaultLetterMap}" var="entry">
					<table class="placeholderTable" id="placeholders_${entry.key}" border="1" style="max-width:350px; display:none">
						<tr align="center" style="font-weight:bold">
							<td>Name</td>
							<td>Description</td>
						</tr>
						<c:forEach items="${entry.value.placeholders}" var="placeholder">
							<tr>
								<td><c:out value="${placeholder.name}" /></td>
								<td><c:out value="${placeholder.description}" /></td>
							</tr>
						</c:forEach>
					</table>
				</c:forEach>
			</fieldset>
		</div>
	</div>
	<div class="clearCenter centerContent">
		<c:if test="${not FORM_READ_ONLY}">
			<input style="display: none" class="updateFields" type="submit"
				value="Submit">
		</c:if>
		<a id="cancelFormButton" class="buttonAnchor keepEnabledForInactive"
			href="#">Cancel</a>
	</div>
</form>