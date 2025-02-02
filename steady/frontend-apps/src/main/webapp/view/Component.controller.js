/*
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
var groupId = "";
var artifactId = "";
var versionId = "";

var oVulnModel = null;
var oWHistModel = null;
var ajaxQueue = [];

sap.ui.controller("view.Component", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
		// Add event delegate
		// This is commented out has it overrides the standard definition of the event.
//		this.getView().byId("idPatchAnalysisList").addEventDelegate({
//			onAfterRendering:function() {
//              console.log("focus in");
//            }
//          },this);
	},

	_handleRouteMatched : function(evt) {
		if (evt.getParameter("name") !== "component") {
            return;
        }
		if (groupId!=evt.getParameter("arguments").group ||
				artifactId!=evt.getParameter("arguments").artifact ||
				versionId!=evt.getParameter("arguments").version ||
				workspaceSlug!=evt.getParameter("arguments").workspaceSlug){
			
			groupId = evt.getParameter("arguments").group;
			artifactId = evt.getParameter("arguments").artifact;
			versionId = evt.getParameter("arguments").version;
			workspaceSlug = evt.getParameter("arguments").workspaceSlug;
			model.Config.cleanRequests();
			var req;
			while(req = ajaxQueue.pop()){
				req.abort();
			}
			this.getView().byId('componentPage').setTitle(
					groupId + " : " + artifactId + " : " + versionId);
			
			this.loadData();
		}
	},
	
	// Returns an empty model
	getEmptyModel:function() {
		var emptyModel = new sap.ui.model.json.JSONModel();
		emptyModel.setData([]);
		return emptyModel;
	},
	
	computeDebloatViewMetrics : function(_test){
		var archiveInScope = this.getView().byId("archiveInScope");
		var archiveDebloat = this.getView().byId("archiveDebloat");
		var archiveTraced = this.getView().byId("archiveTraced");
		var archiveReachable = this.getView().byId("archiveReachable");
		var archiveTotalTraces = this.getView().byId("archiveTotalTraces");
		
		var archivesInTable = this.getView().byId("idBloatList").getModel().getObject("/");
		var archives = 0;
		var traced = 0;
		var reachable = 0;
		var traces = 0;
		var debloatable = 0;
		for (var a=0;a<archivesInTable.length;a++){
			if(_test || archivesInTable[a].scope!="TEST"){
				archives++;
				if(archivesInTable[a].tracedExecConstructsCounter>0){
					traced++;
					traces = traces + archivesInTable[a].tracedExecConstructsCounter;
				}
				if(archivesInTable[a].reachExecConstructsCounter>0){
					reachable++;
				}
				if(archivesInTable[a].tracedExecConstructsCounter==0 && archivesInTable[a].reachExecConstructsCounter==0 && archivesInTable[a].lib.constructTypeCounters.countExecutable>0){
					debloatable++;
				}
			}
		}
		
		archiveInScope.setText("Archives in selected scopes: " + archives);
		archiveDebloat.setText("Debloatable archives: " + debloatable);
		archiveTraced.setText("Archives Traced: " + traced);
		archiveReachable.setText("Archives Reachable: " + reachable);
		archiveTotalTraces.setText("Total Number of Traces: " + traces);
		
	},
	
//	includeTestDeps: function(){
//		if(this.getView().byId("showDebloat").getSelected()==true){
//			this.showDebloatableArchives();
//		}
//		else{
//			var scopeColumn = this.getView().byId("scopeColumn");
//			var oBloatView = this.getView().byId("idBloatList");
//			if(scopeColumn.getFiltered()==true){
//				scopeColumn.setFilterValue("");
//				scopeColumn.setFiltered(false);
//				oBloatView.filter(scopeColumn,"")
//				this.computeDebloatViewMetrics(true);
//			}
//			else{
//				scopeColumn.setFilterValue("!=TEST");
//				scopeColumn.setFiltered(true);
//				oBloatView.filter(scopeColumn,"!=TEST")
//				this.computeDebloatViewMetrics(false);
//			}
//		}
//	},
	
	showDebloatableArchives: function(){
		var array=[];
		var scopeColumn = this.getView().byId("scopeColumn");
		if(this.getView().byId("showDebloat").getSelected()==true){
			var filter1 = new sap.ui.model.Filter(
				    "reachExecConstructsCounter", 
				    sap.ui.model.FilterOperator.EQ, 
				    "0");
			var filter2 = new sap.ui.model.Filter(
				    "tracedExecConstructsCounter", 
				    sap.ui.model.FilterOperator.EQ, 
				    "0");
			var filter3 = new sap.ui.model.Filter(
				    "lib/constructTypeCounters/countExecutable", 
				    sap.ui.model.FilterOperator.NE, 
				    "0");
			
			array.push(filter1);
			array.push(filter2);
			array.push(filter3);
		}
		if(this.getView().byId("includeTest").getSelected()==false){
			var filter4 = new sap.ui.model.Filter(
				    "scope", 
				    sap.ui.model.FilterOperator.NE, 
				    "TEST");
			array.push(filter4);
			scopeColumn.setFilterValue("!=TEST");
			scopeColumn.setFiltered(true);
			this.computeDebloatViewMetrics(false);
		}
		else if (this.getView().byId("includeTest").getSelected()==true){
			scopeColumn.setFilterValue("");
			scopeColumn.setFiltered(false);
			this.computeDebloatViewMetrics(true);
		}
		
		if(array!=[]){
			var tableFilter = new sap.ui.model.Filter({
					    filters: array,
					    and: true
			});

			this.getView().byId("idBloatList").getBinding("rows").filter(tableFilter);
		}
		else{
			this.getView().byId("idBloatList").getBinding("rows").filter();
		}
		this.getView().byId("idBloatList").setVisibleRowCount(this.getView().byId("idBloatList").getBinding("rows").iLength);
		
	},
	
	// Vulnerabilities tab: Sets the 2 counters
	setVulnDepCounters:function(_archives, _vulns) {
		var archiveCount = this.getView().byId("archiveCount");
		var vulnCount = this.getView().byId("vulnCount");
		if(_archives===null && _vulns===null) {
			archiveCount.setText("Vulnerable Archives (distinct digest): loading..." );
			vulnCount.setText("Vulnerabilities: loading..." );
		}
		else {
			archiveCount.setText("Vulnerable Archives (distinct digest): " + _archives);
			vulnCount.setText("Vulnerabilities: " + _vulns);
		}
	},

	// Vulnerabilities tab: Sets date for last scan
	loadAndSetLastScanDate: function() {
		var lastScanDate = this.getView().byId("lastScanDate");
		const prefix = 'Date of last scan (APP goal): '
		lastScanDate.setText(prefix + "loading...");
		var oLastScanModel = this.getEmptyModel();
		that = this;
		return new Promise(function(resolve, reject) {
			try {
				var sUrl = model.Config.getLatestGoalExecutionServiceUrl(groupId, artifactId, versionId, model.lastChange)
				model.Config.addToQueue(oLastScanModel);
				model.Config.loadData (oLastScanModel, sUrl, 'GET');
				oLastScanModel.attachRequestCompleted(function() {
					model.Config.remFromQueue(oLastScanModel);			
					var _date = oLastScanModel.getObject("/startedAtClient");
					_date = _date.substring(0,19);
					_date = _date.replace("T", ", ");
					var lastScanDate = that.getView().byId("lastScanDate");
					lastScanDate.setText(prefix + _date );
					resolve()
				});
			} catch (e) {
				lastScanDate.setText(prefix + "error");
				reject(e)
			}
		})
	},
	
	toggleAdvancedResults: function () {
		if(this.getView().byId("toggleAdvancedResults").getSelected()){
			this.getView().byId("idReach").setVisible(true);
			this.getView().byId("idExec").setVisible(true);
		} else {
			this.getView().byId("idReach").setVisible(false);
		    this.getView().byId("idExec").setVisible(false);
		}
	},

	// Vulnerabilities tab: Loads data from backend, post-processes CVSS info and prepares mitigation tab
	loadVulns:function(hard) {
		
		// Empty table, set to busy and reset counter
		var oConstructView = this.getView().byId("idPatchAnalysisList");
		oConstructView.setNoData("Loading...");
		oConstructView.setModel(this.getEmptyModel());
		oConstructView.setBusy(true);
		this.setVulnDepCounters(null, null);
		this.loadAndSetLastScanDate();
		
		// URL to load data
		var incl_hist = this.getView().byId("includeHistorical").getSelected();
		var incl_unconfirmed = this.getView().byId("includeUnconfirmed").getSelected();
		  
		var add_excemption_info = true;
		var cache = model.lastChange
		if (hard === true) {
			cache = false
		}
		var sUrl = model.Config.getUsedVulnerabilitiesServiceUrl(groupId, artifactId, versionId, incl_hist, incl_unconfirmed, add_excemption_info, cache);
		
		model.Config.addToQueue(oVulnModel);
		model.Config.loadData (oVulnModel, sUrl, 'GET');
		oVulnModel.attachRequestCompleted(function() {
			model.Config.remFromQueue(oVulnModel);
			oConstructView.setVisibleRowCount(oVulnModel.getObject("/").length);

			// Update CVSS data and compute overall counters
			var data = oVulnModel.getObject("/");
			var archives = [];
			var vuln_count = 0;
			for(var v =0; v<data.length; v++) {

				// Update counters
				if(data[v].affected_version===1 || data[v].affected_version_confirmed===0) {
					// Add to distinct archives
					if(archives.indexOf(data[v].dep.lib.digest)==-1)
						archives.push(data[v].dep.lib.digest);
					// Increase vuln count
					vuln_count++;
				}
				
				// Set CVSS score for bugs w/o such info
				if(data[v].bug.cvssScore===null || data[v].bug.cvssVersion===null) {
					data[v].bug.cvssScore = 11; // Higher than 10 in order to make sure they appear above the CVSS 10 vulns (when sorted descending)
				}
				
				// Vuln description
				if(data[v].bug.description===null) {
					if(data[v].bug.descriptionAlt===null) {
						data[v].bug.tooltip = "n/a"; 
					} else {
						data[v].bug.tooltip = data[v].bug.descriptionAlt;
					}
				}
				else {
					if(data[v].bug.descriptionAlt===null) {
						data[v].bug.tooltip = data[v].bug.description;
					} else {
						data[v].bug.tooltip = data[v].bug.description + " Addendum: " + data[v].bug.descriptionAlt;
					}
				}
				
			}
			
			// Update counters
			this.setVulnDepCounters(archives.length, vuln_count);
			
			// Set model for tab
			oVulnModel.setData(data);
			oConstructView.setModel(oVulnModel);
			oConstructView.setNoData("No known vulnerabilities found");
			oConstructView.setBusy(false);
			//oConstructView.getModel().refresh(true);
			
			
			// Compute model for mitigation tab
			//var libVulns = oVulnModel.getObject("/") ;
			var libVulns = data;
			var mitigationArray = [];
			var mitigationRow = {};
			var processed = [];
			var count=0;
			for(var v=0;v<libVulns.length;v++){
				if(libVulns[v].affected_version==1){
					count = 1;
					mitigationRow = {};
					if(processed.indexOf(libVulns[v].dep.lib.digest)==-1){
						processed.push(libVulns[v].dep.lib.digest);
						mitigationRow.dep = libVulns[v].dep;
						for(var w=v+1;w<libVulns.length;w++) {
							if(libVulns[v].dep.lib.digest==libVulns[w].dep.lib.digest){
								if(libVulns[w].affected_version==1)
									count++;
							}
						}
						mitigationRow.count = count;
						mitigationRow.latest = "Loading...";
						mitigationArray.push(mitigationRow);
					}
				}
			}
					//console.log(JSON.stringify(packages));
			var oMitigationTable = this.getView().byId("idLibraryList");
			var mitigationModel = new sap.ui.model.json.JSONModel();
			mitigationModel.setData(mitigationArray);
			oMitigationTable.setModel(mitigationModel);
			oMitigationTable.setVisibleRowCount(mitigationModel.getObject("/").length);
		//	oMitigationTable.rerender();
			oMitigationTable.setBusy(false);
			
			this.loadLatestAsynch();
			//	var component = this.getView("view.Component");
			//component.getController().loadLatestAsynch();
			
			
				
		}.bind(this));
	},
	
	onSelectChanged: function(oEvent) {
//		 var key =oEvent.getParameters().key;
//         if(key=='4') {
//        	 this.loadLatestAsynch();
//         }
	},
	
	loadData:function(oEvent) {
		let hard = false
		if (oEvent && oEvent.getSource()) {
			const method = oEvent.getSource().data("hard")
			if ( method === "true") {
				hard = true
			}
		}
		
		// Used for table labels
		var i18nModel = new sap.ui.model.resource.ResourceModel({
			bundleUrl : "i18n/messageBundle.properties"
		});
		
	//	var oApp = sap.ui.getCore().byId("__xmlview0--idListApplications");
	//	oApp.setBusy(true);
		
		var oArchiveView = this.getView().byId("idArchivesList");
		oArchiveView.setModel(this.getEmptyModel());
		oArchiveView.setBusy(true);
		
		var oBloatView = this.getView().byId("idBloatList");
		oBloatView.setModel(this.getEmptyModel());
		oBloatView.setBusy(true);
		
		//var oConstructView = this.getView().byId("idPatchAnalysisList");		
		//oConstructView.setModel(emptyModel);
		//oConstructView.setBusy(true);
		
		var oMitigationTable = this.getView().byId("idLibraryList");
		oMitigationTable.setModel(this.getEmptyModel());
		oMitigationTable.setBusy(true);
		
		var oExecutionsView = this.getView().byId("idExecutionsList");
		oExecutionsView.setModel(this.getEmptyModel());
		
		var oRatioView = this.getView().byId("idRatioList");
		oRatioView.setModel(this.getEmptyModel());
		oRatioView.setBusy(true);
		
		var oPackagesView = this.getView().byId("idPackagesList");
		oPackagesView.setModel(this.getEmptyModel());
		oPackagesView.setBusy(true);
		
		var resultTable = this.getView().byId("idSearchResultTable");
		resultTable.setModel(this.getEmptyModel());
		resultTable.setVisibleRowCount(0);
		this.getView().byId("idSearchResultCounter").setText("Result count: N/a");
	
		this.getView().byId("id-legend").setText("");
		this.getView().byId("id-legend1").setText("");

	
		var sUrl = "";
		var oPackagesModel = new sap.ui.model.json.JSONModel();
		var oArchiveCoverageModel = new sap.ui.model.json.JSONModel();
		var oAppDepRatioModel = new sap.ui.model.json.JSONModel();
		var oArchiveModel = new sap.ui.model.json.JSONModel();
		var oExecutionsModel = new sap.ui.model.json.JSONModel();
		oVulnModel = new sap.ui.model.json.JSONModel();
		oWHistModel = null;
		
		// Vulnerabilities tab
		if (!model.Config.isMock) {
			this.loadVulns(hard);
		}
		
		// Archive-Tab All
		if (!model.Config.isMock) {
			var cache = model.lastChange
			if (hard) {
				cache = false
			}
			sUrl = model.Config.getArchivesServiceUrl(groupId,artifactId,versionId, cache);

			
			var archiveTotal = this.getView().byId("archiveTotal");
			var archiveAvgAge = this.getView().byId("archiveAvgAge");
			
			var archiveInScope = this.getView().byId("archiveInScope");
			var archiveDebloat = this.getView().byId("archiveDebloat");
			var archiveTraced = this.getView().byId("archiveTraced");
			var archiveReachable = this.getView().byId("archiveReachable");
			var archiveTotalTraces = this.getView().byId("archiveTotalTraces");
			
			var scopeColumn = this.getView().byId("scopeColumn");
			
			archiveTotal.setText("Archives Total: ");
			archiveAvgAge.setText("Average age (in months): ");
			

			archiveInScope.setText("Archives in selected scopes: ");
			archiveDebloat.setText("Debloatable archives: ");
			archiveTraced.setText("Archives Traced: ");
			archiveReachable.setText("Archives Reachable: ");
			archiveTotalTraces.setText("Total Number of Traces: ");
			
			
			model.Config.addToQueue(oArchiveModel);
			model.Config.loadData(oArchiveModel, sUrl, 'GET');
			
			oArchiveModel.attachRequestCompleted(function() {
				model.Config.remFromQueue(oArchiveModel);
				oArchiveView.setVisibleRowCount(oArchiveModel.getObject("/").length);
				oArchiveView.setModel(oArchiveModel);
				oArchiveView.setBusy(false);

				oBloatView.setModel(oArchiveModel);
				scopeColumn.setFilterValue("!=TEST");
				scopeColumn.setFiltered(true);
				oBloatView.filter(scopeColumn,"!=TEST")
				oBloatView.setBusy(false);
				
				var archives = oArchiveModel.getObject("/");
				var days = 0, archives_with_timestamp = 0, now = Date.now();
				for(var a=0;a<archives.length; a++){
					
					if(archives[a].lib.digestTimestamp!=null) {
						archives_with_timestamp++;
						var timestamp19 = archives[a].lib.digestTimestamp.substring(0,19);
						var timestamp = Date.parse(timestamp19);
						days += Math.floor(Math.abs(now-timestamp)/86400000);
					}
				}
				archiveTotal.setText("Archives Total: " + archives.length);
				archiveAvgAge.setText("Average age (in months): " + (archives_with_timestamp==0 ? "n/a" : Math.floor(days/30/archives_with_timestamp)));
				
				this.computeDebloatViewMetrics(false);
				
			}.bind(this));
		}
		
		// Goal-Executions-Tab
		if (!model.Config.isMock) {
			var cache = model.lastChange
			if (hard) {
				cache = false
			}
			sUrl = model.Config.getGoalExecutionsServiceUrl(groupId,artifactId,versionId, cache);
			
			model.Config.addToQueue(oExecutionsModel);
			model.Config.loadData (oExecutionsModel,sUrl, 'GET');
			
			oExecutionsModel.attachRequestCompleted(function() {
				model.Config.remFromQueue(oExecutionsModel);
				var execs = [];
				var exec = oExecutionsModel.getObject("/") ;
				for(var g=0;g<exec.length;g++){
					var item;
					item = exec[g];
					var ns=exec[g].runtimeNano;
					var memMax = exec[g].memMax;
					var memUsedMax = exec[g].memUsedMax;
					var memUsedAvg = exec[g].memUsedAvg;
					if(memMax!=-1)
						item.memMax = Math.round((memMax/(1024*1024*1024))*100)/100;
					if(memUsedMax!=-1)
						item.memUsedMax = Math.round((memUsedMax/(1024*1024))*100)/100;
					if(memUsedAvg!=-1)
						item.memUsedAvg = Math.round((memUsedAvg/(1024*1024))*100)/100;
					if(ns!=-1){
						var ms = ns/1000000;
						var seconds = ms / 1000;
						
						var hh = parseInt( ms / 3600 /1000); 
						ms = ms % 3600000; 
						    
						var mm = parseInt( ms / 60 /1000); 
						  
						ms = ms % 60000;
						
						var ss = parseInt( ms / 1000);
						
						ms = Math.floor(ms % 1000);
						
						// Create the formatted timespan (duration) string
						item.runtimeNanoFormatted = "";
						if(hh!==0)
							item.runtimeNanoFormatted += hh.toString() + " h ";
						if(mm!==0)
							item.runtimeNanoFormatted += mm.toString() + " m ";
						if(ss!==0)
							item.runtimeNanoFormatted += ss.toString() + " s ";
						item.runtimeNanoFormatted += ms.toString() + " ms";
					}
					execs.push(item);
					
				}
				
				oExecutionsModel.setData(execs);
				
			
				
				oExecutionsView.setModel(oExecutionsModel);
				oExecutionsView.setVisibleRowCount(oExecutionsModel.getObject("/").length);
			})
		}

		// Test Coverage Tab
		if (!model.Config.isMock) {
			
			var execConstructTotal = this.getView().byId("execConstructTotal");
			var execConstructTraced = this.getView().byId("execConstructTraced");
			
			// Coverage of app packages
			var cache = model.lastChange
			if (hard) {
				cache = false
			}
			sUrl = model.Config.getPackagesWithTestCoverageServiceUrl(groupId,artifactId,versionId, cache);

			model.Config.addToQueue(oPackagesModel);
			model.Config.loadData (oPackagesModel,sUrl, 'GET');
			//oPackagesView.setModel(oPackagesModel);
			/*$.get(sUrl, function(data) {
				oPackagesModel.setData(data);
				oPackagesView.setModel(oPackagesModel);
			}.bind(this));*/
			//var busyIndicator = new sap.m.BusyIndicator();
			
			oPackagesModel.attachRequestCompleted(function() {
				model.Config.remFromQueue(oPackagesModel);
				var packageCounters = oPackagesModel.getObject("/packageCounters/packageCounters") ;
				var packageCountersTraced = oPackagesModel.getObject("/packageTraceCounters/packageCounters") ;
			//	console.log(JSON.stringify(packageCountersTraced));
				var packages = [];
				var singlePackage={};
				var total = 0;
				var traced = 0;
				if(packageCounters!=null) {
					for(var k in Object.keys(packageCounters)) {
						var pack = Object.keys(packageCounters)[k];
						singlePackage={};
						singlePackage.name=pack.slice(pack.lastIndexOf(':')+1,pack.length-1);
						singlePackage.constructors=packageCounters[pack].CONS;
						singlePackage.methods=packageCounters[pack].METH;
						singlePackage.modules=packageCounters[pack].MODU;
						singlePackage.functions=packageCounters[pack].FUNC;
						singlePackage.static_inits=packageCounters[pack].INIT;
						
						if(packageCountersTraced!=null&&packageCountersTraced[pack]){
						//add if else to avoid traced CONSTRUCTORS > traced
						//	if(packageCountersTraced[pack].CONS > singlePackage.constructors)
						//		singlePackage.constructorsTested=singlePackage.constructors;
						//	else
							singlePackage.constructorsTested=packageCountersTraced[pack].CONS;
							singlePackage.methodsTested=packageCountersTraced[pack].METH;
							singlePackage.modulesTested=packageCountersTraced[pack].MODU;
							singlePackage.functionsTested=packageCountersTraced[pack].FUNC;
							singlePackage.static_initsTested=packageCountersTraced[pack].INIT;
							traced = traced + packageCountersTraced[pack].countExecutable;
						}
						else {
							singlePackage.constructorsTested = 0;
							singlePackage.methodsTested = 0;
							singlePackage.static_initsTested = 0;
							singlePackage.modulesTested = 0;
							singlePackage.functionsTested = 0;
						}
						
						//TODO ADD MODU & FUNCTION? (INITIATED
					    // Test coverage on total library
						if (!((singlePackage.constructors == 0) && (singlePackage.methods == 0) && (singlePackage.static_inits == 0)
								&&  (singlePackage.modules == 0) && (singlePackage.functions == 0))) {
							singlePackage.testcoverage = Math.round(((singlePackage.constructorsTested + singlePackage.methodsTested + singlePackage.static_initsTested) / (singlePackage.constructors + singlePackage.methods + singlePackage.static_inits)) * 10000) / 100 + " %";
						} else {
							singlePackage.testcoverage = "100 %";
						}
						total = total + packageCounters[pack].countExecutable;
						
						packages.push(singlePackage);
					}
					execConstructTotal.setText("Executable application constructs (total): " + total);
					execConstructTraced.setText("Executable application constructs (traced): " + traced);
				}
				
				var packageModel = new sap.ui.model.json.JSONModel();
				packageModel.setData(packages);
				oPackagesView.setModel(packageModel);
				oPackagesView.setVisibleRowCount(packages.length);
				oPackagesView.setBusy(false);
				
		//		oApp.setBusy(false);
				
			});
			
			// App Dep Ratio
			var depCounter = this.getView().byId("depCounter");
			var cache = model.lastChange
			if (hard) {
				cache = false
			}
			sUrl = model.Config.getAppDepRatios(groupId, artifactId, versionId, cache);			
			
			model.Config.addToQueue(oAppDepRatioModel);
			model.Config.loadData(oAppDepRatioModel, sUrl, 'GET');
			
			oAppDepRatioModel.attachRequestCompleted(function() {
				model.Config.remFromQueue(oAppDepRatioModel);
				// Count app dependencies
				var counters = oAppDepRatioModel.getObject("/counters")
				var dep_counter = counters[0];
				depCounter.setText("Dependencies (excluding scopes TEST and PROVIDED): " + dep_counter.count);
				
				// Prepare received data for table display
				var ratios = oAppDepRatioModel.getObject("/ratios");
				for(var i=0; i<ratios.length; i++) {
					ratios[i].name = i18nModel.getProperty(ratios[i].name);
					if(ratios[i].total===-1) {
						ratios[i].ratio = "N/a";
					} else {
						ratios[i].ratio = (Math.round(ratios[i].ratio*10000)) / 100;
						ratios[i].ratio = ratios[i].ratio + " %";
					}					
				}
				
				// Create model and update table
				var ratioModel = new sap.ui.model.json.JSONModel();
				ratioModel.setData(ratios);
				oRatioView.setModel(ratioModel);
				oRatioView.setVisibleRowCount(ratios.length);
				oRatioView.setBusy(false);
				
				
			});
			
			// Coverage of archives
			//Url = model.Config.
			/*getArchivesWithTestCoverageServiceUrl()
					+ "?groupid=" + groupId + "&artifactid=" + artifactId
					+ "&version=" + version;*/
		//	oArchiveCoverageView = this.getView().byId("idArchiveCoverage");
			//model.Config.loadData (oArchiveCoverageModel,sUrl, 'GET');
			
//			var archiveCounters = [];
//			//archive converage info moved to /apps/{GAV}/
//			//oArchiveModel.attachRequestCompleted(function() {
//				//var archiveList = oArchiveModel.getObject("/");
//			oPackagesModel.attachRequestCompleted(function() {
//			
//				var archiveList = oPackagesModel.getObject("/dependencies");
//
//				for(var i=0;i<archiveList.length;i++){
//					//console.log(i+": "+JSON.stringify(archiveList[i]));
//					var constructTypeCounters = archiveList[i].lib.constructTypeCounters ;
//					var constructTypeCountersReachable = archiveList[i].reachableConstructTypeCounters;
//					var constructTypeCountersTraced = archiveList[i].tracedConstructTypeCounters ;
//					var archiveCounter = {};
//					archiveCounter.id=archiveList[i].lib.sha1;
//					archiveCounter.filename=archiveList[i].filename;
//					archiveCounter.constructors=constructTypeCounters.CONS;
//					archiveCounter.methods=constructTypeCounters.METH;
//				
//					if(constructTypeCountersTraced!=null){
//						archiveCounter.constructorsTested=constructTypeCountersTraced.CONS;
//						archiveCounter.methodsTested=constructTypeCountersTraced.METH;
//					}
//					else{
//						archiveCounter.constructorsTested=0;
//						archiveCounter.methodsTested=0;
//					}
//					if(constructTypeCountersReachable!=null){
//						archiveCounter.constructorsReachable=constructTypeCountersReachable.CONS;
//						archiveCounter.methodsReachable=constructTypeCountersReachable.METH;
//					}
//					else{
//						archiveCounter.constructorsReachable=0;
//						archiveCounter.methodsReachable=0;
//						}
//
//					if (!((archiveCounter.constructors == 0) && (archiveCounter.methods == 0))) {
//						archiveCounter.testcoverage = Math.round(((archiveCounter.constructorsTested + archiveCounter.methodsTested) / (archiveCounter.constructors + archiveCounter.methods)) * 100) 
//								+ " %";
//					} else {
//						archiveCounter.testcoverage = 100 + "%";
//					}
//					if (!((archiveCounter.constructorsReachable == 0) && (archiveCounter.methodsReachable == 0))) {
//						archiveCounter.reachabletestcoverage = Math.round(((archiveCounter.constructorsTested + archiveCounter.methodsTested) / (archiveCounter.constructorsReachable + archiveCounter.methodsReachable)) * 100) 
//								+ " %";
//					} else {
//						archiveCounter.reachabletestcoverage = 100 + "%";
//					}
//					archiveCounters.push(archiveCounter);
//				}
//			
//			//	console.log(JSON.stringify(archiveCounters));
//				var archiveCoverageModel = new sap.ui.model.json.JSONModel();
//				archiveCoverageModel.setData(archiveCounters);
//				oArchiveCoverageView.setModel(archiveCoverageModel);
//				oArchiveCoverageView.setBusy(false);
//			});
				//oArchiveCoverageView.setModel(oArchiveCoverageModel);
				/*$.get(sUrl, function(data) {
					oArchiveCoverageModel.setData(data);
					oArchiveCoverageView.setModel(oArchiveCoverageModel);
				}.bind(this));*/
		}
		
		//set tooltips
		
	//	var sHtml = "";//The rating specifies the trustworthiness of the account. You can choose between the following values:<br>";
	//	sHtml += "<ul>";
		//sHtml += "<li><img src=\"img/version_alert.png\"></img> Vulnerable Version</li>";
		//sHtml += "<li><img src=\"img/version_ok.png\"></img> Fixed Version</li>";
		//sHtml += "<li><img src=\"img/version_qmark.png\"></img> Info not yet available</li>";
		//sHtml += "</ul>";
		
		
		var oRttTextField = new sap.ui.commons.RichTooltip({
			//text : sHtml
			title:"Legend",
			imageSrc : "img/version_legend.png"
		});

		var vulnImg = this.getView().byId("affectedVersion");
		vulnImg.setTooltip(oRttTextField);

		var oRttTextField1 = new sap.ui.commons.RichTooltip({
			//text : sHtml
			title:"Legend",
			imageSrc : "img/reach_legend.png"
		});

		var vulnImg = this.getView().byId("vulnReachable");
		vulnImg.setTooltip(oRttTextField1);

		var oRttTextField2 = new sap.ui.commons.RichTooltip({
			//text : sHtml
			title:"Legend",
			imageSrc : "img/trace_legend.png"
		});

		var vulnImg = this.getView().byId("vulnTraced");
		vulnImg.setTooltip(oRttTextField2);

		
	},

	onArchiveListItemTap : function(oEvent) {
		//console.log(oEvent.getSource().getText());
		var archiveid = oEvent.getSource().getText();
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("archiveDetail", {
			workspaceSlug: workspaceSlug,
			group : groupId,
			artifact : artifactId,
			version : versionId,
			archiveid : archiveid
		});
	},		
//	onArchiveListItemTap : function(oEvent) {
//		var archiveid = oEvent.getParameter("listItem").getBindingContext()
//				.getObject().lib.sha1;
//		this.router.navTo("archiveDetail", {
//	        workspaceSlug: workspaceSlug,
//			group : groupId,
//			artifact : artifactId,
//			version : version,
//			archiveid : archiveid
//		});
		
		// Remove selection so that you can click on the same item again
//		oEvent.getSource().removeSelections();
//	},
	
	
	onSearchButtonPress : function() {
		
		// Search string
		var search_string = this.getView().byId("idSearchString").getValue();
		
		// Return in case of 'invalid' search string
		if(search_string==="" || search_string.length<=3) {
			return;
		}
		
		// Set table to 'busy'
		var searchResultCounter = this.getView().byId("idSearchResultCounter");
		searchResultCounter.setText("Result count: Search in progress...");
		
		var resultTable = this.getView().byId("idSearchResultTable");
		resultTable.setBusy(true);
		
		var wildcardSearch = this.getView().byId("idSearchWildcard");
		var do_wc = wildcardSearch.getSelected();
		
		// Build URL
		var url = model.Config.getHostBackend()
		url += "/apps/" + groupId + "/" + artifactId + "/" + versionId + "/search?searchString=" + search_string + "&wildcardSearch=" + do_wc;
		
		// Load data
		var oSearchResult = new sap.ui.model.json.JSONModel();
		model.Config.addToQueue(oSearchResult);
		model.Config.loadData(oSearchResult, url, 'GET');
		
		// Update table once data has been loaded
		oSearchResult.attachRequestCompleted(function() {
			model.Config.remFromQueue(oSearchResult);
			// Number of search results
			var count = oSearchResult.getObject("/").length;
			var count_displayed = Math.min(100, count);
			if(count>100) {
				searchResultCounter.setText("Result count: " + count + " (only the first 100 results are shown)");
			} else {
				searchResultCounter.setText("Result count: " + count);
			}
			
			// Max display 100 results
			var oMaxSearchResult = new sap.ui.model.json.JSONModel();
			var searchResultData = oSearchResult.getData();
			var newSearchResultData = [];
			for(var j=0;j<count_displayed; j++) {
				newSearchResultData.push(searchResultData[j]);
			}
			oMaxSearchResult.setData(newSearchResultData);			
			
			// Populate table
			resultTable.setModel(oSearchResult);
			resultTable.setVisibleRowCount(count_displayed);
			resultTable.setBusy(false);
		});
	},
	
	onSearchResultItemTap : function(oEvent) {
		var archiveid = oEvent.getParameters().rowBindingContext.getObject("dependency/lib/digest");
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("archiveDetail", {
			workspaceSlug: workspaceSlug,
			group : groupId,
			artifact : artifactId,
			version : versionId,
			archiveid : archiveid
		});
	},

	onBugListItemTap : function(oEvent) {
		//console.log(oEvent);
		var bugid = oEvent.getParameters().rowBindingContext.getObject("bug/bugId"); 
		//var bugid = oEvent.getParameter("listItem").getBindingContext()
		//		.getObject().bug.bugId;
		var archiveid = oEvent.getParameters().rowBindingContext.getObject("dep/lib/digest");
			//oEvent.getParameter("listItem").getBindingContext()
			//	.getObject().dep.lib.digest;
		var origin = oEvent.getParameters().rowBindingContext.getObject("vulnDepOrigin");
		model.Config.setVulnDepOrigin(origin);
		if(origin == 'BUNDLEDCC')
			model.Config.setBundledDigest(oEvent.getParameters().rowBindingContext.getObject("bundledLib/digest"));
		else if(origin == 'BUNDLEDAFFLIBID'){
			model.Config.setBundledGroup(oEvent.getParameters().rowBindingContext.getObject("bundledLibId/group"));
			model.Config.setBundledArtifact(oEvent.getParameters().rowBindingContext.getObject("bundledLibId/artifact"));
			model.Config.setBundledVersion(oEvent.getParameters().rowBindingContext.getObject("bundledLibId/version"));
		}
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("bugDetail", {
			workspaceSlug: workspaceSlug,
			group : groupId,
			artifact : artifactId,
			version : versionId,
			bugid : bugid,
			archiveid : archiveid
		});
		
		// Remove selection so that you can click on the same item again
		//oEvent.getSource().removeSelections();
	},
	
	onDigestListItemTap : function(oEvent) {

		var archiveid = oEvent.getParameters().rowBindingContext.getObject("dep/lib/digest");
			//oEvent.getParameter("listItem").getBindingContext()
			//	.getObject().dep.lib.sha1;
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("archiveDetail", {
			workspaceSlug: workspaceSlug,
			group : groupId,
			artifact : artifactId,
			version : versionId,
			archiveid : archiveid
		});
		
		// Remove selection so that you can click on the same item again
		//oEvent.getSource().removeSelections();
	},
	
	onExecutionListItemTap : function(oEvent) {
		//var exe_id = oEvent.getParameter("listItem").getBindingContext().getObject().id;
		var exe_id = oEvent.getParameters().rowBindingContext.getObject("id");
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("exeDetail", {
			workspaceSlug: workspaceSlug,
			group : groupId,
			artifact : artifactId,
			version : versionId,
			exeid : exe_id
		});
		
		// Remove selection so that you can click on the same item again
		oEvent.getSource().removeSelections();
	},


	handleNavBack : function() {
		this.router.myNavBack("master", {});
	},
	
	resetVulnTable : function(){
		
		var oConstructView = this.getView().byId("idPatchAnalysisList");
		//var column = this.getView().byId(oConstructView.getGroupBy());
		var column_id=oConstructView.getGroupBy();
		var columns = oConstructView.getColumns();
		var column = null;
		for(var j=0;j<columns.length; j++){
			if(columns[j].getId()==column_id){
				column = columns[j];
			}
		}
		
	//	console.log(column);
	//	var column_id = oConstructView.getGroupBy();
		if(column!=undefined && column!=null){
			column.setGrouped(false);
		}
		oConstructView.setEnableGrouping(false);
		oConstructView.setGroupBy(null);
		oConstructView.getModel().refresh(true);
			
		oConstructView.setEnableGrouping(true);
		oConstructView.getModel().refresh(true);
	//	}
	},
	
	filterEvent : function(evt){
		//console.log(evt);
		var oConstructView = this.getView().byId("idPatchAnalysisList");
		
	//	console.log(oConstructView.getBinding().getLength());
	//	this.getView().byId("id-count").setText(oConstructView.getBinding().getLength());
		
	},
	
	openWiki : function(evt){
		
		var key = this.getView().byId("id_tabBar").getSelectedKey();
		if(key==this.getView().byId("id_compVuln").getId())
			model.Config.openWiki("user/manuals/frontend/#vulnerabilities");
		else if(key==this.getView().byId("id_compArch").getId())
			model.Config.openWiki("user/manuals/frontend/#dependencies");
		else if(key==this.getView().byId("id_compTest").getId())
			model.Config.openWiki("user/manuals/frontend/#application-statistics");
		else if(key==this.getView().byId("id_compExe").getId())
			model.Config.openWiki("user/manuals/frontend/#history");
		else if(key==this.getView().byId("id_compSearch").getId())
			model.Config.openWiki("user/manuals/frontend/#search");
		else if(key==this.getView().byId("id_compRem").getId())
			model.Config.openWiki("user/manuals/frontend/#mitigation");
		else
			model.Config.openWiki("user/manuals/frontend/#start-page");
	
		
	},
	
	
	onExit : function() {
		
	},
	
	loadLatestAsynch : function() {
		var oMitigationTable = this.getView().byId("idLibraryList");
		var mitigationModel = this.getView().byId("idLibraryList").getModel();
		
		
		
		oMitigationTable.setVisibleRowCount(mitigationModel.getObject("/").length);
		
		
		var el = JSON.parse(mitigationModel.getJSON());
	//	var archiveDetailPageController = this.getView("view.ArchiveDetail").getController();
		for(var o=0; o<el.length;o++) {
			let rowIndex=o;
			var lib = mitigationModel.getProperty("/"+rowIndex+"/dep/lib");
			if(lib.libraryId!=null) { // the additional condition is only needed if we load the latest upon selection of the mitigation tab (&& el[rowIndex].latest=="Loading...")
				// used when Mvn Central was not availble
				// TODO: use this as default
				//mitigationModel.setProperty("/"+rowIndex+"/latest", "Recommendation currently unavailable ***");
				//this.getView().byId("id-legend").setText("*** The lookup for the latest version in Maven Central is temporarily unreachable.");
				
				var oLatest = new sap.ui.model.json.JSONModel();

				// Check whether lib is known by Maven
				var mUrl = model.Config.isArchiveInMaven(lib.libraryId.group,lib.libraryId.artifact);
				var r = $.ajax({
			        type: "GET",
			        url: mUrl,
			        async: true,
			        headers : {'content-type': "application/json",'cache-control': "no-cache" },
			        statusCode: {
			        	
			        	// Lib is known by Maven
			            200: function(data, status, jqXHR1) {
			            	ajaxQueue.pop(r);
			            	var mitigationModel = this.getView().byId("idLibraryList").getModel();
			            	var lib = mitigationModel.getProperty("/"+rowIndex+"/dep/lib");
			            	// Get latest non-vulnerable version
							var	vUrl = model.Config.getArchiveVulnServiceUrl(lib.libraryId.group,lib.libraryId.artifact,null,true,true);
							var r1 = $.ajax({
						        type: "GET",
						        url: vUrl,
						        headers : {'content-type': "application/json",'cache-control': "no-cache" ,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'appfrontend'},
						        
						        success: function(data, status, jqXHR) {
						        	ajaxQueue.pop(r1);
						        	var model = this.getView().byId("idLibraryList").getModel();
						        	if(data.length>0){
							        	var oJson = JSON.parse(model.getJSON());
							        	
							        	var latest = data.length-1;
							        	
							        	/*for (var j=0;j<oJson.length;j++){
							        		if(oJson[j].dep.lib.libraryId!=null && oJson[j].dep.lib.libraryId.group ==  data[0].group && oJson[j].dep.lib.libraryId.artifact ==  data[0].artifact){
							        			model.setProperty("/"+j+"/latest", data[0].group.concat(":"+data[0].artifact).concat(":"+data[0].version));
								        		model.refresh();
							        		}
							        	}*/
							        	if(oJson[rowIndex].dep.lib.libraryId!=null && oJson[rowIndex].dep.lib.libraryId.group.toUpperCase() == data[latest].group.toUpperCase()&& oJson[rowIndex].dep.lib.libraryId.artifact.toUpperCase() ==  data[latest].artifact.toUpperCase())
							        		model.setProperty("/"+rowIndex+"/latest", data[latest].group.concat(":"+data[latest].artifact).concat(":"+data[latest].version));
										else{
											model.setProperty("/"+rowIndex+"/latest", data[latest].group.concat(":"+data[latest].artifact).concat(":"+data[latest].version).concat(" **"));
											this.getView().byId("id-legend1").setText("** The latest non-vulnerable release has a different group and/or artifact then the one in use. This may indicate that the library underwent a major refactoring. Please click on the row to get details about the update metrics, hence, migration efforts.");
										}
						        		model.refresh(true);
						        	}else{
						        		model.setProperty("/"+rowIndex+"/latest", "Latest is vulnerable");
						        		model.refresh(true);
						        	}
							        	
						        }.bind(this), 
					        });	
							ajaxQueue.push(r1);
				        }.bind(this),
				        
				        // Lib is not known by Maven
				        404: function(jqXHR1){
				        	ajaxQueue.pop(r);
				        	var model = this.getView().byId("idLibraryList").getModel();
				        	mitigationModel.setProperty("/"+rowIndex+"/latest", "Artifact identifier unknown to Maven Central *");
				        	this.getView().byId("id-legend").setText("* The lookup for the latest version in Maven Central is not possible.");
				        	model.refresh(true);
				        }.bind(this), 
				        500: function(jqXHR1){
				        	ajaxQueue.pop(r);
				        	var model = this.getView().byId("idLibraryList").getModel();
				        	mitigationModel.setProperty("/"+rowIndex+"/latest", "Recommendation currently unavailable ***");
							this.getView().byId("id-legend").setText("*** The lookup for the latest version in Maven Central is temporarily unreachable.");
							model.refresh(true);
				        }.bind(this),
			        }
			    });
				ajaxQueue.push(r);
			}
			else {
				mitigationModel.setProperty("/"+rowIndex+"/latest", "Artifact identifier unknown to Steady *");
				this.getView().byId("id-legend").setText("* The lookup for the latest version in Maven Central is not possible.");
			}
		} // for 
	},
});
