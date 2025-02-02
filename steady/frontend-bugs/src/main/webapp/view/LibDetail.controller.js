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
var sha1;

var archiveId;
var app = {};
var bAllShown = false;

sap.ui.controller("view.LibDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
                sha1 = evt.getParameter("arguments").sha1;      
		this.loadDataIntoView();
	},

	loadDataIntoView : function() {
	    if(sha1) {
    	    // Get page and set title
                var libDetailPage = this.getView().byId('idLibDetailPage');
                var libAppsPage = this.getView().byId('idLibAppsPage');
                var oLibDetailModel = new sap.ui.model.json.JSONModel();
                var oLibAppsModel = new sap.ui.model.json.JSONModel();
                if (!model.Config.isMock) {
                    var url = model.Config.getLibraryDetailsUrl(sha1);
                    model.Config.loadData(oLibDetailModel, url, 'GET');
                    libDetailPage.setModel(oLibDetailModel);
                    var urlApps = model.Config.getLibraryApplicationsUrl(sha1);
                    model.Config.loadData(oLibAppsModel, urlApps, 'GET');
                    oLibAppsModel.attachRequestCompleted(function(){
                    	var apps = [];
                    	var data=oLibAppsModel.getData();
                    	for(var i=0;i<data.length;i++){
                    		var app={};
                    		app.group=data[i].group;
                    		app.artifact=data[i].artifact;
                    		app.version=data[i].version;
                    		app.createdAt=data[i].createdAt;
                    		app.depFilename=data[i].dependencies[0].filename;
                    		
                    		apps.push(app);
                    	}
                    	var oLibAppsFinalModel = new sap.ui.model.json.JSONModel();
                    	oLibAppsFinalModel.setData(apps);
                    	libAppsPage.setModel(oLibAppsFinalModel);
                    });
                    
                }
	    }
	},
	
	onAppListTap : function(oEvent) {
		var g = oEvent.getParameters().rowBindingContext.getObject("group");
		var a = oEvent.getParameters().rowBindingContext.getObject("artifact");
		var v = oEvent.getParameters().rowBindingContext.getObject("version");
		window.open("/apps/#/apps/"+g+"/"+a+"/"+v, '_blank').focus();
		
		
	},
	

	
	
	openLink : function(_url, _window) {
	    window.open(_url, _window).focus();
	},

	onExit : function() {

	},

	handleNavBack : function() {
		window.history.go(-1);
	}
});

