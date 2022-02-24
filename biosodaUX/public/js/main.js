// refactor?
// https://medium.com/ninjaconcept/interactive-dynamic-force-directed-graphs-with-d3-da720c6d7811

// first some variables we will need across the whole code
var biosodacolor = ['#26348b', '#bf5e22', '#f1c1a4', '#987651'];
// this container is already present in the html
var d3table = d3.select('#example');

// we need this to fit the graph into the window calculated width/height
var graphratio = 2;

// pattern to get last element of IRI e.g. FunctionalProperty, scientificName, ...
// zber: was tmppattern = new RegExp(/^.*[\#/]([^>]*)>?$/);
var tmppattern = new RegExp(/^.*[\#\/]([^>]*)>?$/);

// we need it all over - should not be changeable since we have to requery anyway on change
autosizer = '<input name="tags[]" value="" class="autosize" disabled="disabled">';

// here we store all our knowledge
overallobject = [];

// d3 colors
var conceptscolors_range_max = 100;
var conceptscolors = d3.scaleLinear()
.domain([1, conceptscolors_range_max])
.range(biosodacolor)
.interpolate(d3.interpolateHcl);

var conceptscolors_range_max = 2 * Math.PI;
var conceptscolors = d3.scaleSequential()
.domain([0, conceptscolors_range_max])
.interpolator(d3.interpolateRainbow);

// a bunch of variables
allthingsdata = []
allthingsdata['groups'] = [];
allthingsdata['nodes'] = [];
allthingsdata['links'] = [];

// helper function to escape HTML in a string
var escapeHtmlEntityMap = {
	'&': '&amp;',
	'<': '&lt;',
	'>': '&gt;',
	'"': '&quot;',
	"'": '&#39;',
	'/': '&#x2F;',
	'`': '&#x60;',
	'=': '&#x3D;'
};
function escapeHtml (string) {
	return String(string).replace(/[&<>"'`=\/]/g, function (s) {
		return escapeHtmlEntityMap[s];
	});
}

// this function pastes the data from tabledata to the data table
function fillData(remove = false) {
	console.log('fillData');
	// if we don't run it for the first time, we need to delete the old values
	if (typeof(d3tablehead) !== 'undefined') {
		d3tablehead.remove();
		d3tablebody.remove();
		d3table.selectAll('thead').remove();
	}
	d3tablehead = d3table.append('thead').append('tr');
	d3tablebody = d3table.append('tbody');
	
	// for every column in the data, there should be a column header
	d3tablehead.selectAll('th')
	.data(tabledata).enter()
	.append('th')
	.html(function (d) {
		rettext = d.columntitle;
		// if column is part of results[0]['filtered_concepts'], we mark it as such
		if (typeof(overallobject.answer_altered.results[0]) != 'undefined') {
			isfilter = overallobject.answer_altered.results[0]['filtered_concepts'].find(
				element =>
				element == d.columntitle
			);
			if (typeof(isfilter) != 'undefined') {
				rettext = rettext + '<sup class="fas fa-filter" title="this column uses a filter" style="color: #bbb;"></sup>';
			}
		}
		return rettext;
	})
	.attr('data-columntitle' , function (d) {
		return d.columntitle
	})
	.attr('data-hovertarget', function(d) {
		return d.id;
	})
	.attr('data-group', function(d) {
		return d.group;
	})
	.attr('style', function (d) {
		var lastpart = null;
		if (typeof(d.technicalname) != 'undefined') {
			lastpart = d.technicalname.match(tmppattern);
		}
		// look for original thing using id
		nodes.nodes().forEach(function (v, i) {
			if (v.__data__.id == d.id) {
				newcolor = v.__data__.groupcolor;
			}
		});
		
		if (typeof(newcolor) != "undefined") {
			return 'border-bottom: 1em ' + newcolor + ' solid';
			} else {
			return false
		}
	})
	
	// now we fill the data into the table cells
	d3tablebody.selectAll('td')
	.html(function () {
		var thisrow = this.parentNode.getAttribute('data-rownum');
		var thiscol = this.getAttribute('data-colnum');
		return '<div class="watermark"></div>' + tabledata[thiscol].values[thisrow];
	})
	
	d3tablehead.selectAll('th')
	.filter(function(d) {
		//if (d.original) return false;
		return true;
	})
	.classed('notoriginal', true)
	.append('div')
	.attr('class', 'fas fa-times leftright closetimes')
	.html('')
	.attr('onclick', 'pushDatatable(this.__data__)')
	
	// if we hover somtehing connected to a concept, we want everything connected to this concept to be hovered
	$('[data-hovertarget]')
	.on( 'click', function (e) {
		$('[data-hovertarget="' + $(this).attr('data-hovertarget') + '"]').each(function(index, value) {
			nodes.nodes().map(function (d, i) {
				if (typeof(d.__data__['id']) != 'undefined' && d.__data__['id'] == value.__data__['id']) {
					d.dispatchEvent(new MouseEvent("click"));
				}
			}, value, index);
		});
	})
	.on( 'mouseover', function (e) {
		nodes.nodes().map(function (d) {
			if (
				typeof(d.__data__['id']) != 'undefined' // id has to be defined
				&&
				d.__data__['id'] == $(e.target).attr('data-hovertarget') // id has to be same in target element
				) {
				d.dispatchEvent(new MouseEvent("mouseover"));
			}
		}, e);
	})
	.on( 'mouseout', function (e) {
		nodes.nodes().map(function (d, i) {
			d.dispatchEvent(new MouseEvent("mouseout"));
		});
	});
	showTabledata(true);
	$('.double-scroll').doubleScroll();
}
// we have got a table for all manually chosen data
function pushDatatableManual(data) {
	var deleted = false; // set default
	tabledata_manual = $.grep(tabledata_manual, function (single, i) { // loop over all existing data and remove deleted parts
		if (JSON.stringify(single) == JSON.stringify(data)) { // if new data is already present
			deleted = true; // set as deleted
			return false; // don't give back this one to tabledata variable
		}
		return true;
	});
	if (deleted === false) { // only push if new node is not already present
		tabledata_manual.push(data);
	}
	pushDatatable(data);
}

function pushDatatable(data, wait = false) {
	var deleted = false; // set default
	tabledata = $.grep(tabledata, function (single, i) { // loop over all existing data and remove deleted parts
		if (JSON.stringify(single) == JSON.stringify(data)) { // if new data is already present
			deleted = true; // set as deleted
			return false; // don't give back this one to tabledata variable
		}
		return true;
	});
	if (deleted === false) { // only push if new node is not already present
		tabledata.push(data);
		} else { // remove it from datatable
		tabledata = tabledata.filter(function(d) {
			if (d == data) {
				return false;
				} else {
				return true;
			}
		});
	}
	if (wait == false) { // only fill the visible data table if it's not during a list of pushes
		fillData();
	}
}

function svginit() {
	console.log('svginit');
	$("#svg svg").remove();
	$('#d3graph').show();
	svg = d3.select("#svg")
		.append('svg')
		.attr("width", $('#svg').width())
		.attr("height", $('#svg').height());
	
	width = +svg.attr("width");
	height = +svg.attr("height");
	
	graphratio = width/height;
	
	// Define the div for the tooltip
	tooldiv = svg.append("div")
	.attr("id", "tooltip")
	.style("opacity", 0)
	
	defs = svg.append('defs')
	
	defs.append('marker')
	.attrs({'id':'arrowhead-start',
		'orient':'auto',
		'refY': 2.5,
		'refX': -25,
		'markerWidth':5,
		'markerHeight':5,
	})
	.append('svg:path')
	.attr('d', 'M0,0 L5,3 0,5')
	.attr('fill', 'none')
	.attr('stroke', '#666')
	
	defs.append('marker')
	.attrs({'id':'arrowhead-end',
		'orient':'auto',
		'refY': 2.5,
		'refX': 25,
		'markerWidth':5,
		'markerHeight':5,
	})
	.append('svg:path')
	.attr('d', 'M0,0 L5,3 0,5')
	.attr('fill', 'none')
	.attr('stroke', '#666')
	
	container = svg.append("g");
	
	linkgroup = container
	.append("g")
	.attr("class", "links");
	
	nodegroup = container
	.append("g")
	.attr("class", "nodes");
	
	simulation = d3.forceSimulation([])
		.force("charge", d3.forceManyBody().strength(-10000))
		// .force("charge", d3.ellipseForce())
		// .force("collide", d3.ellipseForce(6, 0.5, 5))
		.force("center", d3.forceCenter(width / 2, height / 2))
		.force("x", d3.forceX(width / 2).strength(1))
		.force("y", d3.forceY(height / 2).strength(1))
		.force("link", d3.forceLink([]).id(function(d) {return d.id; }).distance(150).strength(1));
	
	zoomFunc = d3.zoom()
	.scaleExtent([0.1, 40])
	.on("zoom", zoomed);
	
	svg.call(zoomFunc)
	.on("dblclick.zoom", null);
	
	loadMaingraph();
}

function zoomed(){
	container.attr("transform",d3.event.transform);
}

function dragstarted(d) {
	if (!d3.event.active) simulation.alphaTarget(0.3).restart();
	d.fx = d.x;
	d.fy = d.y;
}

function dragged(d) {
	d.fx = d3.event.x;
	d.fy = d3.event.y;
}

function dragended(d) {
	if (!d3.event.active) simulation.alphaTarget(0);
	d.fx = null;
	d.fy = null;
}

function ticked() {
	// console.log('ticked');
	nodes
	.attr("transform", function(d) {
		// if (Math.random() > 0.99) console.log(d.x);
		return "translate(" + d.x + "," + d.y/graphratio + ")";
	})
	.classed('userchosen', function (d) {
		return d.userchosen ? true : false;
	})
	.classed('foundconcept', function (d) {
		return d.foundconcept ? true : false;
	})
	
	nodes.selectAll('circle')
	.attr("fill", function(d, i) {
		if (d.userchosen) {
			return('url("#gradient_' + d.index + '")');
			// return d.groupcolor;
			} else {
			return('url("#gradient_' + d.index + '")');
			// return d3.rgb(d.groupcolor).brighter(0.5);
		}
	})
	.attr("r", function(d) {
		if (d.instanceOf == '<http://www.w3.org/2002/07/owl#Class>') {
			return 15; // 35;
			} else if (d.userchosen) {
			return 25;
			} else {
			return 15;
		}
	})
	
	links.selectAll("line")
	.attr("x1", function(d) {
		// if (Math.random() > 0.99) console.log(d.source);
		return d.source.x;
	})
	.attr("y1", function(d) { return d.source.y/graphratio; })
	.attr("x2", function(d) { return d.target.x; })
	.attr("y2", function(d) { return d.target.y/graphratio; })
	.attr("stroke", function(d, i) {
		if (d.userchosen) {
			return d.groupcolor;
			} else {
			return d3.rgb(d.groupcolor).brighter(0.5);
		}
	})
	.classed('userchosen', function (d) {
		return d.userchosen ? true : false;
	})
	.attr('stroke-dasharray', function(d, i) {
		if (d.userchosen && d.shortestlink) {
			return '3 1';
			} else if (d.attachlink && d.shortestlink) {
			return '4 1';
			} else if (d.userchosen) {
			return '1 0';
			} else if (d.attachlink) {
			return '1 1';
			} else {
			return '5 2';
		}
	})
	.attr('stroke-width', function(d, i) {
		if (d.userchosen && d.shortestlink) {
			return '30';
			} else if (d.attachlink && d.shortestlink) {
			return '20';
			} else if (d.shortestlink) {
			return '10';
			} else if (d.userchosen) {
			return '5';
			} else if (d.attachlink) {
			return '2';
			} else {
			return '1';
		}
	})
	
	links.selectAll("text")
	.attr('x', function (d) {
		newpos = d.source.x - (d.source.x - d.target.x)/2;
		return newpos ? newpos : 0;
	})
	.attr('y', function (d) {
		newpos = d.source.y - (d.source.y - d.target.y)/2;
		return newpos ? newpos/graphratio : 0;
	})
}

function centerNode(nodeindex = 0) {
	console.log(nodeindex);
	tmptarget = nodes.nodes()[nodeindex];
	matrix = tmptarget.transform.baseVal[0].matrix
	
	var parent = container.node().parentElement;
	var fullWidth = parent.clientWidth,
	fullHeight = parent.clientHeight;
	
	zoomfac = d3.zoomTransform(svg.node()).k;
	svg.call(
		zoomFunc.transform,
		d3.zoomIdentity
		.translate(
			-matrix.e*zoomfac+fullWidth/2,
			-matrix.f*zoomfac+fullHeight/2
		)
		.scale(zoomfac)
	);
}

function centerGraph() {
	// var bounds = container.node().getBBox(); // includes very loooong text also
	var bounds = nodegroup.node().getBBox();
	// var boundsnotext = svg.selectAll('circle').node().getBBox(); // does not work, only calculates one single element
	
	var parent = container.node().parentElement;
	var fullWidth = parent.clientWidth,
	fullHeight = parent.clientHeight;
	var width = bounds.width,
	height = bounds.height;
	var midX = bounds.x + width / 2,
	midY = bounds.y + height / 2;
	if (width == 0 || height == 0) return; // nothing to fit
	var scale = (0.75) / Math.max(width / fullWidth, height / fullHeight);
	var translate = [fullWidth / 2 - scale * midX, fullHeight / 2 - scale * midY];
	
	svg.call(
		zoomFunc.transform,
		d3.zoomIdentity
		.translate(translate[0], translate[1])
		.scale(scale)
	);
}

function loadMaingraph() {
	console.log('loadMaingraph');
	// can be downloaded from here: biosoda.expasy.org:4481/soda/api/?q=main_graph
	$.ajax({
		url: "main_graph.json", // local file is faster - data does not change a lot
		// url: "https://biosoda.expasy.org:4481/soda/api/?q=main_graph",
	}).done(function(data)
	{
		main_graph = data;
		// inject main_graph received at first API call
		if (typeof(overallobject) != 'undefined' && typeof(overallobject.answer_original) != 'undefined') {
			data = overallobject.answer_original.results.main_graph;
		}
		// inject main_graph received at second API call
		if (typeof(overallobject) != 'undefined' && typeof(overallobject.answer_altered) != 'undefined') {
			data = overallobject.answer_altered.results.main_graph;
			// data = main_graph; // even if we take the whole main_graph, there are unattached nodes, nope, there aren't, until I don't create them artificially.
		}
		data_nodes = [];
		$(data.vertices).each(function (ind, elem) {
			// push data to nodes
			tmpdatanode = {
				id: elem.uri,
				uri: elem.uri,
				title: elem.uri, // + 675,
				label: '',
				inmatchgroup: null,
				groupmatchgroups: ['base graph'],
				answerindex: null,
				groupcolor: 'gray',
				groupcolors: ['gray'],
				instanceOf: elem.instanceOf,
			};
			if (typeof(elem.json_description) != 'undefined' && typeof(elem.json_description.map) != 'undefined') { // if map is defined, it's a list
				for (oneelem in elem.json_description.map) { // we loop over the list
					if (oneelem != 'uri') {
						tmpdatanode[oneelem] = elem.json_description.map[oneelem]; // we translate the sub-entries and reattach them if they are not URI
					}
					if (oneelem.toString().substring(0, 4) == 'http') { // only for links/URI
						var lastpart = oneelem.match(tmppattern); // regex to find "name" part of URI
						tmpdatanode[oneelem] = elem.json_description.map[oneelem];
						// this one is a little over the top, should be programmed more specific
						if (lastpart[1] == 'label' || lastpart[1] == 'scientificName' || lastpart[1] == 'commonName' || lastpart[1] == 'mnemonic') {
							tmpdatanode.label = elem.json_description.map[oneelem]; // + 601;
						}
					}
				}
			}
			// if it's allready there, merge
			target_node = data_nodes.filter(function (index) {
				return index.uri == elem.uri;
			})
			if (target_node.length > 0) {
				data_nodes[target_node[0]] = tmpdatanode; // replace it
			} else {
				data_nodes.push(tmpdatanode);
			}
		});
		
		data_links = [];
		$(data.edges).each(function (ind, elem) {
			// push data to links
			tmpdatalink = {
				id: elem.source + '_' + elem.labels[0] + '_' + elem.target,
				idsolo: elem.labels[0],
				idtwostep: elem.source + '_' + elem.target,
				uri: elem.labels[0],
				source: elem.source,
				target: elem.target,
				title: elem.labels[0], // + '616',
				inmatchgroup: null,
				groupmatchgroups: ['base graph'],
				answerindex: null,
				groupcolor: 'gray',
				groupcolors: ['gray'],
				instanceOf: elem.instanceOf,
			};
			if (typeof(elem.json_description) != 'undefined' && typeof(elem.json_description.map) != 'undefined') {
				for (oneelem in elem.json_description.map) {
					if (oneelem != 'uri') {
						tmpdatalink[oneelem] = elem.json_description.map[oneelem];
					}
					// only for links/URI
					if (oneelem.toString().substring(0, 5) == '<http') {
						var lastpart = oneelem.match(tmppattern);
						tmpdatalink[oneelem] = elem.json_description.map[oneelem];
						if (lastpart[1] == 'label') {
							tmpdatalink.title = elem.json_description.map[oneelem]; // + 601;
						}
					}
				}
			}
			data_links.push(tmpdatalink);
		});
		
		if (typeof(overallobject) != 'undefined' && typeof(overallobject.answer_original) != 'undefined') { // if we already have an answer from the API, we add nodes and edges
			overallobject.answer_original.concepts.forEach(function(oneconcept, indexc) { // we loop over the concepts
				// tmpalltargets is a container to hold and manipulate all nodes and links that are found in concepts
				tmpalltargets = [];
				oneconcept.matches.forEach(function(oneanswer, indexa) { // inside the concepts, we find the targeted objects
					tmpuri = '<' + oneanswer.uri + '>'; // what would be it's uri?
					tmptitle = oneanswer.title; // + '656'; // what would be it's title

					// check if it is an already present node or link
					tmpuri_node = data_nodes.filter(onenode => onenode.id == tmpuri );
					tmpuri_link = data_links.filter(onelink => onelink.uri == tmpuri);
					
					// where would it be attached
					tmpattach = '<' + oneanswer.instanceOf + '>'; // what would be the link?
					tmpattach_node = data_nodes.filter(onenode => onenode.uri == tmpattach); // what would be the node
					// add a node if there is no node to attach to
					if (tmpattach_node.length == 0) {
						tmpattach_node = {
							id: tmpattach,
							title: tmpattach,
							label: tmpattach,
							uri: tmpattach,
							answerindex: indexa,
							inmatchgroup: indexc,
							// instanceOf: oneanswer.instanceOf,
						};
						// data_nodes.push(tmpattach_node);
						// tmpalltargets.push(tmpattach_node);
					}
					
					if (tmpattach_node.length == undefined) {
						tmpattachtitle = tmpattach_node.title;
					} else {
						tmpattachtitle = tmpattach_node[0].title;
					}
					
					if (tmpuri_node.length == 0 && tmpuri_link.length == 0) { // the thing is not present at all so we have to create it
						// we filter for properties because, they are links
						if (
							// 1 || // debug
							oneanswer.instanceOf != '<http://www.w3.org/2002/07/owl#ObjectProperty>'
							&&
							oneanswer.instanceOf != 'http://www.w3.org/2002/07/owl#ObjectProperty'
							&&
							oneanswer.instanceOf != '<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>'
							&&
							oneanswer.instanceOf != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property'
							&&
							oneanswer.instanceOf != '<http://www.w3.org/2002/07/owl#AnnotationProperty>'
							&&
							oneanswer.instanceOf != 'http://www.w3.org/2002/07/owl#AnnotationProperty'
							&&
							oneanswer.instanceOf != '<http://www.w3.org/2002/07/owl#DatatypeProperty>'
							&&
							oneanswer.instanceOf != 'http://www.w3.org/2002/07/owl#DatatypeProperty'
							) { // it isn't a link, is it?
							// create node for uri
							tmpnode = {
								id: tmpuri,
								title: tmptitle,
								label: tmptitle,
								uri: tmpuri,
								answerindex: indexa,
								inmatchgroup: indexc,
								instanceOf: oneanswer.instanceOf,
							};
							data_nodes.push(tmpnode);
							tmpalltargets.push(tmpnode);
							
							tmplinklink = { // create the link from new node to (new) attachnode
								id: oneanswer.uri + '_attachlink', // oneanswer.labels[0]
								source: tmpattach,
								target: tmpuri,
								idtwostep: tmpattach + '_' + oneanswer.uri,
								title: 'attach link for: ' + oneanswer.title + ' attached to ' + tmpattachtitle, // + ' 659', // oneanswer.labels[0],
								label: 'attacher',
								attachlink: true,
								answerindex: indexa,
								inmatchgroup: indexc,
							};
							data_links.push(tmplinklink);
							tmpalltargets.push(tmplinklink);
							
							} else { // congratulations: it's a link!
							tmplinklink = { // create the link from new node to (new) attachnode
								id: oneanswer.uri, // oneanswer.labels[0]
								source: tmpattach,
								target: tmpuri,
								title: oneanswer.title, // + '672', // oneanswer.labels[0],
								answerindex: indexa,
								inmatchgroup: indexc,
								instanceOf: oneanswer.instanceOf,
							};
							 data_links.push(tmplinklink);
							 tmpalltargets.push(tmplinklink);
						}
						
						} else if (tmpuri_node.length > 0) {
							tmpuri_node.forEach(elem => elem.answerindex = indexa);
							tmpuri_node.forEach(elem => elem.inmatchgroup = indexc);
							tmpuri_node.forEach(elem => elem.instanceOf = oneanswer.instanceOf);
						} else if (tmpuri_link.length > 0) {
							tmpuri_link.forEach(elem => elem.answerindex = indexa);
							tmpuri_link.forEach(elem => elem.inmatchgroup = indexc);
							tmpuri_link.forEach(elem => elem.instanceOf = oneanswer.instanceOf);
					}
					
					// add what was already there to the found concepts
					tmpalltargets = tmpalltargets.concat(tmpuri_node);
					tmpalltargets = tmpalltargets.concat(tmpuri_link);
					tmpalltargets = tmpalltargets.concat(tmpattach_node);
				});
				
				tmpalltargets.forEach(function (onenode, ind) {
					if (ind == 0) { // the first answer is the best and thus automatically chosen
						onenode['userchosen'] = true;
					}
					onenode['groupcolor'] = conceptscolors(conceptscolors_range_max/(overallobject.answer_original.concepts.length) * indexc);
					if (typeof(onenode['groupcolors']) == 'undefined' || onenode['groupcolors'][0] == 'gray') onenode['groupcolors'] = [];
					onenode['groupcolors'].push(conceptscolors(conceptscolors_range_max/(overallobject.answer_original.concepts.length) * indexc));
					
					if (typeof(onenode['groupmatchgroups']) == 'undefined' || onenode['groupmatchgroups'][0] == 'base graph') onenode['groupmatchgroups'] = [];
					onenode['groupmatchgroups'].push(indexc);
					
					onenode['foundconcept'] = true;
				});
			});
			
		}
		
		// remove links where at least one node is missing
		data_links = data_links.filter(function (onelink) {
			cntnodes = 0;
			tmpnodesmissing = data_nodes.filter(function (onenode) {
				if (onenode.id == onelink.target || onenode.id == onelink.source) {
					return true;
					} else {
					return false;
				}
			});
			if (tmpnodesmissing.length >= 2) {
				return true;
				} else {
				return false;
			}
		});
		
		for (keys in data_nodes) {
			// create a linearGradient for every node and define its colors
			tmpgrad = defs.append('linearGradient')
			.attrs({
				'id':'gradient_' + keys,
			});
			
			for (colors in data_nodes[keys]['groupcolors']) {
				tmpgrad.append('stop').attrs({
					'offset': parseInt(1 / data_nodes[keys]['groupcolors'].length * colors * 100) + '%',
					'stop-color': data_nodes[keys]['groupcolors'][colors]
				});
			}
		}
		
		// here we could go over to a function that only redraws the graph.
		// that can be called even after the first data load
		drawGraphFromData();
	});
}

function drawGraphFromData() {
	console.log('drawGraphFromData');
	nodes = nodegroup
	.selectAll("g")
	.data(data_nodes);
	
	nodegroup.selectAll("g").remove();
	
	nodes = nodes.enter()
	// better: http://jsfiddle.net/mdml/Q7uNz/
	/*.filter(function (d) {
		if (typeof(overallobject.answer_original) != 'undefined') {
		return d.userchosen;
		}
		return true;
	})*/
	.append("g")
	.attr('class', 'singlenode')
	.attr('data-uri', function (e) { return e.uri })
	.attr('data-inmatchgroup', function (e) { return e.inmatchgroup })
	.on( 'mouseover', function (e) {
		d3.select(this).classed('isHovered', true);
		d3.select(this).selectAll('circle').attr('stroke', 'red');
		// highlight connected nodes and path
		// what about info on subgraphs (like google nav)?
		d3.select(this).selectAll('text').style('opacity', 1);
		
		// tooltip
		tooldiv.transition()
		.duration(200)
		.style("opacity", .9);
		// tooldiv.html(d3.select(this).selectAll('text').html())
		tooldiv.html(escapeHtml(d3.select(this).selectAll('text').attr('fulltitle')))
		.style("left", 0)
		.style("top", 0);
	})
	.on( 'mouseout', function (e) {
		d3.select(this).classed('isHovered', false);
		// d3.select(this).selectAll('circle').attr('stroke', d3.select(this).attr('data-useColor'));
		d3.select(this).selectAll('circle').attr('stroke', 'white');
		d3.select(this).selectAll('text').style('opacity', 0);
		// tooltip
		tooldiv.transition()
		.duration(500)
		.style("opacity", 0);
	})
	.on('dblclick', function(d, i) {
		targetnode = d.id;
		if (targetnode != null) {
			$('.btn-group .dropdown-item[data-instanceuri="' + targetnode + '"]').click();
		}
		
	})
	.on("dblclick.zoom", null)
	.on('click', function(e) {
		// show more information info in infobox
		d3.select('#tabDetails-tab').classed('disabled', false)
		if (e.id.substring(0, 5) == '<http') {
			rawurl = e.id.slice(1,-1);
			if (rawurl.substring(0, 'http://omabrowser.org/ontology/oma#GENE_'.length) == 'http://omabrowser.org/ontology/oma#GENE_') { // omalinks should be redirected
					rawurl = rawurl.replace('http://omabrowser.org/ontology/oma#GENE_', 'https://omabrowser.org/oma/info/')
				}
		tmptext = '<a href="' + rawurl + '" target="_blank">' + rawurl + '</a>';
		} else {
			tmptext = escapeHtml(e.id);
		}
		
		
		// get and show all the info from the matches overallobject.answer_original
		// anatomical entity 0,0
		
		if (typeof(e.foundconcept) != 'undefined' && typeof(e.inmatchgroup) != 'undefined' && e.answerindex != null) {
			tmpobj = overallobject.answer_original.concepts[e.inmatchgroup].matches[e.answerindex];
			tmporderobj = {title: tmpobj.title};
			
			// show color(s) of object and explain
			var matchesstr = '';
			
			// eliminate duplicates
			var uniqcolors = e.groupcolors.filter(function (item, pos) {  
				return e.groupcolors.indexOf(item) == pos;
			});
			var uniqcolorsmap = e.groupmatchgroups.filter(function (item, pos) {  
				return e.groupmatchgroups.indexOf(item) == pos;
			});
			for (onecolor in uniqcolors) {
				if (typeof(overallobject.answer_original) != "undefined" && uniqcolorsmap[onecolor] != 'base graph') {
					var matchstr = overallobject.answer_original.concepts[uniqcolorsmap[onecolor]].concept;
					} else {
					var matchstr = 'base graph';
				}
				
				matchesstr += '<div><svg width="40" height="20" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><line stroke="' + uniqcolors[onecolor] + '" stroke-width="20" x1="1" y1="1" x2="40" y2="1" ></line></svg> Matches <em>' + matchstr + '</em></div>';
			}
			if (matchesstr.length > 0 || 1) {
				tmporderobj['matches'] = matchesstr;
			}
			
			for (key in tmpobj) {
				if (key != 'title') {
					tmporderobj[key] = tmpobj[key];
				}
			}
			for (key in tmporderobj) {
				linky = tmporderobj[key];
				if (linky.toString().substring(0, 4) == 'http') {
					linky = '<a href="' + tmpobj[key] + '" target="_blank">' + tmpobj[key] + '</a>';
				}
				tmptext += '<div data-key="' + key + '" class="row m-0 p-0"><label class="col-4 col-sm-12">' + key + ':</label><div class="entry col-8 col-sm-12">' + linky + '</div></div>';
			}
			} else {
			// main graph
			tmptext += '<div data-key="title" class="row m-0 p-0"><label class="col-4 col-sm-12">title</label><div class="entry col-8 col-sm-12">' + e.title + '</div></div>';
			for (key in e) {
				if (key.toString().substring(0, 4) == 'http') {
					var lastpart = key.match(tmppattern);
					tmptext += '<div data-key="' + key + '" class="row m-0 p-0"><label class="col-4 col-sm-12">' + lastpart[1] + ':</label><div class="entry col-8 col-sm-12">' + e[key] + '</div></div>';
				}
			}
		}
		
		// was e.label which is not everytime very userfriendly/readable
		tmptext += '<div class="row"><a onclick="pushDatatableManual({ \'column\': \'' + e.id + '\', \'label\': \'' + e.label + '\', \'columntitle\': \'' + e.label + '\', values: [], group: 0, original: true, id: \'' + e.id + '\'});" class="addremove">add to/remove from table</a></div>';
		
		// overallobject.answer_original.concepts[inmatchgroup].find(...)
		// if it is not from match, load from online source?
		d3.select('#tabDetails').html('<h2>Information</h2><p>' + tmptext + '</p>');
		$('#tabDetails-tab').click();
		
	})
	.attrs(function(d) {
		var tmpattrs = {};
		var tmpkeys = Object.keys(d);
		for (i=0; i < tmpkeys.length; i++) {
			//var tmpnewkey = "data-"+tmpkeys[i].replace('@', '').replace(':', '-');
			//var tmpnewval = d[tmpkeys[i]];
			//if (typeof(tmpnewval) == "object") continue;
			//tmpattrs[tmpnewkey] = tmpnewval;
		}
		return tmpattrs;
	})
	.call(d3.drag()
		.on("start", dragstarted)
		.on("drag", dragged)
	.on("end", dragended))
	.merge(nodes);
	
	circles = nodes
	.append("circle")
	.attr("r", function(d) {
		if (d.instanceOf == '<http://www.w3.org/2002/07/owl#Class>') {
			return 15; // 35;
			} else if (d.userchosen) {
			return 25;
			} else {
			return 15;
		}
	})
	.attr("group", function(d) { return d['group']})
	.attr("fill", function(d, i) {
		if (d.userchosen) {
			return d.groupcolor;
			} else {
			// return d3.rgb(d.groupcolor).brighter(0.5);
			return d.groupcolor;
		}
	})
	.attrs(function (d) { // check if the node has to be highlighted and which color
		var tmpattrs = { 'stroke-width': '3.5px', 'stroke': 'white', 'data-matchgroup': 'default' };
		return tmpattrs;
	})
	
	nodegroup.selectAll("text").remove();
	
	nodetexts = nodes
	.append("text")
	.text(function(d) {
		if (typeof(d.title) != 'undefined') {
			rettext = d.title;
		}
		if (typeof(d.label) != 'undefined') {
			rettext = d.label;
		}
		if (d.id == d.title == d.uri == d.label) {
			rettext = tmppattern.exec(d.id);
			rettext = rettext[1];
		}
		if (rettext.length > 20) {
			rettext = rettext.substr(0,20) + '...'; // + 917;
		}
		return rettext;
	})
	.attr('fulltitle', function(d) {
		rettext = d.title; // + 922;
		return rettext;
	})
	.attr('x', 9) // static because of size of circle
	.attr('y', 3)
	.style("pointer-events", "none") // to prevent mouseover/drag capture
	.style('opacity', 0) // hide all texts
	
	
	links = linkgroup
	.selectAll("g")
	.data(data_links)
	
	links.exit().remove();
	
	links = links.enter()
	/*.filter(function (d) {
		// look if the link is on the path of shortest links
		if ( d.shortestlink == 1 ) {
		return true;
		} else {
		return false;
		}
	})*/
	.append("g")
	.attr('class', 'singlelink')
	.attr('data-uri', function (e) { return e.uri })
	.merge(links)
	.on( 'mouseover', function (e) {
		d3.select(this).selectAll('text').style('opacity', 1);
	})
	.on( 'mouseout', function (e) {
		d3.select(this).selectAll('text').style('opacity', 0);
	})
	.on('click', function(e) {
		d3.select('#tabDetails-tab').classed('disabled', false)
		tmptext = escapeHtml(e.title);
		// tmptext = escapeHtml(e.id);
		// get and show all the info from the matches overallobject.answer_original
		if (e.title.substr(6) != 'attach') {
			if (typeof(e.foundconcept) != 'undefined' && typeof(e.inmatchgroup) != 'undefined' && e.answerindex != null) { // && typeof(e.inmatchgroup) != 'undefined' && e.answerindex != null
				tmpobj = overallobject.answer_original.concepts[e.inmatchgroup].matches[e.answerindex];
				tmporderobj = {title: tmpobj.title};
				
				var matchesstr = '';
				for (onecolor in e.groupcolors) {
					if (typeof(overallobject.answer_original) != "undefined" && e.groupmatchgroups[onecolor] != 'base graph') {
						var matchstr = overallobject.answer_original.concepts[e.groupmatchgroups[onecolor]].concept;
						} else {
						var matchstr = 'base graph';
					}
					matchesstr += '<div><svg width="40" height="20" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><line stroke="' + e.groupcolors[onecolor] + '" stroke-width="20" x1="1" y1="1" x2="40" y2="1" ></line></svg> Matches <em>' + matchstr + '</em></div>';
				}
				if (matchesstr.length > 0 || 1) {
					tmporderobj['matches'] = matchesstr;
				}
				
				for (key in tmpobj) {
					if (key != 'title') {
						tmporderobj[key] = tmpobj[key];
					}
				}
				for (key in tmporderobj) {
					linky = tmporderobj[key];
					if (linky.toString().substring(0, 5) == '<http') {
						linky = '<a href="' + tmpobj[key] + '" target="_blank">' + tmpobj[key] + '</a>';
					}
					tmptext += '<div data-key="' + key + '" class="row m-0 p-0"><label class="col-4">' + key + '</label><div class="entry col-8">' + linky + '</div></div>';
				}
				} else {
				// main graph
				for (key in e) {
					if (
						typeof(e[key]) != null
						&& typeof(e[key]) != 'undefined'
						&& typeof(e[key]) != 'object'
						&& e[key].toString().substring(0, 5) == '<http'
						) {
						var lastpart = e[key].match(tmppattern);
						tmptext += '<div data-key="' + key + '" class="row m-0 p-0"><label class="col-4">' + lastpart[1] + '</label><div class="entry col-8">' + escapeHtml(e[key]) + '</div></div>';
					}
				}
			}
		}
		
		d3.select('#tabDetails').html('<h3>Info</h3><p>' + tmptext + '</p>');
		$('#tabDetails-tab').click();
	})
	
	
	linkgroup.selectAll("line").remove();
	linkgroup.selectAll("path").remove();
	
	lines = links
	.append("line")
	.attr("stroke", function(d) {
		return 'gray'
	})
	// .attr('stroke-dasharray', '5 2')
	.attr("stroke-width", 5)
	.attr("source", function(d) {
		console.log(d.source);
		return d.source;
	})
	.attr("target", function(d) { return d.target; })
	
	linkgroup.selectAll("text").remove();
	
	linktexts = links
	.append("text")
	.text(function(d) {
		retlabel = d.title; // + '1000';
		return retlabel;
	})
	.attr('x', 9)
	.attr('y', 3)
	.style('opacity', 0)
	
	simulation.nodes(data_nodes);
	simulation.force("link").links(data_links);
	simulation.on("tick", ticked);
	simulation.alpha(1);
	simulation.on("end", function() {
		centerGraph();
		simulation.on("end", null);
	})
	if (typeof(overallobject.answer_original) != 'undefined') {
		highlightShortest(973);
	}
	// simulation.restart(); // can we move it to highlightShortest?
}

// highlight all nodes and edges along the shortest pathes between all chosen nodes
function highlightShortest(from = 'blank') {
	console.log('highlightShortest:' + from);
	// has only to be done after data load not every call of highlightShortest
	// contains information about the path between any two nodes
	allnodesmap = onpath();
	
	// we look at all the chosen links
	allinvolved = data_nodes.filter(function(d, i) {
		if (d.userchosen == true) {
			return true;
		}
		return false;
	});
	allinvolved = [];
	for (i=0; i < data_nodes.length; i++) {
		// tmpthis = data_nodes[1].id;
		// if (d3.select(circles._groups[0][i]).style('stroke') == 'rgb(255, 0, 0)') {
		if (data_nodes[i].userchosen == true) {
			allinvolved.push(i);
		}
	}
	
	// find the links which should be dashed/dotted:
	// create matrix size of involveds
	involvedcostsmatrix = []; // matrix to line up the whole paths
	involvedcosts = []; // all the links involved as links ids
	for (i=0; i < allinvolved.length; i++) {
		for (j=0; j < allinvolved.length; j++) {
			// fill with costs a->b
			tmpval = 'node'+allinvolved[i]+','+allnodesmap['node'+allinvolved[i]]['node'+allinvolved[j]];
			// it happens, that the sub note is not there so we add it as empty
			if (typeof(involvedcostsmatrix['node'+allinvolved[i]]) == 'undefined') involvedcostsmatrix['node'+allinvolved[i]] = [];
			if (i != j) {
				involvedcostsmatrix['node' + allinvolved[i]]['node' + allinvolved[j]] = tmpval;
				splinter = tmpval.split(',');
				for (k = 0; k < splinter.length-1; k++) {
					// these have to be translated to the ids of the lines
					involvedcosts.push(findnodefromnodetext(splinter[k]) + '_' + findnodefromnodetext(splinter[k+1]));
				}
			}
		}
	}
	
	for (oneshort in data_links) {
		if (involvedcosts.includes(data_links[oneshort]['idtwostep'])) {
			data_links[oneshort].shortestlink = 1;
			} else {
			data_links[oneshort].shortestlink = 0;
		}
	}
	
	// fill tabledata with chosen and shortest-path and extra
	tabledata = tabledata_manual; // reset it to manually chosen columns
	for (i=0; i < data_nodes.length; i++) {
		if (data_nodes[i].userchosen == true) {
			// data_nodes[i]["<http://www.w3.org/2000/01/rdf-schema#comment>"]
			// values should be example values to show in the table beneath
			pushDatatable({
				'column': data_nodes[i]['title'],
				'columntitle': overallobject.answer_original.concepts[data_nodes[i].inmatchgroup].concept,
				'label': data_nodes[i]['label'],
				values: [],
				group: 0,
				original: true,
			id: data_nodes[i].id },
			true
			); 
			// we are looking for suggestions and add them automatically
			if (typeof(dataJSON) != 'undefined') {
				// translate data to SPARQL variable which are stored as suggestions ordered by concepts
				targetstring = overallobject.answer_original.concepts[data_nodes[i]['inmatchgroup']].concept;
				if (typeof(dataJSON.results[0]['suggested_fields'][targetstring]) != 'undefined') {
					for (onesuggestion in dataJSON.results[0]['suggested_fields'][targetstring]) {
						// we cut off the last _var to build a human readable label
						endpattern = new RegExp('_([^_]*)$', 'g');
						secondpart = endpattern.exec(dataJSON.results[0]['suggested_fields'][targetstring][onesuggestion]);
						if (secondpart) {
							pushDatatable({ 'column': targetstring + ' | ' + secondpart[1], 'columntitle': targetstring + ' | ' + secondpart[1], 'label': '', values: [], group: 0, original: true, id: data_nodes[i]['uri'], suggestion: true}, true);
						}
					}
				}
			}
		}
	}
	loadTabledata();
}

function findnodefromnodetext(text) {
	// if (text == -1) return 'notfound'; // problematic behaviour
	if (text == -1 || text == 'undefined') text = 'node0'; // has it to do with detached nodes?
	number = text.substr(4);
	node = circles._groups[0][number];
	if (!node) node = circles._groups[0][0];
	nodeid = node.__data__.id;
	return nodeid;
}

function onpath(reset = false, maxdepth = 50, curdepth = 0, allnodesmap = {}) { // maxdepth should be null, but out of fear, I set it here ;-)
	var i, j;
	
	// init allnodesmap which contains all calculatory possible combinations node > node
	if (typeof(allnodesmap) == "undefined" || reset == true || curdepth == 0) {
		var allnodesmap = {};
		// only needed for the show
		for (i=0; i < data_nodes.length; i++) {
			// create map entry for every node
			allnodesmap['node'+i] = {};
			for (j=0; j < data_nodes.length; j++) {
				// we set it as -1 because at the start, there is nothing
				allnodesmap['node'+i]['node'+j] = -1;
				// self == 1
				// link with id a.id--b.id == 2
				if (i == j) { // node x > node x is 0
					allnodesmap['node'+i]['node'+j] = 0;
					} else if (data_links.find( // if in data_links, there is a link with these two node ids as id, we set it as the direct connection
						element => 
						element.idtwostep == data_nodes[i]['id'] + '_' + data_nodes[j]['id'] // can be node x > node y
						||
						element.idtwostep == data_nodes[j]['id'] + '_' + data_nodes[i]['id'] // or can also be node y > node x
					)) {
					allnodesmap['node'+i]['node'+j] = 'node'+j; // we set the path information to [nodex][nodey] = nodey
				}
			}
		}
	}
	// now we've got 0, direct paths and -1 in allnodesmap
	
	// from here on, we have to calculate costs that are bigger than 1 using the respective last costmap
	// 10 minutes later: no, we just have to calculate every entry that is -1 and this in an endless loop undtil al the -1 are eliminated
	
	// X  1==2==3
	// 1  0  1  -
	// 2  1  0  1
	// 3  -  1  0
	
	// X  1==2==3
	// 1  0  1  - 1==2 + 2==3 // we have to check every node in this row which is larger than one, if it has a cost to our target the other way over two steps
	// 2  1  0  1
	// 3  -  1  0
	
	// next day: instead of numbers, we can directly insert the nodes so we directly get the path if we are looking for it
	
	for (i=0; i < data_nodes.length; i++) {
		for (j=0; j < data_nodes.length; j++) {
			// only calculate those which are not already solved
			if (allnodesmap['node' + i]['node' + j] <= -1 ) { // == -1
				path = getPath(i, j, allnodesmap)
				if (path.length > 0) {
					// we can only take a path which is as long as our current step. otherwise we overcalculate earlier nodes
					for (k = 0; k < path.length; k++) {
						if (allnodesmap['node' + i]['node' + path[k]].split(',').length < curdepth && allnodesmap['node' + path[k]]['node' + j] != -1) {
							targetpath = path[k];
							if (typeof(targetpath) != 'undefined') {
								if (i == 15 && j == 14) {
									hmps = 'node' + targetpath + ',' + allnodesmap['node' + targetpath]['node' + j].toString();
								}
								allnodesmap['node' + i]['node' + j] = 'node' + targetpath + ',' + allnodesmap['node' + targetpath]['node' + j].toString();
							}
						}
					}
				}
			}
		}
	}
	
	// check if there is still -1 left or maxdepth arrived if set and reached
	// how can we handle detached nodes? could we --minusminus them?
	minusonefound = 0;
	for (i=0; i < data_nodes.length && minusonefound == 0; i++) {
		for (j=0; j < data_nodes.length && minusonefound == 0; j++) {
			if (allnodesmap['node' + i]['node' + j] < 0) {
				// allnodesmap['node' + i]['node' + j] = allnodesmap['node' + i]['node' + j] - 1; // to check only, does not run with other part of code
				minusonefound = 1;
			}
		}
	}
	if (minusonefound > 0 && (maxdepth == null || curdepth <= maxdepth)) { // curdepth <= maxdepth
		nextdepth = curdepth + 1;
		// printarraytable(allnodesmap, 'current 1291: ' + curdepth);
		onpath(false, maxdepth, nextdepth, allnodesmap); // onpath(reset = false, maxdepth = 50, curdepth = 0, allnodesmap = {})
	}
	return allnodesmap;
}

function getPath(a, b, allnodesmap) { // is there a path between these two nodes jumping over other nodes?
	// all connections from a
	conna = [];
	var i;
	for (i=0; i < data_nodes.length; i++) {
		if (allnodesmap['node'+a]['node'+i] != -1 && a != i) {
			conna.push(i)
		}
	}
	// all connections to b
	connb = [];
	for (i=0; i < data_nodes.length; i++) {
		if (allnodesmap['node'+b]['node'+i] != -1 && b != i) {
			connb.push(i)
		}
	}
	var connab = conna.filter(function(val) {
		return connb.indexOf(val) != -1;
	});
	
	connab.sort(function(r, s){
		return allnodesmap['node'+a]['node'+r].split(',').length < allnodesmap['node'+a]['node'+s].split(',').length;
	});
	
	return connab;
}

// add some filters and the videos to the api
function filterFAQ(query) {
	// add videos as FAW entries
	var videofaq = $('#tabHelp .singleq.fromvideo');
	if (videofaq.length == 0) {
		$('#carouselExampleIndicators .carousel-item').each(function() {
			var _this = $(this);
			$('#tabHelp').append('<div class="singleq fromvideo"><h4>' + _this.find('.carousel-caption').html() + '</h4><p>' + _this.find('.videodiv').html() + '</p></div>');
		});
	}
	// filter FAW (and video ;-) according to the given string - multiple words possible, delimited by space
	var filter = query.toLowerCase();
	var filterlis = filter.split(' ');
	$('#tabHelp .singleq').each(function() {
		var _this = $(this);
		var content = $(this).html().toLowerCase();
		var foundone = 0;
		filterlis.forEach(function(el) {
			if (content.indexOf(el) >= 0) foundone++;
		});
		if (foundone < filterlis.length) {
			_this.hide();
			} else {
			_this.show();
		}
	});
}

// search button gets clicked, submit query or reset it to editable
function queryButtonClick(elem = $('#searchButton')) {
	if ($(elem).html() == $(elem).attr('data-default')) {
		submitQuery();
		return true;
		} else if ($(elem).html() == $(elem).attr('data-after')) {
		$('#pseudoinput .autosize')
		.val(overallobject.query)
		.attr('disabled', false)
		.trigger('keyup');
		resetQuery();
	}
}

// reset query, hide all result things, empty data things
function resetQuery(query = overallobject.query) {
	console.log('resetQuery');
	$('#pseudoinput').empty().append($(autosizer).attr('disabled', false));
	$('#pseudoinput .autosize').val(query);
	$('#pseudoinput .autosize').trigger('keyup');
	$('#searchButton').html($('#searchButton').attr('data-default'));
	$('input.autosize').trigger('keyup');
	// $('#datatable').hide();
	// $('#downloadbutton').hide();
	// $('#tabHelp-tab').click();
	// tabledata = [];
	// tabledata_manual = [];
	// overallobject = [];
	// svginit(); // let's leave the old results there
}

// submit the query to the api and receive results (first api call doesn't contain concepts)
function submitQuery() {
	console.log('submitQuery');
	$('#searchButton').html('<div class="spinner-border spinner-border-sm" role="status" style="color: #26348B;"><span class="sr-only">Loading...</span></div>').blur();
	$('#interactapi').show();
	$('#datatable').hide();
	$('#downloadbutton').hide();
	// init tabledata with standard values
	tabledata = tmptabledata;
	
	// prepare query
	overallobject.query = $('#pseudoinput .autosize').val();
	
	$.ajax({
		// url: "http://localhost:3003/",
		// url: "http://biosoda.expasy.org:3003/",
		// url: "http://biosoda.cloudlab.zhaw.ch:8082/soda/api/",
		url: "https://biosoda.expasy.org:4481/soda/api/", // CORS missing?
		data: {q: overallobject.query},
		method: 'GET'
		}).fail(function() {
			// what do we do with an error?
			$('#searchButton').html($('#searchButton').attr('data-default'));
			alert('problems receiving response, please try again');
		}).done(function(data) {
		// clone object and fill with data value
		answer = data;
		// we keep the original answer as reference
		overallobject.answer_original = JSON.parse(JSON.stringify(answer));
		// from now on, everything we want to change, we do on the answer_altered
		overallobject.answer_altered = JSON.parse(JSON.stringify(answer));
		workOnData();
	})
}

// after submitting and regetting the data, we can work on it
function workOnData() {
	console.log('workOnData');
	// do some changes on the ui to let the user know what happens
	$('#d3graph').show();
	$('#searchButton').html($('#searchButton').attr('data-after'));
	$('#pseudoinput').empty();
	
	// construct superextended html question
	pseudoquery = overallobject.answer_original.question;
	
	// what do we want to color how?
	howtocolor = [];
	howtocolor.index = [];
	howtocolor.color = [];
	
	svginit();
	
	tmppseudo_array = {};
	
	$(overallobject.answer_original.concepts).each(function(index) {
		allinner = '';
		addactive = ' active';
		tmpcolor = conceptscolors(conceptscolors_range_max/(overallobject.answer_original.concepts.length) * index);
		overallobject.answer_altered.concepts[index].useColor = tmpcolor;
		
		// per default we chose the first answer as chosen by the user
		overallobject.answer_altered.concepts[index].matches.map(elem => elem.foundconcept = true );
		
		$(this.matches).each(function(match) {
			instancetext = '';
			tmpClass = this.instanceOf.match(tmppattern);
			tmpInstance = this.uri.match(tmppattern);
			if (tmpClass == null) {
				tmpClass = 'undefined';
			}
			// create nice(r) human readable label text
			instancetext = tmpClass[1] + ':' + tmpInstance[1];
			allinner += '<a class="dropdown-item' + addactive + '" href="#" data-instancetext="' + instancetext + '" data-instanceclass="' + tmpClass[1] + '" data-instanceuri="<' + this.uri + '>" data-concept="' + index + '" data-match="' + match + '">' + this.title + ' (' + instancetext + ')</a>';
			addactive = '';
			// we need to know which concept we want to color how
			if (match == 0) {
				howtocolor.index[index] = instancetext;
				howtocolor.index[index+overallobject.answer_original.concepts.length] = 'Class:' + tmpClass[1];
				howtocolor.color[index] = tmpcolor;
				howtocolor.color[index+overallobject.answer_original.concepts.length] = tmpcolor;
			}
		});
		targetword = this.concept_original;
		tmppseudo_array[targetword] = '<div class="btn-group" data-replacetext="' + targetword + '" data-conceptindex="' + index + '"><button type="button" class="btn btn-success btn-title" title="' + instancetext + '" style="background-color: ' + tmpcolor + '; border-color: ' + tmpcolor + ';">' + targetword + '</button><button type="button" class="btn btn-success dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="background-color: ' + tmpcolor + '; border-color: ' + tmpcolor + ';"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu">' + allinner + '<div class="dropdown-divider"></div><a class="dropdown-item" href="#" data-concept="' + index + '">Don\'t include in query</a></div></div>';
		pseudoquery = pseudoquery.split(targetword).join('<dummy>' + targetword + '</dummy>');
	});
	
	findstringpattern = new RegExp(/(<[^>]*>([^<]*)<\/[^<]*>|\w+(?: \w+)+)/gm);
	findstringpattern = new RegExp(/(<[^>]*>([^<]*)<\/[^<]*>|[a-zA-Z0-9_]+)/gm);
	pseudoquery_html = '';
	lasttag = '';
	
	while(null != (matches=findstringpattern.exec(pseudoquery))) {
		if (matches[2] == undefined) { // if this is an untagged entry (not conceptionaly recognized)
			pseudoquery_html += '<input name="tags[]" value="' + matches[1] + '" class="autosize" disabled="disabled">';
			lasttag = 'text';
			} else {
			if (lasttag != 'text') {
				pseudoquery_html += autosizer;
			}
			pseudoquery_html += matches[1];
			lasttag = 'dropdown';
		}
	}
	$.each(tmppseudo_array, function(oneindex, onepseudo) {
		pseudoquery_html = pseudoquery_html.split('<dummy>' + oneindex + '</dummy>').join(onepseudo);
	});
	$('#pseudoinput').html('' + pseudoquery_html + '' + autosizer);
	$('input.autosize').trigger('keyup');
	
	$('#pseudoinput, table#example')
	.on('mouseover', '[data-conceptindex], [data-conceptindex] .dropdown-item', function(e) {
		// are we hovering the button or the single entry?
		if ($(e.target).is('.btn')) {
			// find userchosen concept (concept number and chosen number)
			// if empty, find all concepts?
			// we should attach the result of the query to the button somehow - do we?
			tmptargetconceptindex = $(this).attr('data-conceptindex');
			// tmptargetconcepts = overallobject.answer_altered.concepts[tmptargetconceptindex]
			
			// only center if concept is chosen (eventually we should center over all of the subconcepts)
			// scenterNode(tmpindex);
			
			nodes.nodes().map(function (d,i) {
				if (typeof(d.attributes['data-inmatchgroup']) != 'undefined' && d.attributes['data-inmatchgroup'].value == tmptargetconceptindex) {
					d.dispatchEvent(new MouseEvent("mouseover"));
				}
			});
			} else {
			tmptargeturi = $(this).attr('data-instanceuri');
			nodes.nodes().map(function (d,i) {
				if (typeof(d.attributes['data-uri']) != 'undefined' && d.attributes['data-uri'].value == tmptargeturi) {
					d.dispatchEvent(new MouseEvent("mouseover"));
				}
			});
		}
	})
	.on('mouseout', '[data-conceptindex]', function(e) {
		targetnodes = nodes.nodes().map(function (d,i) {
			d.dispatchEvent(new MouseEvent("mouseout"));
		});
	});
	
	$('#pseudoinput').on('click', '.dropdown-item', function(e) {
		$(this).parents('.btn-group').find('.active').removeClass('active');
		tmpthisconcept = $(this).attr('data-concept');
		tmpthismatch = $(this).attr('data-match');
		data_nodes.map(function(cur, ind, arr) {
			if (cur.inmatchgroup == tmpthisconcept) {
				cur.userchosen = false;
			}
		});
		
		data_links.map(function(cur, ind, arr) {
			if (cur.inmatchgroup == tmpthisconcept) {
				cur.userchosen = false;
			}
		});
		
		// since we will have all the other spanning graph concepts also, I'll have to mark it somehow but now overwrite it
		if (typeof($(this).attr('data-instancetext')) !== "undefined") { // user chooses this concept
			$(e.target).addClass('active');
			$(this).parents('.btn-group').find('.btn-title').attr('title', $(this).attr('data-instancetext'));
			// overallobject.answer_altered.concepts[tmpthisconcept].matches[tmpthismatch].userchosen = true;
			// find the node which is targeted by this
			tmptargeturi = '<' + overallobject.answer_original.concepts[tmpthisconcept].matches[tmpthismatch].uri + '>';
			tmpchosentarget = data_nodes.map(function(cur, ind, arr) {
				if (cur.id == tmptargeturi) {
					cur.userchosen = true;
				}
			});
			
			tmpchosentarget = data_links.map(function(cur, ind, arr) {
				if (cur.uri == tmptargeturi) {
					cur.userchosen = true;
				}
			});
			
			howtocolor.index[$(this).attr('data-concept')] = $(this).attr('data-instancetext');
			// howtocolor.color[$(this).attr('data-concept')] = tmpcolor;
			$(this).parents('.btn-group').find('button').css({'background-color': howtocolor.color[$(this).attr('data-concept')], 'border-color': howtocolor.color[$(this).attr('data-concept')]});
			overallobject.answer_original.concepts[$(this).attr('data-concept')].userdeleted = false;
			} else { // user removes this concept from results
			// $(this).parents('.btn-group').replaceWith($(this).parents('.btn-group').attr('data-replacetext'));
			overallobject.answer_original.concepts[$(this).attr('data-concept')].userdeleted = true;
			$(this).parents('.btn-group').find('button').css({'background-color': 'rgb(200, 200, 200)', 'border-color': 'rgb(200, 200, 200)'});
		}
		ticked();
		highlightShortest(1437);
		e.preventDefault();
	});
	$('#datatable').show();
	// highlightShortest(1442); // we cannot do it until we have some chosen nodes
}

// finally download the CSV
function loadFinalData() {
	$.ajax({
		url: "https://biosoda.expasy.org:4481/soda/api/",
		data: JSON.stringify({
			sparql_query: dataJSON.results[0].query,
		}),
		method: 'POST',
		success: function (data) {
			window.open("data:text/csv;charset=utf-8," + escape(data));
		}
	})
}

function loadTabledata() {
	console.log('loadTabledata');
	$('#datatable .loader').show();
	
	// fetch data to be overgiven to the api
	chosenconcepts = [];
	data_nodes.forEach(function (v, i) {
		if (typeof(v.userchosen) != "undefined" && v.userchosen == true) {
			tmpconcept = JSON.parse(JSON.stringify(overallobject.answer_original.concepts[v.inmatchgroup]));
			tmpconcept.matches = [];
			tmpmatch = overallobject.answer_original.concepts[v.inmatchgroup].matches[v.answerindex];
			tmpconcept.matches.push(tmpmatch);
			chosenconcepts.push(tmpconcept);
		}
	});
	data_links.forEach(function (v, i) {
		if (typeof(v.userchosen) != "undefined" && v.userchosen == true) {
			tmpconcept = JSON.parse(JSON.stringify(overallobject.answer_original.concepts[v.inmatchgroup]));
			tmpconcept.matches = [];
			tmpmatch = overallobject.answer_original.concepts[v.inmatchgroup].matches[v.answerindex];
			tmpconcept.matches.push(tmpmatch);
			chosenconcepts.push(tmpconcept);
		}
	});
	
	APIcolumns = [];
	APIcolumns_only = [];
	$(tabledata).each(function (index) {
		tmpcolumn = {
			column: tabledata[index].column,
			id: tabledata[index].id
		}
		APIcolumns.push(tmpcolumn);
		if (!chosenconcepts.find(function (elem) {
			if (typeof(elem.matches[0]) != 'undefined') {
				if ('<' + elem.matches[0].uri + '>' == tmpcolumn.id) {
					return true;
				}
			}
			return false;
		})) {
		APIcolumns_only.push(tmpcolumn);
		}
	});
	
	$.ajax({
		url: "https://biosoda.expasy.org:4481/soda/api/",
		data: JSON.stringify({
			q: overallobject.query,
			concepts: chosenconcepts,
			columns: APIcolumns,
			columns_only: APIcolumns_only
		}),
		method: 'POST',
		success: function (data) {
			console.log('data back');
			answerdata = data;
			overallobject.answer_altered = data;
			fillData(); // before (while pushing), we waited for the data to be fully filled in, now we're ready to get the answers
			$('#datatable .loader').hide();
		}
	});
}

function showTabledata() {
	console.log('showTabledata');
	$('#example tbody').empty();
	// we store the data in an object
	// $("#API .progress").remove();
	if (typeof(answerdata) != 'undefined') {
		dataJSON = answerdata;
	}
	dataresults = dataJSON.results[0].concepts;
	// we use the translate table to translate the table (what a surprise :-)
	$(dataresults).each(function(e,i) { // a row for each result
		var row = document.createElement("tr");
		$('#datatable th').each(function (edat, idat) { // get correct column data value
			// suggested columns are marked with a pipe e.g. "anatomic entities | label"
			thisdat = idat.attributes['data-columntitle'].value;
			targetdata = i[0]; // just to have a default targetdata
			pipecut = thisdat.split('|');
			// if there is a pipe
			if (pipecut.length > 1) {
				firstdat = pipecut[0].trim();
				targetsecond = pipecut[1].trim();
				secondpattern = new RegExp('_' + targetsecond + '$');
				if (dataJSON.results[0]['suggested_fields'][firstdat]) {
					targetdata = dataJSON.results[0]['suggested_fields'][firstdat].find(
						elem => elem.search(secondpattern) > 0
					);
				}
				// targetdata = dataJSON.results[0]['suggested_fields'][firstdat][seconddat];
				} else {
				targetdata = dataJSON.results[0]['translation_table_concepts'][thisdat];
			}
			// create new td object
			var col = document.createElement("td");
			// insert target data
			var targetval = i[targetdata];
			
			// fallback: open it empty
			if (typeof(targetval) == 'undefined') targetval = '';
			if (targetval.substring(0, 4) == 'http') {
				if (targetval.substring(0, 'http://omabrowser.org/ontology/oma#GENE_'.length) == 'http://omabrowser.org/ontology/oma#GENE_') { // omalinks should be redirected
					targetval = targetval.replace('http://omabrowser.org/ontology/oma#GENE_', 'https://omabrowser.org/oma/info/')
				}
				targetval = '<a href="' + targetval + '" target="_blank">' + targetval + '</a>';
			}
			col.innerHTML = targetval;
			row.appendChild(col);
		});
		$('#example tbody').append(row);
	});
	console.log('done display - build table');
	
	if (dataresults.length == 0) {
		console.log('done display - no results');
		var row = document.createElement("tr");
		var col = document.createElement("td");
		var lin = document.createElement("a");
		lin.innerHTML = 'no data loaded - try to reload';
		lin.setAttribute('onclick', '$(this).append(\'<span>...</span>\');loadTabledata();');
		lin.setAttribute('style', 'cursor: pointer;');
		col.appendChild(lin);
		row.appendChild(col);
		$('#example').append(row);
	} else {
		$('#downloadbutton').show();
	}
}

function showSPARQL(secondrun = false) {
	if (secondrun == false && typeof(dataJSON) == false) {
		window.setTimeout(1000, showSPARQL(true));
	} else {
		d3.select('#tabDetails-tab').classed('disabled', false)
		tmptext = escapeHtml(dataJSON.results[0].query);
		d3.select('#tabDetails').html('<h2>SPARQL query of loaded data</h2><p>' + tmptext + '</p>');
		$('#tabDetails-tab').click();
	}
}

$(document).ready(function() {
	console.log('document.ready');
	
	// allow datatable to be double scrolled
	$('.double-scroll').doubleScroll();
	
	// init the svg and prepare it to display the nodes and edges and stuff
	svginit();
	
	// add some filters and the videos to the api
	filterFAQ('');
	
	// init the video carousel
	// https://getbootstrap.com/docs/4.0/components/carousel/
	// https://datatables.net/reference/option/
	$('.carousel').carousel({
		interval: 0,
		}).on('slide.bs.carousel', function () {
		$('#demo video').each(function (vid) {
			this.pause();
		});
	});
	
	$('#demo').hide();
	$('#interactapi').hide();
	
	// input boxes should autosize
	$('#boxsearch').on( 'keyup change input', 'input.autosize', function (e) {
		$("#boundingbox").html($(this).val());
		$(this).width($("#boundingbox").width()+8);
		// last has to fill the line
		tmplast = $(this).parent().find('input:last-child');
		tmplast = tmplast[0];
		$("#boundingbox").html($(tmplast).val());
		tmpwidth = $("#boundingbox").width();
		tmpparentwidth = $(tmplast).parent().width();
		$(tmplast).width(0);
		tmpoffset = tmplast.offsetLeft;
		if (tmpparentwidth < (tmpoffset + tmpwidth)) {
			newwidth = '100%';
			} else {
			newwidth = tmpparentwidth-tmpoffset-20;
		}
		$(tmplast).width(newwidth);
	});
	$('input.autosize').trigger('keyup');
	
	$('#datatable').hide();
	$('#downloadbutton').hide();
	
	tabledata = [];
	tabledata_manual = [];
	tmptabledata = [];
	
	$('.searchexamples a[data-search]').on('click', function(e) {
		e.preventDefault();
		resetQuery($(this).attr('data-search'));
	});
});