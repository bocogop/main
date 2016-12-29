<%@ include file="inc_header.jsp"%>

<script type="text/javascript">
	/**
	 * Take everything within the #appContainerDiv and change it so it can only be viewed
	 * changes the title, text inputs, textareas, checkboxes and select options
	 */
	//do this on load since some select boxes are put in place by javascript (i.e. focus)
	$(window).load(function() {
		toView()
	})

	function toView() {
		$('#pageTitleSpan').text($('#pageTitleSpan').text().replace("Modify", "View"))
		$(':text', '#appContainerDiv').not(".alwaysEnabled").each(function() {
			$(this).prop('disabled', true)
			// $(this).replaceWith(escapeHTML($(this).val()))
		})
		$('textarea', '#appContainerDiv').not(".alwaysEnabled").prop(
				'disabled', true).css("color", "black")
		$(':checkbox', '#appContainerDiv').not(".alwaysEnabled").prop(
				'disabled', true).css("color", "black")
		$(':radio', '#appContainerDiv').not(".alwaysEnabled").prop('disabled',
				true).css("color", "black")
		$(':button', '#appContainerDiv').not(".alwaysEnabled").prop('disabled',
				true).css("color", "black")
		$('.requdIndicator', '#appContainerDiv').remove()
		$('select', '#appContainerDiv').not(".alwaysEnabled").prop('disabled', true)
		$('select', 'div.dataTables_length').prop('disabled', false)
		$('table.dataTable thead select', '#appContainerDiv').prop('disabled', false)
		
		$('.actionable', '#appContainerDiv').hide()
		$('.ui-datepicker-trigger', '#appContainerDiv').not(".alwaysEnabled").hide()
		$('input.alwaysEnabled ~ .ui-datepicker-trigger', '#appContainerDiv').show()
		applyFocusToFirstActiveField()
	}
</script>