<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	var selectedVols = new SortedArray([])
	var selectedVolEmails = {} // map of vol ID to email
	
	$(function() {
		$('.dateInput').each(function() {
			$(this).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(this).mask(twoDigitDateMask)
		})
		
		$('#paramsShowHide').showHide({
			speed : 500,
			easing : '',
			showText : 'Customize',
			hideText : 'Close Customizations'
		})
		
		$('#customRestrictionsShowHide').showHide({
			speed : 100,
			easing : '',
			changeText : 1,
			showText : 'Show Custom Restrictions',
			hideText : 'Hide Custom Restrictions'
		})

		buildTable()
	})
	
	var colDefs = []
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var isChecked = selectedVols.search(row.id) != -1
			return '<input name="voterIds" class="volCheckbox" type="checkbox" value="'
				+ row.id + '" email="' + escapeHTML(defaultStr(row.email)) + '" />'
		}
	})
	
	function buildTable() {
		$("#voterList").DataTable({
			"ajax" : {
				"url" : ajaxHomePath + "/voter/demographics",
				"type" : "POST",
				"data" : function(d) {
					var detailsRestrictions = {}
					$("select, input[type='text']", "#paramsTable").each(function(index, item) {
						var id = $(item).attr('id')
						if (id && id.lastIndexOf("rx", 0) === 0)
							detailsRestrictions[id] = $(item).val()
					})
					$("input[type='checkbox']", "#paramsTable").each(function(index, item) {
						var id = $(item).attr('id')
						if (id && id.lastIndexOf("rx", 0) === 0)
							detailsRestrictions[id] = $(item).is(":checked")
					})
					
					var displayColumns = []
					$('#paramsTable input.columnCheckbox:checked').each(function(index, item) {
						displayColumns.push($('#paramsTable input.columnCheckbox').index(item))
					})
					
					return $.extend({}, d, detailsRestrictions, {
						"displayColumnIndexes" : displayColumns
					});
				},
				"complete" : function() {
					$("#paramsWrapper").show()
					$(window).resize()
				},
				"error" : commonAjaxErrorHandler
			},
			"buttons" : [ {
				extend : 'excel',
				exportOptions : {
					columns : ':visible:gt(0)',
					stripNewlines : false,
					orthogonal : 'export'
				}
			}, {
				extend : 'pdfHtml5',
				orientation : 'landscape',
				exportOptions : {
					columns : ':visible:gt(0)',
					stripNewlines : false,
					orthogonal : 'export'
				}
			}, {
				extend : 'print',
				exportOptions : {
					columns : ':visible:gt(0)',
					stripHtml : false
				},
				autoPrint : false
			}, {
				text: 'Email Voters',
	            action: emailSelected
			}],
			"columnDefs" : colDefs,
			"dom" : '<"top"l><"clearCenter"><"top2"pBi>rt<"clear">',
			"drawCallback": function() {
				syncColumns()
				syncCheckboxes()
			},
			"lengthMenu" : [ [ 5, 50, 1000, 5000 ], [ 5, 50, 1000, 5000 ] ],
			"order" : [ [ 1, 'asc' ] ],
			"pagingType" : "full_numbers",
			"pageLength" : 5,
			"serverSide" : true,
			"stateSave" : false
		})
		
		$('#voterList').on('change', '.volCheckbox', volCheckChanged)
	
		$('#voterList').on('change', '.columnFilter', function() {
			var colIndex = parseInt($(this).attr('colIndex'))
			var myVal = getColumnFilterVal($(this))
			$("#voterList").DataTable().columns(colIndex + 1).search(myVal, true, false).draw()
		})
		
		var setToCustom = function() {
			$("#presetSelect").val('custom')
		}
		$('#paramsSlidingDiv').on('change', '.allParamInputs', setToCustom)
		$('#voterList').on('change', '.allParamInputs', setToCustom)
		
		$("#presetSelect").change(function() {
			if ($(this).val() == 'custom') {
				if ($("#paramsSlidingDiv").is(":visible") == false)
					$('#paramsShowHide').trigger('click')
				resetTable(false)
			}
		})
	}
	
	function resetTable(draw) {
		var theTable = $("#voterList").DataTable()
		
		// reset column visibilities & checkboxes
		$(".columnCheckbox", "#paramsTable").prop('checked', false)
		// reset all advanced filters
		$(".primaryAdvancedSelect").val("all").trigger('change')
		
		// set specific column and other filters vals
		$(".columnFilter").val('')
		$("#voterList_filter").val('')
		theTable.search('').columns().search('')
		theTable.page.len(5)
		theTable.order([1, 'asc'])
		if (draw)
			theTable.draw()
	}
	
	function getColumnFilterVal($colFilter) {
		var colIndex = parseInt($colFilter.attr('colIndex'))
		var colValFunc = $colFilter.attr('colValueFn')
		var myVal = colValFunc ? eval(colValFunc + "()") : $colFilter.val()
		return myVal
	}
	
	function syncColumns() {
		var theTable = $("#voterList").DataTable()
		$('#paramsTable input.columnCheckbox').each(function(colIndex) {
			// +1 to accommodate the checkbox column
			theTable.columns(colIndex + 1).visible($(this).prop('checked'))
		})
	}
	
	function volCheckChanged() {
		var val = $(this).val()
		
		if ($(this).is(":checked")) {
			if (selectedVols.search(val) == -1) {
				selectedVols.insert(val)
				selectedVolEmails[val] = $(this).attr('email')
			}
		} else {
			selectedVols.remove(val)
			delete selectedVolEmails[val]
		}
	}

	function syncCheckboxes() {
        $("input.volCheckbox").each(function(index, item) {
       		$(item).prop('checked', selectedVols.search($(item).val()) != -1)
        })
	}
	
	function setAllCheckboxes(isChecked) {
		if (isChecked) {
			$("input.volCheckbox").each(function(index, item) {
				var val = $(item).val()
				if (selectedVols.search(val) == -1) {
					selectedVols.insert(val)
					selectedVolEmails[val] = $(item).attr('email')
				}
			})
			
			var pageInfo = $("#voterList").DataTable().page.info()
			if (pageInfo.pages > 1) {
				displayAttentionDialog('Only voters displayed are automatically checked. To select every voter in the table, please first increase the page length.')
			}
		} else {
			selectedVols = new SortedArray([])
			selectedVolEmails = {}
		}
		
		syncCheckboxes()
	}

	function refreshDemographics() {
		if (validateParams()) {
			var theTable = $('#voterList').DataTable()
			setAllCheckboxes(false)
			$('#paramsTable input.columnCheckbox').each(function(index, item) {
				if (!$(item).is(":checked")) {
					$(".columnFilter[colindex=" + index + "]").val('')
					theTable.columns(index + 1).search('')
				}
			})
			theTable.ajax.reload()
		}
	}
	
	function validateParams() {
		var allErrors = new Array()
		
		if ($('#paramsTable input.columnCheckbox:checked').length == 0) {
			allErrors.push('Please select at least one column to display.')
		}
		
		validateAdvancedFilters(allErrors)

		if (allErrors.length > 0)
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ allErrors.join("</li><li>") + "</li></ul>");

		return allErrors.length == 0
	}

	function getAddressDashedBoxEl(ee) {
		var theHtml = '<table width="100%" class="addressBox">'
		theHtml += '<tr valign="top"><td nowrap width="99%">'

		var addressHtml = ""
		if (ee.addressMultilineDisplay)
			addressHtml = escapeHTML(ee.addressMultilineDisplay)

		theHtml += convertLinefeedToBR(addressHtml) + '</td></tr></table>'
		return getBoxEl(theHtml, false)
	}
	
	function printSelected() {
		if (selectedVols.array.length == 0) {
			displayAttentionDialog('Please select at least one voter to print.')
			return
		}
		
		if (selectedVols.array.length > 990) {
			displayAttentionDialog('Please limit number of labels to print to 990 (33 pages worth of labels) at a time.')
			return
		}
		
		printIfNeeded(selectedVols.array)
	}
	
	function printIfNeeded(voterIds) {
		var reportsToPrint = []
		
		var commonParams = {
			Username : "<c:out value="${username}" />",
			UserPasswordHash : "<c:out value="${userPasswordHash}" />",
			PrecinctContextId : "<c:out value="${siteContextId}" />"
		}
		
		reportsToPrint.push({
			reportName : 'Voter_AddressLabels_By_Vol_Id',
			reportOutputFormat : 'PDF',
			reportParams : $.extend({}, commonParams, {
				VolId : voterIds
			})
		})
		
		if (reportsToPrint.length > 0)
			printReports(reportsToPrint)
	}
	
	function emailSelected() {
		if (selectedVols.array.length == 0) {
			displayAttentionDialog('Please select at least one voter to email.')
			return
		}
	
		var combined = []
		var totalLen = 0
		for (var i = 0; i < selectedVols.array.length; i++) {
			var email = selectedVolEmails[selectedVols.array[i]]
			if (email && email != '') {
				totalLen += email.length + (i > 0 ? 3 : 0) // +1 to accommodate the space-semicolon-space
				combined.push(email)
			}
		}
		
		if (totalLen > ${maxGetRequestLength}) {
			confirmDialog('Too many voters were selected to use the normal mail functionality.\n\n'
					+ 'Would you like to receive an email containing the complete recipient list?', function() {
						$.ajax({
							url : ajaxHomePath + "/voter/emailRecipientList",
							type : "POST",
							dataType : 'json',
							data : {
								emails : combined
							},
							error : commonAjaxErrorHandler,
							success : function(results) {
								displayAttentionDialog('The email was sent successfully.')
							}
						})
			})
			return
		}
		
		if (combined.length > 0)
			location.href = 'mailto:' + combined.join(' ; ')
	}
	
	function getEntryDateFilterVal() {
		return $("#filterEntryDateMonth").val() + '/' + $("#filterEntryDateYear").val()
	}
	function getStatusDateFilterVal() {
		return $("#filterStatusDateMonth").val() + '/' + $("#filterStatusDateYear").val()
	}
	function getLastVoteredDateFilterVal() {
		return $("#filterLastVoteredDateMonth").val() + '/' + $("#filterLastVoteredDateYear").val()
	}
	function getDateLastAwardFilterVal() {
		return $("#filterDateLastAwardMonth").val() + '/' + $("#filterDateLastAwardYear").val()
	}
</script>

<c:forEach items="${allVoterDemographicsColumns}" var="col">
	<jsp:include page="columnDefs/${fn:toLowerCase(col)}.jsp"></jsp:include>
</c:forEach>

<style>
ul#detailsList li {
	margin: 5px;
}

div.restrictDiv {
	display: inline-block;
}
</style>

<div class="clearCenter" style="display: none" id="paramsWrapper">
	<div class="clearCenter" style="margin-bottom: 10px;">
		<nobr>
		<a class="appLink" id="paramsShowHide" href="#"
			rel="#paramsSlidingDiv">Customize</a>
		
		<%@ include file="voterDemographicsPresets.jsp" %>
		</nobr>
	</div>
	<div id="paramsSlidingDiv" class="toggleDiv" style="display: none;">
		<fieldset>
			<legend id="paramsLegend">Parameters</legend>
			<table id="paramsTable" cellpadding="7" style="min-width: 700px">

				<tr>
					<td colspan="6" align="center"><i><u>Add/Remove
								Columns:</u></i></td>
				</tr>
				<tr valign="top">
					<c:forEach items="${columnsByDivider}" var="entry">
						<td nowrap>
							<c:forEach items="${entry.value}" var="col">
								<c:set var="checked" value="" />
								<c:if test="${col.initiallyChecked}">
									<c:set var="checked" value='checked="checked"' />
								</c:if>
								<input type="checkbox" class="allParamInputs columnCheckbox"
									${checked}> <c:out value="${col.fullName}" /><br>
							</c:forEach>
						</td>
					</c:forEach>
				</tr>
				<tr valign="top">
					<td nowrap colspan="5"
						style="border-top: 1px dashed gray; padding-top: 15px">

						<a class="appLink" id="customRestrictionsShowHide" href="#"
						rel="#customRestrictionsSlidingDiv"></a>
						<div id="customRestrictionsSlidingDiv"
							style="display: none; margin-left: 20px">
							<%@ include file="voterDemographicsAdvancedFilters.jsp" %>
						</div>
					</td>
					<td style="border-top: 1px dashed gray; padding-top: 15px"
						align="center"><a href="javascript:refreshDemographics()"
						class="buttonAnchor">Search</a></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<div style="padding: 10px">
	<table id="voterList" class="stripe cell-border"
		summary="List of Voters" width="100%">
		<thead>
			<tr>
				<td class="noborder" width="1"></td>
				<c:forEach items="${allVoterDemographicsColumns}" var="col">
					<c:if test="${not col.filtered}">
						<td class="noborder"></td>
					</c:if>
					<c:if test="${col.filtered}">
						<jsp:include page="columnFilters/${fn:toLowerCase(col)}.jsp"></jsp:include>
					</c:if>
				</c:forEach>
			</tr>
			<tr>
				<th align="center" nowrap class="tableHeaderLinkWrapper" width="1"><a
					class="tableHeaderLink" href="javascript:setAllCheckboxes(true)">All</a>
					/ <br> <a class="tableHeaderLink"
					href="javascript:setAllCheckboxes(false)">None</a></th>
				<c:forEach items="${allVoterDemographicsColumns}" var="c">
					<th><c:out value="${c.shortName}" /></th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>