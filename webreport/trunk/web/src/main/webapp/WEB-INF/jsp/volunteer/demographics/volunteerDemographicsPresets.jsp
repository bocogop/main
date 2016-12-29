<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	$(function() {
		var presetCols = {
			"activeInactives" : [
				${COL_INDEX_NAME},
				${COL_INDEX_ENTRY_DATE},
				${COL_INDEX_STATUS},
				${COL_INDEX_ACTIVE_ASSIGNMENTS},
				${COL_INDEX_LAST_VOLUNTEERED_DATE} ],
			"alphabetical" : [
				${COL_INDEX_NAME},
				${COL_INDEX_DOB},
				${COL_INDEX_AGE},
				${COL_INDEX_GENDER},
				${COL_INDEX_IDENTIFYING_CODE},
				${COL_INDEX_ENTRY_DATE},
				${COL_INDEX_FULL_ADDRESS},
				${COL_INDEX_FULL_CONTACT},
				${COL_INDEX_LAST_VOLUNTEERED_DATE},
				${COL_INDEX_ACTIVE_ASSIGNMENTS}	],
			"newVolunteers" : [
				${COL_INDEX_NAME},
				${COL_INDEX_IDENTIFYING_CODE},
				${COL_INDEX_AGE_GROUP},
				${COL_INDEX_ENTRY_DATE},
				${COL_INDEX_PRIMARY_FACILITY},
				${COL_INDEX_LAST_VOLUNTEERED_DATE},
				${COL_INDEX_TOTAL_HOURS}],
			"separatedFromService" : [
				${COL_INDEX_NAME},
				${COL_INDEX_STATUS},
				${COL_INDEX_STATUS_DATE},
				${COL_INDEX_LAST_VOLUNTEERED_DATE},
				${COL_INDEX_TOTAL_HOURS}]
		}
		var presetFilterVals = {
			"activeInactives" : {
				"#rxIncludeActive" : true,
				"#rxIncludeInactive" : false,
				"#rxIncludeTerminated" : false,
				"#rxIncludeTerminatedByCause" : false,
				"#isLocal" : true,
				"#rxLastVolOptions" : "havent",
				"#rxHaventLastVolOption" : "haventLastVolIn30"
			},
			"alphabetical" : {
				"#rxIncludeActive" : true,
				"#rxIncludeInactive" : false,
				"#rxIncludeTerminated" : false,
				"#rxIncludeTerminatedByCause" : false,
				"#isLocal" : true
			},
			"newVolunteers" : {
				"#rxIncludeActive" : true,
				"#rxIncludeInactive" : true,
				"#rxIncludeTerminated" : true,
				"#rxIncludeTerminatedByCause" : true,
				"#isLocal" : true,
				"#filterEntryDateMonth" : ${curMonth},
				"#filterEntryDateYear" : ${curYear}
			},
			"separatedFromService" : {
				"#rxIncludeActive" : false,
				"#rxIncludeInactive" : true,
				"#rxIncludeTerminated" : true,
				"#rxIncludeTerminatedByCause" : true,
				"#isLocal" : true,
				"#rxStatusDateOptions" : "within2FY"
			}
		}
		var presetSorts = {
			"activeInactives" : [${COL_INDEX_LAST_VOLUNTEERED_DATE}, 'asc'],
			"alphabetical" : [${COL_INDEX_NAME}, 'asc'],
			"newVolunteers" : [${COL_INDEX_NAME}, 'asc'],
			"separatedFromService" : [${COL_INDEX_STATUS_DATE}, 'desc']
		}
		var presetLengths = {
			"activeInactives" : 5000,
			"alphabetical" : 5,
			"newVolunteers" : 50,
			"separatedFromService" : 1000
		}
		
		$("#presetSelect").change(function() {
			var v = $(this).val()
			if (v == 'custom') return
			
			var theTable = $("#volunteerList").DataTable()
			
			resetTable()
			
			// set specific column visibilities & checkboxes
			for (var i = 0; i < presetCols[v].length; i++) {
				$(".columnCheckbox", "#paramsTable").eq(presetCols[v][i]).prop('checked', true)
			}
			
			syncColumns()
			
			var colFilterVals = presetFilterVals[v]
			$.each(colFilterVals, function(key, value) {
				if ($.type(value) == 'boolean') {
					$(key).prop('checked', value)
				} else {
					$(key).val(value)
					
					if ($(key).hasClass('columnFilter')) {
						var myVal = getColumnFilterVal($(key))
						var colIndex = parseInt($(key).attr('colIndex'))
						theTable.columns(colIndex + 1).search(myVal, true, false)
					} else {
						$(key).trigger('change')
					}
				}
			})
			
			// set my own val once more since triggering "change" to certain inputs above sets me to "custom"
			$(this).val(v)
			
			// set page length			
			$("select[name='volunteerList_length']").val(presetLengths[v])
			theTable.page.len(presetLengths[v])
			// set sort
			var sort = presetSorts[v]
			theTable.order(sort ? [sort[0] + 1, sort[1]] : [1, 'asc'])
			
			theTable.draw()
		})
	})
</script>

<div style="margin-left: 50px; display:inline-block">
	Presets: <select id="presetSelect"><option value="alphabetical" selected="selected">Alphabetical List</option>
		<option value="activeInactives">Potentially Inactive Volunteers</option>
		<option value="separatedFromService">Separated from Service</option>
		<option value="newVolunteers">New Volunteers</option>
		<option value="custom">Custom</option>
		</select>
</div>