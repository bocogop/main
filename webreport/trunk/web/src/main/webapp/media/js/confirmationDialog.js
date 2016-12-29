var confirmationDialogInitialized = false
var confirmationDialogCallback = null

/**
 *  Displays Confirmation Dialog 
 */
function confirmDialog(messageToDisplay, acceptanceCallback, options) {
	initializeConfirmationDialog()
	
	var finalOpts = $.extend({}, {
		cancelCallback : null,
		title : null,
		width : 550,
		height : 200
	}, options)
	
	confirmationDialogCallback = acceptanceCallback
	confirmationDialogCancelCallback = finalOpts.cancelCallback
	
	$('#confirmationDialog').dialog('option', 'width', finalOpts.width)
	$('#confirmationDialog').dialog('option', 'height', finalOpts.height)
	$("#confirmationDialogMessage").empty().html(messageToDisplay)
	
	$('#confirmationDialog').dialog('option', 'title', finalOpts.title ? escapeHTML(finalOpts.title) : 'Confirmation')
	$("#confirmationDialog").dialog("open")
}

function initializeConfirmationDialog() {
	if (confirmationDialogInitialized)
		return
	
	$("#confirmationDialog").dialog({
		autoOpen: false,
		modal: true,
		show: 'slide',
		draggable: true,
		resizable:true,
		width: 550,
		buttons: {
			OK : function() {
				$(this).dialog('close')
				confirmationDialogCallback()
			},
			Cancel: function() {
				$(this).dialog('close')
				if (confirmationDialogCancelCallback)
					confirmationDialogCancelCallback()
			}
		}
	})
	
	confirmationDialogInitialized = true
}
