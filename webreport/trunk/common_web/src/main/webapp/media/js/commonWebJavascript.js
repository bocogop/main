jQuery.fn.reverse = [].reverse

function logoutConfirm() {
	confirmDialog("Are you sure you want to logout?", function() {
		document.location.href = homePath + '/logout.htm'
	})
}

function showPageTitle() {
	$("#appTitle").show()
}
function hidePageTitle() {
	$("#appTitle").hide()
}
function setPageTitleText(pageTitle) {
	$("#pageTitleSpan").text(pageTitle)
}
function setPageTitleHtml(pageTitleHtml) {
	$("#pageTitleSpan").html(pageTitleHtml)
}


function jumpToVoter(id) {
	document.location.href = homePath + '/voterEdit.htm?id=' + id
}

function menuVoterSelectedCallback(voterObj) {
	jumpToVoter(voterObj.id)
}

function menuVoterAddSelectedCallback() {
	document.location.href = homePath + "/voterCreate.htm"
}