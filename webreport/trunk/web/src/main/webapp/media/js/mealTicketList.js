$(function() {
    var theDataTable = $('#mealTicketSearchResultsList').DataTable({
    	buttons: ['excel', {
			extend : 'pdfHtml5',
			orientation : 'landscape'
		}, 'print'],
		"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
		"order": [],
		"paging" : false,
    	"stateSave": false
	})
    
    rebuildTableFilters('mealTicketSearchResultsList')
})

function printSelectedMealTickets() {
	var mealTicketIds = new Array()
	$("input[name='mealTicketSelect']:checked").each(function(index, item) {
		mealTicketIds.push($(item).val())
	})
	if (mealTicketIds.length == 0) {
		displayAttentionDialog('Please select at least one meal ticket.')
		return
	}
	
	var buildForm = function(docContext) {
		var form = $('<form action="' + protocolHostnamePort + homePath + '/mealTicketPrint.htm" method="POST"></form>', docContext)
		$('<input type="hidden" name="' + csrfParamName + '" value="' + csrfValue + '" />', docContext).appendTo(form)
		
		$.each(mealTicketIds, function(index, item) {
			$('<input type="hidden" />', docContext).attr('name', 'mealTicketIds').attr('value', item).appendTo(form)
		})
		return form
	}
	
	var popupWindow = window.open("", "mealTicketPrintPopupWindow",
		"toolbar=no, scrollbars=yes, resizable=yes, top=100, left=100, width=" + window.innerWidth + ", height=" + (screen.availHeight - 2 * 100))
	var form = buildForm(popupWindow.document)
	
	$("body", popupWindow.document).append(form)
	popupWindow.document.close()
	$("form", popupWindow.document).submit()
}

function setAllCheckboxes(isChecked) {
	$("input[name='mealTicketSelect']").prop('checked', isChecked)
}

function showPrintMealTicketDialog(mealTicketId) {
	alert('TODO!')
}
	
function deleteMealTicket(mealTicketId) {
	confirmDialog('Are you sure you want to delete this meal ticket?', function() {
		document.location.href = homePath + '/mealTicketDelete.htm?mealTicketId=' + mealTicketId 
	})
}

function validateMealTicketOccasionalVol() {
	var errors = new Array();

	if ($.trim($("#occasionalLastNameInput").val()) == '') {
		errors.push("Occasional Volunteer Last Name is required.")
	}
	
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>" + errors.join("</li><li>")
				+ "</li></ul>");
	}
	return errors.length == 0;
}

function addOccasionalVolunteer() {

	   if (!validateMealTicketOccasionalVol()) return;

	   $.ajax({
		url : ajaxHomePath + '/mealTicketOccasionalSubmit',
		method: 'POST',
		dataType : 'json',
		data : {
			lastName : $("#occasionalLastNameInput").val(),
			firstName: $("#occasionalFirstNameInput").val()
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			document.location.href = homePath + '/mealTicketList.htm'
		}
	})
}
	

function addVolunteerMealTicket() {
	popupVolunteerSearch('addMealTicket', '', {
		searchFirstNameStr : $('#volunteerFirstNameInput').val(),
		searchLastNameStr : $('#volunteerLastNameInput').val(),
	})
}

function addMealTicketForVolCallback(volunteerObj) {
	$.ajax({
		url : ajaxHomePath + '/mealTicketAddVolunteer',
		dataType : 'json',
		data : {
			volunteerId : volunteerObj.id
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			document.location.href = homePath + '/mealTicketList.htm'
		}
	})
}
	