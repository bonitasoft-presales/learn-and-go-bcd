package com.company.rest.api

import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder

class ProcessHelper {
	ProcessAPI processApi
	Tracer tracer

	public ProcessHelper(ProcessAPI processApi,Tracer tracer) {
		this.processApi = processApi
		this.tracer = tracer
	}


	def getProcessDefinitionId(def processName) {
		def processDefinitionId
		SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 1);
		optsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, processName);
		def searchResult = processApi.searchProcessDeploymentInfos(optsBuilder.done());
		searchResult.getResult().each{
			tracer.trace("process found","${it.processId} ${it.name} ${it.version}")
			processDefinitionId=it.processId
		}
		if (processDefinitionId==null) {
			throw new IllegalStateException("process $processName not found!" as String)
		}
		processDefinitionId
	}

	def cleanAllInstances(long processDefinitionId) {
		deleteActiveCases(processDefinitionId)
		deleteArchivedProcessInstances(processDefinitionId)
	}

	def deleteActiveCases(long processDefinitionId) {
		SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, 100);
		optionsBuilder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID,processDefinitionId)
		def searchProcessInstances = processApi.searchProcessInstances(optionsBuilder.done());
		searchProcessInstances.getResult().each{ processInstance->
			tracer.trace("found active case", "${processInstance.id} - ${processInstance.processDefinitionId}")
			processApi.deleteProcessInstance(processInstance.id)
		}
	}

	def deleteArchivedProcessInstances(processDefinitionId) {
		processApi.deleteArchivedProcessInstances(processDefinitionId, 0, 100)
	}

	def startProcess(def processDefinitionId) {
		tracer.trace("starting process", "$processDefinitionId")
		processApi.startProcess(processDefinitionId)
	}

	def startProcess(processDefinitionId,contractInputs) {
		processApi.startProcessWithInputs(processDefinitionId,contractInputs)
	}
}
