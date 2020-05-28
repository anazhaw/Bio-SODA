function executeSqlQuery(url, sqlStmt, resultDivId) {

	// prepare request (asynchronous POST)
	var request = new XMLHttpRequest();
	request.open('POST', url, true);
	request.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
	request.onreadystatechange = function(aEvt) {
		if (request.readyState == 4) {
			if (request.status == 200) {
				var xmldoc = request.responseXML;
				var xmlstr = null;
				if (window.ActiveXObject) { // IE
					xmlstr = xmldoc.xml;
				} else {
					xmlstr = new XMLSerializer().serializeToString(xmldoc);
				}
				document.getElementById(resultDivId).innerHTML = xmlstr;
				console.log("XMLString: " + xmlstr);
			} else {
				alert("Response: status code = " + request.status + ", text = "
						+ request.responseText);
			}
		}
	};

	// send request
	var xml = "<sql>" + sqlStmt + "</sql>";
	request.send(xml);
}

function executeSparqlQuery(url, sparqlStmt, resultDivId) {
        // prepare request (asynchronous POST)
        var request = new XMLHttpRequest();
        request.open('POST', url, true);
        request.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
        request.onreadystatechange = function(aEvt) {
                if (request.readyState == 4) {
                        if (request.status == 200) {
                                var xmldoc = request.responseXML;
                                var xmlstr = null;
                                if (window.ActiveXObject) { // IE
                                        xmlstr = xmldoc.xml;
                                } else {
                                        xmlstr = new XMLSerializer().serializeToString(xmldoc);
                                }
                                document.getElementById(resultDivId).innerHTML = xmlstr;
                                console.log("XMLString: " + xmlstr);
                        } else {
                                console.log("Response: status code = " + request.status + ", text = "
                                                + request.responseText);
                        }
                }
        };

        // send request
        var xml = "<sparql>" + sparqlStmt + "</sparql>";
        request.send(xml);
}
