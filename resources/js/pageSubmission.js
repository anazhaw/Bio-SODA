
function submitToPage(pagename) {
	var form = document.getElementById("query_form");
	form.action = "?page=" + pagename;
	form.submit();
}