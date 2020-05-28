wmtt = null;

document.onmousemove = updateTT;

function updateTT(e) {
	x = (document.all) ? window.event.x + document.body.scrollLeft : e.pageX;
	y = (document.all) ? window.event.y + document.body.scrollTop : e.pageY;
	if (wmtt != null) {
		wmtt.style.left = (x + 20) + "px";
		wmtt.style.top = (y + 20) + "px";
	}
}

function showTT(id) {
	wmtt = document.getElementById(id);
	wmtt.style.display = "block"
}

function hideTT() {
	wmtt.style.display = "none";
}
