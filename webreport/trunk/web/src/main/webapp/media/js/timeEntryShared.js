function prepareDateInput(data, $selector, selectFunc) {
	$selector.each(function() {
		var index = $(this).attr('index')
		
		$(this).attr('aria-label', 'Type the date in mm/dd/yyyy format, or push Alt one to exit the worksheet')
		$(this).keydown(function(e) {
			var keyCode = e.keyCode || e.which
			if (e.altKey && keyCode == 49)
				$("#postAllButton").focus()
			if (keyCode == 191 /* The forward slash character */ 
					|| keyCode == 111 /* keypad slash */) {
				var theVal = $(this).val()
				var found = theVal.match(/(.*)([0-9])_[\/].*/)
				if (found) {
					$(this).trigger($.Event('keydown', {which: 8})) // backspace
					// necessary zero
					$(this).trigger($.Event('keypress', {which: '0'.charCodeAt(0) }))
					// the number they typed
					$(this).trigger($.Event('keypress', {which: found[2].charCodeAt(0) }))
					 // forward slash
					$(this).trigger($.Event('keypress', {which: 191 }))
				}
			}
		})
		$(this).blur(function() {
			var pieces = $(this).val().replace(/_/g, '').split('/')
			if (pieces.length == 3 && pieces[2] == '')
				pieces.splice(2, 1)
			
			if (pieces.length < 2) {
				data[index].date = null
				return
			}
			
			if (pieces.length == 2 || pieces[2].length == 1 || pieces[2].length == 3) {
				// autofill complete year
				var month = parseInt(pieces[0])
				var day = parseInt(pieces[1])
				
				var total = 100 * month + day
				
				var now = new Date()
				var thisYear = now.getFullYear()
				var curMMDD = ((now.getMonth() + 1) * 100) + now.getDate()
				if (total > assumePriorYearAfterMMDD && curMMDD < assumePriorYearAfterMMDD)
					thisYear--
				
				$(this).val(getDateStr(month, day, thisYear))
			} else if (pieces.length == 3 && pieces[2].length == 2) {
				// autofill partial year
				var month = parseInt(pieces[0])
				var day = parseInt(pieces[1])
				var year = 2000 + parseInt(pieces[2])
				$(this).val(getDateStr(month, day, year))
			}
			
			if (!validateDate($(this).val()))
				$(this).val('')
			
			if (index) /*
							 * wouldn't exist if our date input was outside the
							 * time entry table - CPB
							 */
				data[index].date = getDateFromMMDDYYYY($(this).val())
		})
		
		$(this).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true,
			onSelect : selectFunc
		})
		$(this).mask(twoDigitDateMask)
	})
}

function prepareHoursInput(data, $selector, inWorksheet, allowNegative) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Enter the number of hours worked with up to two decimal places'
				+ (inWorksheet ? ', or push Alt one to exit the worksheet' : ''))
		$(this).keydown(function(e) {
			var keyCode = e.keyCode || e.which
			if (inWorksheet && e.altKey && keyCode == 49) {
				$("#postAllButton").focus()
			}
		})
		$(this).blur(function() {
			var v = $(this).val()
			var dotIndex = v.indexOf('.')
			if (dotIndex != -1) {
				v = v.substring(0, dotIndex) + '.' + v.substring(dotIndex + 1).replace(/[\.]/,'')
			}
			
			var isNegative = v.charAt(0) == '-'
			
			v = v.replace(/[^0-9\.]/g, '')
			var index = $(this).attr('index')
			
			if (v != '.' && v != '') {
				var newVal = '' + Math.round((v * 100) / 25.0) * 0.25
				var tokens = newVal.split('.')
				
				if (tokens.length == 1) {
					newVal = tokens[0] + '.00'
				} else {
					newVal = tokens[0] + '.' + rpad(tokens[1], 2)
				}
				
				if (isNegative) newVal = '-' + newVal
				$(this).val(newVal)
				
				if (inWorksheet) {
					if (index)
						data[index].hours = $(this).val()
				}
			} else {
				$(this).val('')
				if (inWorksheet) {
					if (index)
						data[index].hours = null
				}
			}
		})
	})
}

function prepareRowDeleteIcon($selector) {
	$selector.each(function() {
		var index = $(this).attr('index')
		
		$(this).attr('aria-label', 'Click to remove this row, or push Alt one to exit the worksheet')
		$(this).keydown(function(e) {
			var keyCode = e.keyCode || e.which
			if (e.altKey && keyCode == 49) {
				$("#postAllButton").focus()
			}
		})
	})
}

var autoAddNewRowEl
function rebindAutoAddRowFn(tableId) {
	if (autoAddNewRowEl)
		autoAddNewRowEl.off("keydown.wrAutoAdd")
	autoAddNewRowEl = $(":tabbable", tableId).eq(-2)
	autoAddNewRowEl.on("keydown.wrAutoAdd", function(e) {
		var keyCode = e.keyCode || e.which
		if (keyCode == $.ui.keyCode.TAB && !e.shiftKey) {
			e.preventDefault()
			addInputRow()
			rebindAutoAddRowFn(tableId)
		}
	})
}

function getDateStrFromDate(date) {
	return getDateStr(date.getMonth() + 1, date.getDate(), date.getFullYear())
}

function getDateStr(month, date, year) {
	return pad(month, 2) + '/' + pad(date, 2) + '/' + year
}

function getPaddedHoursStr(hoursVal) {
	if (hoursVal == null) return null
	return hoursVal.toFixed(2)
}

function bindAlt1Keydown($this) {
	var that = $this
	$this.keydown(function(e) {
		var index = that.attr('index')
		
		var keyCode = e.keyCode || e.which
		/* Alt-"1" */
		if (e.altKey && keyCode == 49) {
			$("#postAllButton").focus()
		}
	})
}

function pageToFirstMatchingRow(tableEl, rowSelectorFn) {
    var numberOfRows = tableEl.data().length
    var rowsOnOnePage = tableEl.page.len()
    if (rowsOnOnePage < numberOfRows) {
    	var theRow = tableEl.row(rowSelectorFn)
    	if (theRow == null) {
    		tableEl.page(0).draw(false)
    		return
    	}
    	
        var selectedNode = theRow.node()
        var nodePosition = tableEl.rows({order: 'current'}).nodes().indexOf(selectedNode)
        if (nodePosition != -1) {
	        var pageNumber = Math.floor(nodePosition / rowsOnOnePage)
	        tableEl.page(pageNumber).draw(false)
        }
    }
}