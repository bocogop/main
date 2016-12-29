<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript" src="${jsHome}/attentionDialog.js"></script>

<style>
div#attentionDialog div.expandTitle {
	font-size: 10pt;
	text-decoration: none;
	font-style: normal;
}
</style>

<div id="attentionDialog" style="display: none;"
	title="Attention" role="alertdialog" aria-labelledby="alertHeading" aria-describedby="attentionDialogMessage">
	<%-- 508 compatibility - CPB --%>
	<h1 id="alertHeading" style="display:none">Attention</h1>
	<table align='center' cellpadding="6">
		<tr valign="top">
			<td><span class='ui-icon ui-icon-alert'></span></td>
			<td id="attentionDialogMessage"></td>
		</tr>
		<tr id="attentionDialogDetailsRow" style="display: none">
			<td colspan="2" align='left'><a id="attentionDialogDetailsLink"
				href="#attnDialogDetails" rel="#attentionDialogDetailsDiv"
				style="text-decoration: none;"></a>
				<div id="attentionDialogDetailsDiv" class="toggleDiv"></div>
			</td>
		</tr>
	</table>
</div>
