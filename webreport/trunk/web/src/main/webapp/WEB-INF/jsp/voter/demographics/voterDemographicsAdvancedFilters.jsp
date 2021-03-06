<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	$(function() {

		// ------------ last volunteered options
		/*
		$("#rxLastVolOptions").change(function() {
			$("#haveLastVolDiv").toggle($(this).val() == 'have')
			$("#haventLastVolDiv").toggle($(this).val() == 'havent')
		})

		$("#rxHaveLastVolOption").change(function() {
			$("#haveLastVolAfterDiv").toggle($(this).val() == 'haveLastVolAfter')
		})

		$("#rxHaventLastVolOption").change(function() {
			$("#haventLastVolSinceDiv").toggle($(this).val() == 'haventLastVolSince')
		})
		*/
		// ------------ status date options
		
		$("#rxAffiliatedDateOptions").change(function() {
			$("#affiliatedDateBeforeDiv").toggle($(this).val() == 'before')
			$("#affiliatedDateAfterDiv").toggle($(this).val() == 'after')
			$("#affiliatedDateBetweenDiv").toggle($(this).val() == 'between')
		})

		// ------------ active options

		// $("#rxActiveOptions").change(function() {
		//	$("#upcomingInactivationDiv").toggle($(this).val() == 'upcomingInactivation')
		// })

		// ------------ inactive options

		// 		$("#rxIncludeInactive").change(function() {
		// 			$("#inactiveDetails").toggle($(this).is(":checked"))
		// 		})

		// 		$("#rxInactiveOptions").change(function() {
		// 			$("#previouslyInactivatedDiv").toggle($(this).val() == 'past')
		// 			$("#willBeInactivatedDiv").toggle($(this).val() == 'future')
		// 		})

		// 		$("#rxPreviouslyInactivatedOptions").change(function() {
		// 			$("#inactivatedSinceDiv").toggle($(this).val() == 'since')
		// 		})

		// ------------ terminated options

		// 		$("#rxIncludeTerminated").change(function() {
		// 			$("#termDetails").toggle($(this).is(":checked"))
		// 		})

		// 		$("#rxTermOptions").change(function() {
		// 			$("#termAfterDiv").toggle($(this).val() == 'after')
		// 		})
	})
	
	function validateAdvancedFilters(allErrors) {
		/*
		if ($("#rxHaveLastVolAfter").is(":visible") && $("#rxHaveLastVolAfter").val() == '') {
			allErrors.push('Please enter a "volunteered after" date.')
		}
		
		if ($("#rxHaventLastVolSince").is(":visible") && $("#rxHaventLastVolSince").val() == '') {
			allErrors.push('Please enter a "volunteered since" date.')
		}
		*/
		if ($("#rxAffiliatedDateBefore").is(":visible") && $("#rxAffiliatedDateBefore").val() == '') {
			allErrors.push('Please enter an "Affiliated Date Before" date.')
		}
		
		if ($("#rxAffiliatedDateAfter").is(":visible") && $("#rxAffiliatedDateAfter").val() == '') {
			allErrors.push('Please enter an "Affiliated Date After" date.')
		}
		
		if ($("#rxAffiliatedDateBetweenStart").is(":visible") && $("#rxAffiliatedDateBetweenStart").val() == '') {
			allErrors.push('Please enter an "Affiliated Date Between" start date.')
		}
		
		if ($("#rxAffiliatedDateBetweenEnd").is(":visible") && $("#rxAffiliatedDateBetweenEnd").val() == '') {
			allErrors.push('Please enter an "Affiliated Date Between" end date.')
		}
	}
</script>

<table id="detailsList">
	<%--
	<tr id="activeDetails">
		<td align="right"><i>Active details:</i></td>
		<td width="15"></td>
		<td>Show <select id="rxActiveOptions"><option
					value="all" class="primaryAdvancedSelect">(all)</option>
				<option value="upcomingInactivation">those who will be
					inactivated...</option>
		</select>

			<div class="restrictDiv" id="upcomingInactivationDiv"
				style="display: none">
				<select id="rxUpcomingInactivationOptions"><option
						value="within30">within 30 days</option>
					<option value="within60">within 60 days</option>
					<option value="within90">within 90 days</option>
				</select>
			</div>

		</td>
	</tr>
	
	<tr id="lastVolDetails">
		<td align="right"><i>Affiliated Date:</i></td>
		<td width="15"></td>
		<td>Show <select id="rxAffDateOptions"
			class="allParamInputs primaryAdvancedSelect"><option
					value="all">(all)</option>
				<option value="have">those who have volunteered...</option>
				<option value="havent">those who haven't volunteered...</option>
		</select>

			<div class="restrictDiv" id="haveLastVolDiv" style="display: none">
				<select id="rxHaveLastVolOption" class="allParamInputs">
					<option value="haveLastVolLast30">in the last 30 days</option>
					<option value="haveLastVolLast60">in the last 60 days</option>
					<option value="haveLastVolLast90">in the last 90 days</option>
					<option value="haveLastVolThisFiscalYear">this fiscal year</option>
					<option value="haveLastVolAfter">after...</option>
				</select>

				<div class="restrictDiv" id="haveLastVolAfterDiv"
					style="display: none">
					<input type="text" class="allParamInputs dateInput"
						id="rxHaveLastVolAfter" size="12" />
				</div>
			</div>

			<div class="restrictDiv" id="haventLastVolDiv" style="display: none">
				<select id="rxHaventLastVolOption" class="allParamInputs">
					<option value="haventLastVolIn30">in the last 30 days</option>
					<option value="haventLastVolIn60">in the last 60 days</option>
					<option value="haventLastVolIn90">in the last 90 days</option>
					<option value="haventLastVolInYear">in this fiscal
						year</option>
					<option value="haventLastVolSince">since...</option>
					<option value="haventLastVolEver">ever</option>
				</select>

				<div class="restrictDiv" id="haventLastVolSinceDiv"
					style="display: none">
					<input type="text" class="allParamInputs dateInput"
						id="rxHaventLastVolSince" size="12" />
				</div>
			</div></td>
	</tr>
--%>
	<tr id="affiliatedDateDetails">
		<td align="right"><i>Affiliated Date:</i></td>
		<td width="15"></td>
		<td>Show <select id="rxAffiliatedDateOptions"
			class="allParamInputs primaryAdvancedSelect"><option
					value="all">(all)</option>
				<option value="within30Days">within the last 30 days</option>
				<option value="within60Days">within the last 60 days</option>
				<option value="within90Days">within the last 90 days</option>
				<option value="withinYear">this calendar year</option>
				<option value="before">before...</option>
				<option value="after">after...</option>
				<option value="between">between...</option>
		</select>

			<div class="restrictDiv" id="affiliatedDateBeforeDiv"
				style="display: none">
				<input type="text" class="allParamInputs dateInput"
					id="rxAffiliatedDateBefore" size="12" />
			</div>

			<div class="restrictDiv" id="affiliatedDateAfterDiv"
				style="display: none">
				<input type="text" class="allParamInputs dateInput"
					id="rxAffiliatedDateAfter" size="12" />
			</div>

			<div class="restrictDiv" id="affiliatedDateBetweenDiv"
				style="display: none">
				<input type="text" class="allParamInputs dateInput"
					id="rxAffiliatedDateBetweenStart" size="12" /> and <input type="text"
					class="allParamInputs dateInput" id="rxAffiliatedDateBetweenEnd"
					size="12" />
			</div>
		</td>
	</tr>
	<%--
	<tr id="inactiveDetails" style="display: none">
		<td align="right"><i>Inactive details:</i></td>
		<td width="15"></td>
		<td>Show <select id="rxInactiveOptions"
			class="allParamInputs primaryAdvancedSelect"><option
					value="all">(all)</option>
				<option value="past">only those inactivated...</option>
		</select>

			<div class="restrictDiv" id="previouslyInactivatedDiv"
				style="display: none">
				<select id="rxPreviouslyInactivatedOptions" class="allParamInputs"><option
						value="within30">within 30 days</option>
					<option value="within60">within 60 days</option>
					<option value="within90">within 90 days</option>
					<option value="since">since...</option>
				</select>

				<div class="restrictDiv" id="inactivatedSinceDiv"
					style="display: none">
					<input type="text" class="allParamInputs dateInput"
						id="rxInactivatedSince" size="12" />
				</div>
			</div>

			<div class="restrictDiv" id="willBeInactivatedDiv"
				style="display: none">
				<select id="rxWillBeInactivatedOptions" class="allParamInputs"><option
						value="within30">within 30 days</option>
					<option value="within60">within 60 days</option>
					<option value="within90">within 90 days</option>
				</select>
			</div>
		</td>
	</tr>

	<tr id="termDetails" style="display: none">
		<td align="right"><i>Terminated details:</i></td>
		<td width="15"></td>
		<td>Show <select id="rxTermOptions"
			class="allParamInputs primaryAdvancedSelect">
				<option value="all">(all)</option>
				<option value="last30">terminated in the last 30 days</option>
				<option value="last60">terminated in the last 60 days</option>
				<option value="last90">terminated in the last 90 days</option>
				<option value="after">terminated after...</option>
		</select>

			<div class="restrictDiv" id="termAfterDiv" style="display: none">
				<input type="text" class="allParamInputs dateInput" id="rxTermAfter"
					size="12" />
			</div>
		</td>
	</tr>
	 --%>
</table>