var donationSearchResults = new Object()

function popupDonationSearch(uid, options) {
	var dialogEl = $("#donationSearchDialog" + uid)
	
	var defaultDate = new Date(new Date().setDate(new Date().getDate() - 30))
	
	options = $.extend({
		beginDate : getDateAsMMDDYYYY(defaultDate),
		endDate : '',
		donorName : '',
		donorType : '',
		donationId : ''
	}, options)

	$("#donationSearchBeginDate" + uid).val(options.beginDate)
	$("#donationSearchEndDate" + uid).val(options.endDate)
	$("#donationSearchDonorName" + uid).val(options.donorName)
	$("#donationSearchDonorType" + uid).val(options.donorType)  
	$("#donationId" + uid).val(options.donationId)
	
	$("#donationSearchNoResults" + uid).hide()
	$("#donationSearchResultsTable" + uid).hide()
	$('#donationSearchBeginDate' + uid).focus()
	$("#donationSearchDialog" + uid).dialog('open')
}

function donationSearchPopupItemSelected(uid, donationId) {
	var fullObj = donationSearchResults[uid]['' + donationId]
	var theDialog = $("#donationSearchDialog" + uid)
	theDialog.dialog('close')
	theDialog.data('callbackMethod')(fullObj)
}

function initDonationSearchPopup(options) {
	var uid = options.uid
	var maxResults = options.maxResults
	var callbackMethod = options.callbackMethod
	
	var parms = {
		"columnDefs" : [
			{
				"targets" : 0,
				"data" : function(row, type, val, meta) {
					if (type === 'display')
						return '<a class="appLink" href="javascript:donationSearchPopupItemSelected(\''
							+ uid + '\', ' + row.id 
							+ ')">' + row.donationDate + '</a>'
					return getAsYYYYMMDD(row.donationDate)
				}
			},{
				"targets" : 1,
				"data" : function(row, type, val, meta) {
					return row.donationType? row.donationType.donationType : ''
				}
			},{
				"targets" : 2,
				"data" : function(row, type, val, meta) {
					return row.donor && row.donor.donorType ? row.donor.donorType.donorType : ''
				}
			},{
				"targets" : 3,
				"data" : function(row, type, val, meta) {
					return row.donor? row.donor.individualName : ''
				}
			},{
				"targets" : 4,
				"data" : function(row, type, val, meta) {
					return row.organization ? row.organization.displayName : ''
				}
			},{
				"targets" : 5,
				"data" : function(row, type, val, meta) {
					return row.donor ? row.donor.otherGroupName : ''
				}
			},{
				"targets" : 6,
				"data" : function(row, type, val, meta) {
					return row.donationDescription
				}
			},{
				"targets" : 7,
				"data" : function(row, type, val, meta) {
					if (type == 'display')
						return '$' + formatAndAddThousandSeparators(row.totalDonationAmount)
					return row.totalDonationAmount
				}
			},{
				"targets" : 8,
				"data" : function(row, type, val, meta) {
					return row.id
				}
			}
			],
		"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false
	}

	var theDataTable = $('#donationSearchResultsList' + uid).DataTable(parms)

	var dialogEl = $("#donationSearchDialog" + uid)
	
	var buttonConfig = {}
	buttonConfig['Cancel'] = function() { $(this).dialog('close') }
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 1200,
		height : 750,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : buttonConfig
	})
	dialogEl.data('maxResults', maxResults)
	dialogEl.data('callbackMethod', callbackMethod)
	
	$.each([".donationSearchInput" + uid], function(index,
			value) {
		$(value).keypress(function(event) {
			if (event.which == 13) {
				submitDonationSearchForm(uid)
			}
		})
	})
	
	$("#donationSearchDonationId" + uid).change(function() {
		$(".donationSearchNonId" + uid).prop('disabled', $(this).val() != '')
	})
	
	$(".donationSearchLink" + uid).click(function(evt) {
		submitDonationSearchForm(uid)
	})
	
	dialogEl.show()
}

function submitDonationSearchForm(uid) { 
	if (allValsEmpty(["donationSearchBeginDate" + uid,
	                  "donationSearchEndDate" + uid,
	                  "donationSearchDonorName" + uid,
	                  "donationSearchDonorType" + uid,
	                  "donationSearchDonationId" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	var dialogEl = $("#donationSearchDialog" + uid)
				
	$.ajax({
		url : ajaxHomePath + "/donation/search",
		type : "POST",
		dataType : 'json',
		data : {
			beginDate : $("#donationSearchBeginDate" + uid).val(),
			endDate : $("#donationSearchEndDate" + uid).val(),
			donorName : $("#donationSearchDonorName" + uid).val(),
			donorTypeId : $("#donationSearchDonorType" + uid).val(),
			donationId : $("#donationSearchDonationId" + uid).val(),
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processDonationSearchResults(uid, results)
		}
	})
}

function processDonationSearchResults(uid, results) {
	var dialogEl = $("#donationSearchDialog" + uid)
	
	$("#donationSearchNoResults" + uid).hide()
	$("#donationSearchResultsTable" + uid).hide()

	var resultMap = new Object()

	var table = $('#donationSearchResultsList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].id] = results[i]
		table.row.add(results[i])
	}

	donationSearchResults[uid] = resultMap

	$("#donationSearchMaxResults" + uid).toggle(results.length >= dialogEl.data('maxResults'))
	if (results.length > 0) {
		table.search('').columns().search('')
		rebuildTableFilters('donationSearchResultsList' + uid)
		$("#donationSearchResultsTable" + uid).show()
	} else {
		$("#donationSearchNoResults" + uid).show()
	}
	table.draw()

	$('#donationSearchLastName' + uid).focus()
}