<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	$(function() {
		var presetCols = {
			"alphabetical" : [
				${COL_INDEX_NAME},
				${COL_INDEX_VOTER_ID},
				${COL_INDEX_PRECINCT},
				${COL_INDEX_PARTY},
				${COL_INDEX_STATUS},
				${COL_INDEX_FULL_ADDRESS},
				${COL_INDEX_GENDER},
				${COL_INDEX_AGE_APPROX},
				${COL_INDEX_FULL_CONTACT}	],
			"newVoters" : [
				${COL_INDEX_NAME},
				${COL_INDEX_VOTER_ID},
				${COL_INDEX_PRECINCT},
				${COL_INDEX_PARTY},
				${COL_INDEX_AFFILIATED_DATE},
				${COL_INDEX_FULL_ADDRESS},
				${COL_INDEX_GENDER},
				${COL_INDEX_AGE_APPROX},
				${COL_INDEX_FULL_CONTACT}],
			"newRepublicans" : [
				${COL_INDEX_NAME},
				${COL_INDEX_VOTER_ID},
				${COL_INDEX_PRECINCT},
				${COL_INDEX_PARTY},
				${COL_INDEX_AFFILIATED_DATE},
				${COL_INDEX_FULL_ADDRESS},
				${COL_INDEX_GENDER},
				${COL_INDEX_AGE_APPROX},
				${COL_INDEX_FULL_CONTACT}]
		}
		var presetFilterVals = {
			"alphabetical" : {
				"#filterStatus" : 'Active',
			},
			"newVoters" : {
				"#rxAffiliatedDateOptions" : "within60Days",
				
			},
			"newRepublicans" : {
				"#rxAffiliatedDateOptions" : "within60Days",
				"#filterParty" : '7'
			}
		}
		var presetSorts = {
			"alphabetical" : [${COL_INDEX_NAME}, 'asc'],
			"newVoters" : [${COL_INDEX_AFFILIATED_DATE}, 'asc'],
			"newRepublicans" : [${COL_INDEX_AFFILIATED_DATE}, 'asc']
		}
		var presetLengths = {
			"alphabetical" : 5,
			"newVoters" : 50,
			"newRepublicans" : 1000
		}
		
		$("#presetSelect").change(function() {
			var v = $(this).val()
			if (v == 'custom') return
			
			var theTable = $("#voterList").DataTable()
			
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
			$("select[name='voterList_length']").val(presetLengths[v])
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
		<option value="newVoters">New Voters Within 60 Days</option>
		<option value="newRepublicans">New Republicans Within 60 Days</option>
		<option value="custom">Custom</option>
		</select>
</div>