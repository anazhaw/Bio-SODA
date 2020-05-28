/**
 * Toggle display of block element.
 * 
 * @param elementId
 *            id of block element to toggle
 */
function toggleDisplay(elementId) {

	// this is the way the standards work
	var style = document.getElementById(elementId).style;
	style.display = (style.display == 'none') ? 'block' : 'none';
}
