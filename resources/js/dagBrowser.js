/**
 * The DAG-Browser displays the DAG built by the SODA-Backend and
 * lets the user interactively explore it.
 *
 * @module dagBrowser
 */
/**
 * The DAG-Browser displays the DAG built by the SODA-Backend and
 * lets the user interactively explore it.
 *
 * @module dagBrowser
 */

 /**
 * DagBrowser is a Closure and returns the function PreparedDagBrwoser from within.
 *
 * @class DagBrowser
 * @constructor
 */
DagBrowser = function() {
	
    var dagBrowserConfig = new DagBrowserConfig();
    var dagBrowserModel = null;

    var nodesG = null;			//define placeholder for group element of nodes
    var linksG = null;			//define placeholder for group elements of links
    var labelG = null;
    
    var nodeDomElements = null;	//define placeholder for array of nodes inserted into DOM
    var linkDomElements = null;	//define placeholder for array of links inserted into DOM
    var labelDomElements = null;
    
    var force = d3.layout.force();	//define the D3 force layout

    var scout = null;
	/**
	 * The function PreparedDagBrowser selects an element inside the DOM and 
	 *
	 * @private
	 * @method PreparedDagBrowser
	 * @param selection {String}  A string which contains a CSS selctor.
	 * @param data {Object} A object containing the raw data with which the DagBrowser is initialized. Mostly loaded directly from a JSON file.
	 */
    var PreparedDagBrowser = function(selection, data) {
        dagBrowserModel = new DagBrowserModel(data);
        enrichDagBrowserData(dagBrowserModel);

        var svg = d3.select(selection)
            .append("svg:svg")
            .attr("width", dagBrowserConfig.getCanvasWidth())
            .attr("height", dagBrowserConfig.getCanvasHeight());
        linksG = svg.append("svg:g").attr("id", "links");
        nodesG = svg.append("svg:g").attr("id", "nodes");
        labelG = svg.append("svg:g").attr("id", "label");
        force.size([dagBrowserConfig.getCanvasWidth(), dagBrowserConfig.getCanvasHeight()])
            .on("tick", forceTick)
            .charge(-200)
            .linkDistance(dagBrowserConfig.linkDistance);

        svg.append('svg:defs').append('svg:marker')
            .attr('id', 'end-arrow')
            .attr('viewBox', '0 -5 10 10')
            .attr('refX', 6)
            .attr('markerWidth', 3)
            .attr('markerHeight', 3)
            .attr('orient', 'auto')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5')
            .attr('fill', 'steelblue');

		updateGraph();
		force.start();
    };

	/**
	 * Inserts all nodes and links into the DOM.
	 *
	 * @private
	 * @method updateGraph
	 */
    var updateGraph = function() {
        //TODO: filter nodes and links
        dagBrowserModel.currentNodes = dagBrowserModel.getNodes();
        dagBrowserModel.currentLinks = dagBrowserModel.getLinks();

        force.nodes(dagBrowserModel.currentNodes);
        updateNodes(dagBrowserModel.currentNodes);

        updateLabels(dagBrowserModel.currentNodes);

        force.links(dagBrowserModel.currentLinks);
        updateLinks(dagBrowserModel.currentLinks);
    };
	
	/**
	 * Enriches the data in the DagBrowserModel with information about how to display them.
	 *
	 * @private
	 * @method enrichDagBrowserData
	 */	
    var enrichDagBrowserData = function(dagBrowserModel) {
    	//Set starting coordinates of all nodes
        var nodes = dagBrowserModel.getNodes();
        nodes.forEach(function(n) {
            var randomnumber;
            n.x = randomnumber = Math.floor(Math.random() * dagBrowserConfig.getCanvasWidth());
            n.y = randomnumber = Math.floor(Math.random() * dagBrowserConfig.getCanvasHeight());
            return n.radius = dagBrowserConfig.getCircleRadius();
        });

        //Set Scout
        scout = nodes[1];
    };

	/**
	 * Performs the data binding for all nodes inside the data,
	 * creates a SVG circle element for each, sets the style and coordinates and
	 * appends it to the DOM.
	 * @private
	 * @method updateNodes
	 */
    var updateNodes = function(nodes) {
        nodeDomElements = nodesG.selectAll("circle.node").data(nodes, function(n){
            return n.id;
        });

        nodeDomElements.enter()
            .append("circle")
            .attr("class", "node")
            .attr("cx", function(d) {
            	return d.x;
            }).attr("cy", function(d) {
                return d.y;
            }).attr("r", function(d) {
                return d.radius;
            }).text(function(d){
                return d.caption;
            });

        nodeDomElements.classed("domainOntologyNode", function(d){return d.typeId===2})
            .classed("conceptualSchemaNode", function(d){return d.typeId===3})
            .classed("logicalSchemaNode", function(d){return d.typeId===4})
            .classed("baseDataNode", function(d){return d.typeId===5});


        nodeDomElements.on("click", setAsScoutEvent)
            .on("mouseover", mouseoverNode)
            .on("mouseout", mouseoutNode)
            .call(force.drag);
    };

    var setAsScoutEvent = function(clickedD){
        //set scoutnode and its style
        nodeDomElements.classed("scout", function(d){
            return d === clickedD;
        });

        scout = clickedD;
        resetLinksAndNodesStyle();
        styleLinksAndNodesAroundNode(clickedD.id);
    };

    var resetLinksAndNodesStyle = function(){
        nodeDomElements.classed("dp1node", false);
        nodeDomElements.classed("dp2node", false);
        nodeDomElements.classed("dp3node", false);
        nodeDomElements.classed("dm1node", false);
        nodeDomElements.classed("dm2node", false);
        linkDomElements.classed("dp0link", false);
        linkDomElements.classed("dp1link", false);
        linkDomElements.classed("dp2link", false);
        linkDomElements.classed("dm0link", false);
        linkDomElements.classed("dm1link", false);
    };

    /**
     * This function recursively styles the links and nodes around the scout.
     * @private
     * @method styleLinksAndNodesAroundNode
     * @param nodeId {Number} The id of the scout node
     */
    var styleLinksAndNodesAroundNode = function(nodeId){
        styleLevel(nodeId, 0, true);
        styleLevel(nodeId, 0, false);
    };

    /**
     * This function recursively styles the links and nodes in or against the direction of the DAG.
     * For more information read the projects documentation. TODO: Verweis auf entsprechenden Absatz
     * @private
     * @method styleLinksAndNodesAroundNode
     * @param nodeId {Number} The id of the current node. Attached links and nodes at the other end according the direction are being styled.
     * @param distance {Number} Distance from the scout.
     * @param isDirectionPositive {Number} Outgoing links are being selected for styling if true. Incoming if false.
     */
    var styleLevel = function(nodeId, distance, isDirectionPositive){
        var nextLvlLinks, nextLvlNodes;

        //get next level nodes and links
        if(isDirectionPositive){
            nextLvlLinks = dagBrowserModel.getLinksFilteredBySourceId(nodeId);
            nextLvlNodes = nextLvlLinks.map(function(d){
                return d.target;
            });
        }
        else{
            nextLvlLinks = dagBrowserModel.getLinksFilteredByTargetId(nodeId);
            nextLvlNodes = nextLvlLinks.map(function(d){
                return d.source;
            })
        }

        //get next level node and link elements
        var myNodeElements = nodesG.selectAll("circle.node").data(nextLvlNodes, function(n){
            return n.id;
        });
        var myLinkElements = linksG.selectAll("path.link").data(nextLvlLinks, function(l){
            return l.id;
        });

        //format next level links and target node
        if(distance == 0){
            if(isDirectionPositive){
                myLinkElements.classed("dp0link", true);
                myNodeElements.classed("dp1node", true);
            }
            else{
                myLinkElements.classed("dm0link", true);
                myNodeElements.classed("dm1node", true);
            }
        }
        else if(distance == 1){
            if(isDirectionPositive){
                myLinkElements.classed("dp1link", true);
                myNodeElements.classed("dp2node", true);
            }
            else{
                myLinkElements.classed("dm1link", true);
                myNodeElements.classed("dm2node", true);
            }
        }
        else if(distance == 2){
            if(isDirectionPositive){
                myLinkElements.classed("dp2link", true);
                myNodeElements.classed("dp3node", true);
            }
            else{
                myLinkElements.classed("dm1link", true);
                myNodeElements.classed("dm2node", true);
            }
        }

        //style next level
        if(!distance>2){
            nextLvlNodes.forEach(function(d){
                styleLevel(d.id, distance+1, isDirectionPositive);
            });
        }
    };

	/**
	 * Performs the data binding for all links inside the data,
	 * creates a SVG path element for each, sets the style and coordinates and
	 * appends it to the DOM.
	 * @private
	 * @method updateLinks
	 */
    var updateLinks = function(links) {
        linkDomElements = linksG.selectAll("path.link").data(links, function(l){
            return l.id;
        });

        linkDomElements.enter()
            .append("path")
            .attr("class", "link")
            .style('marker-end', function(d) { return 'url(#end-arrow)'; });
    };

    var updateLabels = function(nodes){
        labelDomElements = labelG.selectAll("g").data(nodes);

        labelDomElements.enter().append("svg:g");

        labelDomElements.append("svg:text")
            .attr("x", 0)
            .attr("y", ".51em")
            .attr("class", "shadow")
            .text(function (d) {
                return d.caption;
            });

        labelDomElements.append("svg:text")
            .attr("x", 0)
            .attr("y", ".51em")
            .text(function (d) {
                return d.caption;
            });

        labelDomElements.on("mouseover", mouseoverNode)
            .on("mouseout", mouseoutNode);
    };

	/**
	 * Sets the new coordinates of all nodes and links.
	 *
	 * @private
     * @event
	 * @method forceTick
	 */
    var forceTick = function(e) {
        nodeDomElements.attr("cx", function(d) {
	            return d.x;
	        }).attr("cy", function(d) {
	        	return d.y;
			}
		);

        linkDomElements.attr('d', function(d) {
            var deltaX = d.target.x - d.source.x,
                deltaY = d.target.y - d.source.y,
                dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY),
                normX = deltaX / dist,
                normY = deltaY / dist,
                sourcePadding = dagBrowserConfig.getCircleRadius()+ 5,
                targetPadding = dagBrowserConfig.getCircleRadius()+ 5,
                sourceX = d.source.x + (sourcePadding * normX),
                sourceY = d.source.y + (sourcePadding * normY),
                targetX = d.target.x - (targetPadding * normX),
                targetY = d.target.y - (targetPadding * normY);
            return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
        });

        labelDomElements.attr("transform", function (d) {
            return "translate(" + (d.x - dagBrowserConfig.getCircleRadius() + 10) + "," + d.y + ")";
        });
    };

    var mouseoverNode = function(n){
        resetLinksAndNodesStyle();
        /*n = new Array(n);
        var selectedNode = nodeDomElements.data(n, function(n){
            return n.id;
        });
        selectedNode.classed("highlightedNode", true);*/

        styleLinksAndNodesAroundNode(n.id);
    };

    var mouseoutNode = function(n){
        resetLinksAndNodesStyle();
        /*n = new Array(n);
        var selectedNode = nodeDomElements.data(n, function(n){
            return n.id;
        });
        selectedNode.classed("highlightedNode", false);*/

        styleLinksAndNodesAroundNode(scout.id);
    };

	return PreparedDagBrowser;
};

/**
 * Contains configuration information on how the DagBrowser is being displayed.
 *
 * @class DagBrowserConfig
 * @constructor
 */
var DagBrowserConfig = function(){

    /**
     * Define width of canvas
     *
     * @private
     * @property canvasWidth
     * @type Number
     * @default 400
     */
    var canvasWidth = 1400;

    /**
     * Returns the width of the canvas.
     *
     * @method getCanvasWidth
     * @return {Number} width of the canvas
     */
    this.getCanvasWidth = function(){ return canvasWidth; };

    /**
     * Define height of canvas
     *
     * @private
     * @property height
     * @type canvasHeight
     * @default 400
     */
    var canvasHeight = 1000;

    /**
     * Returns the height of the canvas.
     *
     * @method getCanvasHeight
     * @return {Number} height of the canvas
     */
    this.getCanvasHeight = function(){ return canvasHeight; };

    /**
     * Define radius of nodes
     *
     * @private
     * @property circleRadius
     * @type Number
     * @default 10
     */
    var circleRadius = 40;

    /**
     * Returns the radius of a node.
     *
     * @method getCircleRadius
     * @return {Number} radius
     */
    this.getCircleRadius = function(){ return circleRadius; };

    /**
     * The scout is an object of type NodeDatum.
     *
     * @private
     * @property scout
     * @type NodeDatum
     * @default null
     */

    this.linkDistance = 200;

    this.scout = null;
    this.scoutSightForward = 2;
    this.scoutSightBackward = 1;
};


/**
 * Contains all data to be displayed by DagBrowser.
 * Provides various helper functions to access, filter and manipulate the data.
 *
 * @class DagBrowserModel
 * @constructor
 */
var DagBrowserModel = function(json){
    var types = d3.map();
    var nodes = d3.map();
    var links = new Array();

    this.currentNodes = null;
    this.currentLinks = null;

    /**
     * Processes the data from JSON and prepares DagBrowserModel for use.
     * @private
     * @method processJsonData
     * @param json {Object} Contains two properties: property "nodes" with an array of node data and a property "links" with an array of link data
     */
    var processJsonData = function(json) {
        if (!json instanceof Object)
            throw "Error in DagBrowser: json cannot be parsed.";

        types = processJsonTypesData(json.types);
        nodes = processJsonNodesData(json.nodes, types);
        links = processJsonLinksData(json.links);
    };

    var processJsonTypesData = function(jsonTypes) {
        var typesMap = d3.map();
        if (!jsonTypes instanceof Object || !jsonTypes.length)
            throw "Error in DagBrowser: no types defined.";

        jsonTypes.forEach(function (t) {
            if ( isValid(t.id) && isValid(t.name)){
                return typesMap.set(t.id, t);
            }
            else throw "Error in DagBrowser: property is missing, type not valid";
        });
        return typesMap;
    };

    /**
     * Validates the jsonNodes and crates a map with the node's id as key and the object as value.
     * jsonNodes is valid if there is at least one node and all nodes contain all needed properties.
     * @private
     * @method processJsonNodesData
     * @param jsonNodes {Array} Contains node data objects
     * @return {d3.map} Returns a map with the node id as key and the node object as value
     */
    var processJsonNodesData = function(jsonNodes, typesMap){
        var nodesMap = d3.map();
        if (!jsonNodes instanceof Object || !jsonNodes.length || !typesMap instanceof Object)
            throw "Error in DagBrowser: arguments not valid.";

        jsonNodes.forEach(function (n) {
            if ( isValid(n.id) && isValid(n.caption) && isValid(n.typeId)){
                if(n.typeId!==2 && n.typeId!==3 && n.typeId!==4 && n.typeId!==5){
                    console.log(n.caption)
                }
                    return nodesMap.set(n.id, n);
            }
            else throw "Error in DagBrowser: property is missing, node not valid";
        });
        return nodesMap;
    };

    /**
     * Validates the jsonLinks and crates a new array with them.
     * Exchanges the value of the links source and target property which are id's with the node objects themselves.
     * jsonLinks is valid if there is at least one link and all links contain all needed properties.
     * @private
     * @method processJsonLinksData
     * @param jsonLinks {Array} Contains link data objects
     * @return {Array} Returns an array of links
     */
    var processJsonLinksData = function(jsonLinks){
        var linksArray = new Array();
        if (!jsonLinks instanceof Object || !jsonLinks.length)
            throw "Error in DagBrowser: no links defined.";
        //fill up links array
        jsonLinks.forEach(function (l) {
            if (isValid(l.source) && isValid(l.target) && isValid(l.id)){
                //Set a reference to the source and target node inside each link
                l.source = nodes.get(l.source);
                l.target = nodes.get(l.target);

                linksArray.push(l);
            }
            else throw "Error in DagBrowser: no nodes defined.";
        });
        return linksArray;
    };

    /**
     * Returns an array with all link with a source node with the given sourceId
     * @method getLinksFilteredBySourceId
     * @param sourceId {Number} The unique id of a node
     * @return {Array} Returns a new array with the filtered links
     */
    this.getLinksFilteredBySourceId = function(sourceId){
        var filteredLinks;
        filteredLinks = links.filter(function(l){
            return l.source.id === sourceId;
        });
        return filteredLinks
    };

    /**
     * Returns an array with all link with a target object with the given targetId
     * @method getLinksFilteredByTargetId
     * @param targetId {Number} The unique id of a node
     * @return {Array} Returns a new array with the filtered links
     */
    this.getLinksFilteredByTargetId = function(targetId){
        var filteredLinks;
        filteredLinks = links.filter(function(l){
            return l.target.id === targetId;
        });
        return filteredLinks
    };

    /**
     * Returns an array with all nodes
     * @method getNodes
     * @return {Array} Returns a new array of all node objects
     */
    this.getNodes = function(){
        return nodes.values();
    };

    this.getNode = function(nodeId){
        if (isValid(nodeId)) return nodes.get(nodeId);
        else throw "DagBrowserModel.getNode: Argument is not valid";
    }

    /**
     * Returns an array with all links
     * @method getLinks
     * @return {Array} Returns a new array of all link objects
     */
    this.getLinks = function(){
        return links.filter(function(l){ return true; });
    };



    processJsonData(json);
};

    var isValid = function(propertyValue){
        return !(typeof propertyValue  == 'undefined' || propertyValue==null);
    }

DagBrowser.display = function(jsonData){
    var myDagBrowser;
    myDagBrowser = DagBrowser();

    if(typeof jsonData  == 'undefined' || jsonData==null){
        return d3.json("data/extractedData.json", function(json) {
            return myDagBrowser("#dagBrowser", json);
        });
    }
    else{
        return myDagBrowser("#dagBrowser", jsonData);
    }

};

function sendAjax() {
	 
    $.ajax({
        url: "http://localhost:8081/soda/json/?resource=sideBar&sodaQuery=sara",
        type: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        mimeType: 'application/json',
 
        success: function (data) {
        	$("#sidebar").append(data);
            console.log("JSON: " + data); 
        },
        error:function(data,status,er) {
            alert("error: "+data+" status: "+status+" er:"+er);
        }
    });
}


