var attentionDialogInitialized = false

function attentionDialogOpenDetailsFunction() {
	$("#attentionDialogCopyDetailsButton").show()
	var finalHeight = $("#attentionDialog").data('finalHeight')
	
	$("#attentionDialog").dialog("option", "height", finalHeight + 350)
}

function attentionDialogCloseDetailsFunction() {
	$("#attentionDialogCopyDetailsButton").hide()
	var finalHeight = $("#attentionDialog").data('finalHeight')
	
	$("#attentionDialog").dialog("option", "height", finalHeight)
}

function initializeAttentionDialog(callback) {
	if (attentionDialogInitialized) {
		callback()
		return
	}
	
	$('#attentionDialogDetailsLink').expandPanel({
		speed : 500,
		easing : '',
		title : 'Details',
		showText : 'Show Details',
		hideText : 'Hide Details',
		open : attentionDialogOpenDetailsFunction,
		close : attentionDialogCloseDetailsFunction
	})
		
	$("#attentionDialog").dialog({
		autoOpen: false,
		modal: true,
		// show: 'slide',
		draggable: true,
		resizable:true,
		width: 550,
		height: 200,
		buttons: {
			'Copy Details' : {
				text : 'Copy Details',
				id : 'attentionDialogCopyDetailsButton',
				click : function() {
					copyContainerText('attentionDialogDetailsDiv')
					alert('The details were copied to the clipboard.')
				}
			},
			Close: function() {
				$(this).dialog('close')
			}
		},
		create: function() {
			callback()
		}
	})
	
	attentionDialogInitialized = true
}

/**
 *  Displays Attention Dialog 
 */
function displayAttentionDialog(messageToDisplay, detailsHtml, title, options) {
	initializeAttentionDialog(function() {
		$("#attentionDialogMessage").empty().html(messageToDisplay)
		
		var hasDetails = (detailsHtml != null && typeof(detailsHtml) != "undefined" && detailsHtml != '')
		$("#attentionDialogDetailsDiv").empty()
		if (hasDetails)
			$("#attentionDialogDetailsDiv").html(detailsHtml)
		$("#attentionDialogDetailsRow").toggle(hasDetails)
		
		$('#attentionDialog').dialog('option', 'title', title ? escapeHTML(title) : 'Attention')
		$("#attentionDialog").dialog("open")
	})
	
	var finalOpts = $.extend({}, {
		cancelCallback : null,
		title : null,
		width : 550,
		height : 200
	}, options)
	
	$('#attentionDialog').data('finalWidth', finalOpts.width)
	$('#attentionDialog').data('finalHeight', finalOpts.height)
	
	if ($("#attentionDialogDetailsDiv").is(":visible")) {
		attentionDialogOpenDetailsFunction()
	} else {
		attentionDialogCloseDetailsFunction()
	}
}

function displayAttentionDialogForValidationFailure(message, rule, recommendation) {
	var hasRule = (typeof(rule) != "undefined" && rule != '')
	var hasRecommendation = (typeof(recommendation) != "undefined" && recommendation != '')
	var hasEither = hasRule || hasRecommendation
	
	var html = null
	if (hasEither) {	
		html = '<table cellpadding="5">'
		if (hasRule) {
			html += '<tr valign="top">'
				+ '		<td align="right"><b>Rule:</b></td>'
				+ '		<td>' + rule + '</td>'
				+ '	</tr>'
		}
		if (hasRecommendation) {		
			html += '<tr valign="top">'
				+ '		<td align="right"><b>Recommendation:</b></td>'
				+ '		<td>' + recommendation + '</td>'
				+ '	</tr>'
		}
		html += '</table>'
	}
	displayAttentionDialog(message, html, 'Validation Error')
}