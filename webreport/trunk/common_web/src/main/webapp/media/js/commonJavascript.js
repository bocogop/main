$(function() {
	$("input:submit, a.buttonAnchor, a.submitAnchor, button").button()
	
	$("pre").each(function() {
		var $pre = $(this)
		$pre.text($.trim($pre.text()))
	})
	
	hideSpinner()
	$(document).ajaxStart(function() {
		if (!skipSpinnerOnAjaxStart) {
			showSpinner()
		}
	})
	$(document).ajaxStop(hideSpinner)
	$(document).ajaxError(hideSpinner)
		
	$(".confirm").easyconfirm()
	
	// Get all textareas that have a "maxlength" property.
    $('textarea[maxlength]').each(function() {
        // Store the jQuery object to be more efficient...
        var $textarea = $(this)
        // Store the maxlength and value of the field.
        var maxlength = $textarea.attr('maxlength')
        var val = $textarea.val()
        var numLinebreaks = val.split('\n').length - 1
        // Trim the field if it has content over the maxlength.
        $textarea.val(val.slice(0, maxlength - numLinebreaks))
        // Bind the trimming behavior to the "keyup" event.
        var trimFunc = function() {
        	var val = $textarea.val()
        	var numLinebreaks = val.split('\n').length - 1
        	if (val.length >= maxlength - numLinebreaks)
        		$textarea.val(val.slice(0, maxlength - numLinebreaks))
		}
        $textarea.bind('keyup', trimFunc)
        $textarea.bind('paste', trimFunc)
    })
    
	/* Wrap all selects whose current value is inactive - CPB */
	
	$('select.inactiveAppSelect').each(function() {
		var code = $(this).attr("inactiveAppLookupCode")
		/*
		 * If we're including the inactive value in the dropdown as a valid
		 * selection, don't wrap the select
		 */
		if ($(this).find("option[value='" + code + "']").length > 0)
			return
			
		var table = $('<table cellpadding="5" class="inactiveAppSelectWrapper"><tr>'+				
				'<td>' + $(this).attr("inactiveAppLookupName") + ' (inactive)</td>' +
			'</tr>' +
		'</table>')
		
		$(this).replaceWith(table)
	})
	
	/*
	 * Masks - autoclear = true means unless it matches the pattern below, it
	 * will set it to '' before form submission. These have to be paired with
	 * Javascript and/or Spring validation to ensure the entered value is
	 * acceptable before DB persistence. CPB
	 */
   // formats text input in fax input fields
   $(".faxmask").mask("999-999-9999", {autoclear: false});
   // formats text input in phone input fields
   $(".phoneextmask").mask("999-999-9999? x99999" , {autoclear: false});
   
   $(".currency").bind('keyup', function() {
		var typedInVal = $(this).val();
		var decimalIndex = typedInVal.indexOf(".")
		if (decimalIndex == -1)
			return
		
		$(this).val($(this).val().slice(0, decimalIndex + 3))
   })
	
   $.fn.enableDateTimePicker = function(options) {
		this.datetimepicker(options)
		this.mask(twoDigitDateTimeMask)
		this.siblings('.ui-datepicker-trigger').attr('alt',
				'Please select a Date and time')
		this.siblings('.ui-datepicker-trigger').attr('title',
				'Click here to select a Date and time')
		this
				.attr('title',
						'Please specify a Date and Time in the MM/DD/YYYY HH:mm format')
	}
	$.fn.enableDatePicker = function(options) {
		this.datepicker(options)
		this.mask(twoDigitDateMask)
		this.siblings('.ui-datepicker-trigger').attr('alt',
				'Please select a Date')
		this.siblings('.ui-datepicker-trigger').attr('title',
				'Click here to select a Date')
		this
				.attr('title',
						'Please specify a Date in the MM/DD/YYYY format')
	}
	
	$(".dateTimeInput").enableDateTimePicker({
		showOn : "button",
		buttonImage : imgHomePath + "/calendar.gif",
		buttonImageOnly : true
	})
	$(".dateInput").enableDatePicker({
		showOn : "button",
		buttonImage : imgHomePath + "/calendar.gif",
		buttonImageOnly : true
	})
})

function setFocus() {
	window.focus()
}

function scrollToTop() {
   window.scrollTo(0, 0);
}

jQuery.fn.reverse = [].reverse
	
/*
 * Setting cache to false so that IE doesnt fetch the content from the cache for
 * ajax requests (which it does by default on ajax get requests if response type
 * is jsonp or script)
 */
var csrfData = {}
csrfData[csrfParamName] = csrfValue

$.ajaxSetup({
  	cache:false,
  	data: csrfData
})

var militaryZonedDateTimeRegex = /^([0]\d|[1][0-2])\/([0-2]\d|[3][0-1])\/([2][01]|[1][6-9])\d{2}(\s([0]\d|[1]\d|[2][0-3])(\:[0-5]\d){1,2})*\s*$/;

skipSpinnerOnAjaxStart = false
defeatHideAjaxSpinner = false

function hideSpinner() {
	if (!defeatHideAjaxSpinner) {
		$("#spinner").hide()
		$("#spinnerMessage").hide()
		$("#spinner").css('z-index','1')
	}
}

function showSpinner(addlText, showSpinnerUntilPageRefresh) {
	if (showSpinnerUntilPageRefresh) {
		defeatHideAjaxSpinner = true
	}
	
	var topDomZIndex = $.topZIndex("div")
	$("#spinner").show()
	$("#spinner").css('z-index',topDomZIndex+1)
	$("#spinnerMessage").toggle($.trim(addlText) != '')
	$("#spinnerMessage p").text($.trim(addlText) != '' ? addlText : '')
	$("#spinnerMessage").css('z-index', topDomZIndex+2)
}

function showAll(idArray) {
	$.each(idArray,
		function(index, item) {
			$(item).show()
		})
}

function hideAll(idArray) {
	$.each(idArray,
		function(index, item) {
			$(item).hide()
		})
}

function escapeHTML(theVal) {
	if (theVal == null) return null
	theVal = $('<div />').text(theVal).html()
	theVal = theVal.replace(/\"/g,"&quot;")
	theVal = theVal.replace(/\'/g,"&#39;")
	return theVal
}

function convertLinefeedToBR(str) {
	return str.replace(/[\r?\n]/g, '<br />')
}

(function($) {
	$.fn.outerHTML = function() {
	  return $('<div />').append(this.eq(0).clone()).html()
	}
})(jQuery);

/*
 * (function($) { $.widget( "ui.dialog", $.ui.dialog, { _setOption: function(
 * key, value ) { // For title ensure that escapeHTML has been called before //
 * setting it on the dialog if (key=='title') { value = escapeHTML(value); } //
 * call _super to call extended widget behavior this._super( key, value ); } })
 * })(jQuery)
 */

$('.inner').wrap(function() {
  return '<div class="' + $(this).text() + '" />';
})

function doubleClickSafeguard(jqueryObjToDisable, timeout, timeoutExpiredCallback) {
	jqueryObjToDisable.button("option", "disabled", true)
	var finalTimeout = timeout ? timeout : 3000
	setTimeout(function() { jqueryObjToDisable.button("option", "disabled", false) }, finalTimeout)
	if (timeoutExpiredCallback) {
		setTimeout(timeoutExpiredCallback, finalTimeout)
	}
}

function doubleSubmitFormSafeguard(form) {
	// if(form.data.submitted) {
	// alert("Form is already submitted!");
	// return false;
	// } else {
	// form.data.submitted=true;
		return true;
	// }
}

function applyFocusToFirstActiveField() {
	// Focus on the first enabled, visible, input or textarea on the form
	var enabledVisibleInputsSet = $("#AppContainerDiv")
			.find(
					':input:enabled:visible, textarea:enabled:visible, select:enabled:visible')
					
	var anyDialogVisible = $(".ui-dialog").is(":visible");
	if (enabledVisibleInputsSet.size() > 0) {
		if (!anyDialogVisible) {
				enabledVisibleInputsSet.first().focus()
		}
	} else {
		$('#homeLink').focus()
	}
}

function validateEmail(email) {
	if ($.trim(email) == '') return true
	
	if (!validateHibernateEmail(email)) return false
	
	/* By convention this is our max length for emails - CPB */
	if (email.length > 250) return false
	
	/* WR logic to ensure they have "something@something.something"; the main RFC spec accepts just "something@something" - CPB */
	if (email.split("@")[1].indexOf('.') == -1) return false
	
	return true
}

function validateHibernateEmail(email) {
	if ($.trim(email) == '') return true
	
	var parts = email.split("@", 3)
	if (parts.length != 2) return false
	
	if (parts[0].substr(parts[0].length - 1) == '.' ||
			parts[1].substr(parts[1].length - 1) == '.')
		return false
	
	if (parts[0].length > 63 || parts[1].length > 255) return false
		
	var localRegex = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*/i
	var domainRegex = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\]/i
	
	if (localRegex.test(parts[0]) == false) return false
	if (domainRegex.test(parts[1]) == false) return false
	
	return true
}

function validatePhone(phone) {
	var phoneCheck = /^\(?(\d{3})\)?[-]?(\d{3})[-]?(\d{4})[ ]?(x)?(\d{1,5})?$/;
	return phoneCheck.test(phone);
}

function validateFax(fax) {
	var faxCheck = /^\(?(\d{3})\)?[-]?(\d{3})[-]?(\d{4})[ ]?$/;
	return faxCheck.test(fax);
}

function validateDate(date) {
	// validate date format mm/dd/yyyy
	var dateCheck = /^([0][1-9]|[1][0-2])\/([0][1-9]|[1-2]\d|[3][0-1])\/([2][01]|[1][6-9])\d{2}(\s([0]\d|[1]\d|[2][0-3])(\:[0-5]\d){1,2})*\s*$/
	return dateCheck.test(date);
}

function validateNumeric(numeric, allowNegative) {
	return validateNumericWithoutCommas(removeCommas(numeric), allowNegative)
}

function validateNumericWithoutCommas(numeric, allowNegative) {
	var numericCheck = allowNegative ? /^\-?\d+(\.\d+)?$/ : /^\d+(\.\d+)?$/
	return numericCheck.test(numeric)
}

function validateInteger(integer) {
	var integerCheck = /^\d+$/;
	return integerCheck.test(integer);
}


function formatAndAddThousandSeparators(nStr) {
	  nStr.toFixed(2); 
	  nStr += ''; 
	  var x = nStr.split('.'); 
	  var x1 = x[0]; 
	  var x2 = x.length > 1 ? '.' + x[1] : '.00'; 
	  // The following 4 lines are added due to toFixed(2) doesn't work
		// somehow
	  if(x2.length == 2 )
		  x2 = x2 + '0'
	  if(x2.length > 3)  
		  x2 = x2.slice(0, 3);
	  var rgx = /(\d+)(\d{3})/; 
	  while (rgx.test(x1)) {
		  x1 = x1.replace(rgx, '$1' + ',' + '$2');
	  } 
	  return x1 + x2;
}

function removeCommas(str) {
    while (str.search(",") >= 0) {
        str = (str + "").replace(',', '');
    }
    return str;
};

function getBoxEl(theHtml, addBorder) {
	var newEl = $("<div></div>")
		.css({'border' : addBorder ? '1px dashed black' : 'none',
			'text-align' : 'left',
			'padding' : '5px',
			'margin' : '2px',
			'display' : 'inline-block',
			// 'width' : '100%',
			'box-sizing' : 'border-box'
			}).html(theHtml)
	return newEl
}

function commonAjaxErrorHandler(jqXHR, textStatus, errorThrown) {
	var errorText = errorThrown
	var details = null
	if (jqXHR.status == 404 || jqXHR.status == 0) {
		errorText = 'The server is offline or could not be contacted.'
	} else if (jqXHR.status == 403) {
		errorText = 'Your request was denied, possibly due to a session timeout or server restart. Please logout and login again.'
	} else if (jqXHR.responseJSON) {
		var ar = jqXHR.responseJSON.ajaxResult
		if (ar) {
			errorText = ar.statusMessage
			details = ar.statusDetails
		}
	} else if (jqXHR.responseText) {
		details = jqXHR.responseText
	}
	
	if (errorText == null) errorText = 'Unknown error'
	
	displayAttentionDialog(errorText, details, 'Error')
}

function abbreviate(str, maxLen) {
	if (!str) return str
	
	var oneThird = maxLen / 3
	var twoThirds = maxLen * 2 / 3
	if (str.length > maxLen)
	    return str.substr(0, twoThirds) + '...' + str.substr(str.length-oneThird, str.length);
	return str;
}

function pad(n, width, z) {
	var y = '' + n
	while (y.length < width)
		y = '' + (z || '0') + y;
	return y;
}
function rpad(n, width, z) {
	var y = '' + n
	while (y.length < width)
		y = '' + y + (z || '0')
	return y;
}

var monthIndexes = {
		'january' : 0,
		'february' : 1,
		'march' : 2,
		'april' : 3,
		'may' : 4,
		'june' : 5,
		'july' : 6,
		'august' : 7,
		'september' : 8,
		'october' : 9,
		'november' : 10,
		'december' : 11
}
function monthComparator(a, b) {
	if (a === b) return 0
	var aIndex = monthIndexes[a.toLowerCase()]
	var bIndex = monthIndexes[b.toLowerCase()]
	if (aIndex == bIndex) return 0
	if (!aIndex && bIndex) return -1
	if (!bIndex && aIndex) return 1
	return aIndex < bIndex ? -1 : 1
}

function reverseCompareDates(a, b) {
	return new Date(b)-new Date(a)
}

function getAsYYYYMMDD(MMDDYYYYString) {
	if (!MMDDYYYYString) return null
	
	var a = MMDDYYYYString.split('/').join('').split('-').join('')
	return a.substring(4, 8) + a.substring(0, 2) + a.substring(2, 4)
}

function getAsMMDDYYYY(YYYYMMDDString, optionalToken) {
	if (!YYYYMMDDString) return null
	
	var t = optionalToken || ''
	var a = YYYYMMDDString.split('/').join('').split('-').join('')
	return a.substring(4, 6) + t + a.substring(6, 8) + t + a.substring(0, 4)
}

function getDateAsMMDDYYYY(dateObj) {
	  var month = (1 + dateObj.getMonth()).toString();
	  month = month.length > 1 ? month : '0' + month;
	  var day = dateObj.getDate().toString();
	  day = day.length > 1 ? day : '0' + day;
	  return month + '/' + day + '/' + dateObj.getFullYear();
}

function removeChars(val, charArray) {
	if (charArray.length) {
		for (var i = 0; i < charArray.length; i++)
			val = val.split(charArray[i]).join('')
	} else {
		val = val.split(charArray).join('')
	}
	return val
}

function allValsEmpty(elementIdList) {
	for (var i = 0; i < elementIdList.length; i++)
		if ($.trim($("#" + elementIdList[i]).val()) != '')
			return false
	return true
}

function getCookie(name) {
    var parts = document.cookie.split(name + "=");
    if (parts.length == 2)
    	return parts.pop().split(";").shift();
}


function emailInputContent(theId) {
	var emailVal = $("#" + theId).val()
	if ($.trim(emailVal) == '') {
		displayAttentionDialog('Please enter an email address.')
		return
	}
	location.href = 'mailto:' + emailVal
}

function copyInputText($textbox) {
	$textbox.focus()
	$textbox.select()
	copySelectionText()
}

function selectInputText($textbox) {
	$textbox.focus()
	$textbox.select()
}

function copyContainerText(containerId) {
	selectContainerText(containerId)
	copySelectionText()
}

function selectContainerText(containerId) {
    if (document.selection) {
        var range = document.body.createTextRange();
        range.moveToElementText(document.getElementById(containerId));
        range.select();
    } else if (window.getSelection()) {
        var range = document.createRange();
        range.selectNodeContents(document.getElementById(containerId));
        window.getSelection().removeAllRanges();
        window.getSelection().addRange(range);
    }
}

function copySelectionText(){
    try {
        document.execCommand("copy") // run command to copy selected text to clipboard
        return true
    } catch(e) {
        return false
    }
}

function getDateFromYYYYMMDD(yyyymmddString) {
	if (yyyymmddString == null || $.trim(yyyymmddString) == '') return null
	var matchResult = yyyymmddString.match(/([0-9]{4})[\/-]([0-9]{2})[\/-]([0-9]{2})/)
	if (!matchResult) return null
	return new Date(matchResult[1], (matchResult[2] - 1), matchResult[3])
}

function getDateFromMMDDYYYY(mmddyyyyString) {
	if (mmddyyyyString == null || $.trim(mmddyyyyString) == '') return null
	var matchResult = mmddyyyyString.match(/([0-9]{2})[\/-]([0-9]{2})[\/-]([0-9]{4})/)
	if (!matchResult) return null
	return new Date(matchResult[3], (matchResult[1] - 1), matchResult[2])
}

function daysBetween(d1, d2) {
	var millisPerDay = 24 * 60 * 60 * 1000
	var diffDays = (d2.getTime() - d1.getTime()) / millisPerDay
	return diffDays
}

function getTodayWithoutTime() {
	var tempDate = new Date()
	var todayWithoutTime = new Date(tempDate.getFullYear(), tempDate.getMonth(), tempDate.getDate())
	return todayWithoutTime
}

function defaultStr(s) {
	return s === null ? '' : s
}