function rebuildTableFilters(theTableId) {
	var table = $('#' + theTableId).DataTable()
	table.draw()
	table.columns('.select-filter').every(
			function(colIndex) {
				var that = this
				var header = $(this.header())
				var selectSplitter = header.attr("selectSplitter")

				var firstRowHeader = header.parent().prev().find('td').eq(
						header.index())
				var select = $(
						'<select id="' + theTableId + '_filter' + colIndex
								+ '" />').on(
						'change',
						function() {
							var theVal = $(this).val()
							var theStr = null
							if ($.trim(theVal) == '') {
								theStr = '^$'
							} else if (theVal == '(all)') {
								theStr = ''
							} else {
								var escapedVal = $.fn.dataTable.util
										.escapeRegex(theVal)
								if (selectSplitter) {
									var escapedDelimiter = $.fn.dataTable.util
											.escapeRegex(selectSplitter)
									theStr = '^' + escapedVal + '$|'
											+ escapedDelimiter + '\\s*'
											+ escapedVal + '|' + escapedVal
											+ '\\s*' + escapedDelimiter
								} else {
									theStr = '^' + escapedVal + '$'
								}
							}
							that.search(theStr, true, false).draw()
						})
				var searchVal = this.search()
				firstRowHeader.empty().append(select)
				var theData = this.cache('search').unique().sort()

				var sortFunction = header.attr("selectFilterSortFunction")
				var sortFn = sortFunction ? eval("var f = function(){ return "
						+ sortFunction + ";}; f();") : null

				var finalData = new SortedArray([], sortFn)
				var matchedVal = null

				if (selectSplitter) {
					theData.each(function(d) {
						var splitVals = d.split(selectSplitter)
						for (var i = 0; i < splitVals.length; i++) {
							var splitVal = $.trim(splitVals[i])
							if (finalData.search(splitVal) == -1)
								finalData.insert(splitVal)
							if (searchVal != ""
									&& new RegExp(searchVal).test(splitVal
											+ selectSplitter))
								matchedVal = splitVal
						}
					})
				} else {
					theData.each(function(d) {
						if (finalData.search(d) == -1)
							finalData.insert(d)
						if (searchVal != "" && new RegExp(searchVal).test(d))
							matchedVal = d
					})
				}

				select.append($('<option value="(all)">(all)</option>'))
				for (var i = 0; i < finalData.array.length; i++) {
					var d = finalData.array[i]
					select.append($('<option></option>').attr('value', d).text(d))
				}

				if (matchedVal != null)
					select.val(matchedVal).change()
			})
}

function dataTableCustomRenderDate(data, type, sortFormat, displayFormat) {
	if (!data || data == '') {
		return ''
	}

	if (type === 'display') {
		if (typeof data == 'object') {
			/* Assume it's a VistaDate */
			return data.sensitive ? 'Sensitive' : escapeHTML(data.description)
		} else {
			/*
			 * Assume it's a ZonedDateTime (integer) or something equally
			 * compatible with XDate
			 */
			return new XDate(data).toString(displayFormat)
		}
	} else if (type === 'sort') {
		if (typeof data == 'object') {
			/* Assume it's a VistaDate */
			return !data.sensitive && data.normalized ? new XDate(
					data.normalized).toString(sortFormat) : ""
		} else {
			/*
			 * Assume it's a ZonedDateTime (integer) or something equally
			 * compatible with XDate
			 */
			return new XDate(data).toString(sortFormat)
		}
	}

	return data;
}

var dataTableDefaultOptions = {
	"columnDefs" : [
			{
				"targets" : "_all",
				"defaultContent" : ''
			},
			/*
			 * Neat way of adding both sorting and formatting for date columns
			 * just by setting the class of the <TH> - CPB
			 */
			{
				"targets" : "datetime",
				"render" : function(data, type, row, meta) {
					return dataTableCustomRenderDate(data, type,
							"yyyyMMddHHmm", "M/d/yyyy HH:mm")
				}
			},
			{
				"targets" : "date",
				"render" : function(data, type, row, meta) {
					return dataTableCustomRenderDate(data, type, "yyyyMMdd",
							"M/d/yyyy")
				}
			} ],
	"language" : {
		"emptyTable" : "No entries yet.",
		"infoFiltered" : "(filtered from _MAX_ total entries)",
		"loadingRecords" : "Please wait - loading..."
	},
	"retrieve" : true,
	"stateLoaded" : function(settings, data) {
		/*
		 * If our loaded state has a length setting which isn't available in our
		 * current table length's, override and select the first current table
		 * length. Sometimes we get a 1D array of [numbers], sometimes a 2D
		 * array of [[numbers], [display names]] - CPB
		 */
		var searchArr = settings.aLengthMenu
		if ($.isArray(settings.aLengthMenu[0])) {
			searchArr = settings.aLengthMenu[0]
		}

		if (searchArr.indexOf(data.length) == -1) {
			$("#" + settings.sTableId).DataTable().page.len(searchArr[0])
		}
	}
}

$.extend($.fn.dataTable.defaults, dataTableDefaultOptions)