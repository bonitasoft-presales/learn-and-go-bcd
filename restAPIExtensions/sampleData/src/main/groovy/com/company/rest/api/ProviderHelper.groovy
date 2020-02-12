package com.company.rest.api

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.logging.Logger

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.contract.FileInputValue
import org.bonitasoft.web.extension.ResourceProvider

import com.github.javafaker.Address
import com.github.javafaker.Company
import com.github.javafaker.Faker

class ProviderHelper {
	APIClient apiClient
	ResourceProvider resourceProvider

	ProcessHelper processHelper
	Tracer tracer
	Faker faker = new Faker()

	long userId
	Logger logger=Logger.getLogger("org.bonitasoft")
	def ret=[:]

	public ProviderHelper(APIClient apiClient,ResourceProvider resourceProvider, long userId) {
		this.apiClient=apiClient
		this.resourceProvider=resourceProvider
		this.userId=userId

		this.tracer=new Tracer(logger,ret)
		this.processHelper=new ProcessHelper(apiClient.processAPI,tracer)
	}

	def createProviders(int count) {
		def deleteAllDefinitionId = processHelper.getProcessDefinitionId( "fakeDataProviders")
		def newProviderDefinitionID = processHelper.getProcessDefinitionId( "New Provider")


		processHelper.startProcess(deleteAllDefinitionId)
		//wait for process end
		Thread.sleep(3000)

		processHelper.cleanAllInstances(newProviderDefinitionID)

		count.times{ createProvider(newProviderDefinitionID) }

		ret
	}

	def createProvider(def newProviderDefinitionID) {


		Address address = faker.address()
		Company company = faker.company()

		Map<String,Serializable> contractInputs=new HashMap()
		Map<String,Serializable> contract=new HashMap()

		contract.put("country",address.country() )
		contract.put("name",company.name())
		contract.put("alias", company.buzzword())
		contract.put("provType", "Transporter")
		contract.put("provPriority", "Mail Order Selling")
		contract.put("address1",address.streetAddressNumber() + " " + address.streetName())
		contract.put("address2", address.secondaryAddress())
		contract.put("city", address.cityName())

		contract.put("telephone", faker.phoneNumber().cellPhone())
		contract.put("fax", faker.phoneNumber().phoneNumber())
		contract.put("email","info@"+company.domainName()+".com")
		contract.put("currency", "Euro")
		contract.put("paymentDelay" , 45)

		def contacts=[]
		5.times{
			contacts.add(aContact())
		}
		tracer.trace("contacts", contacts)
		contract.put("contact", contacts)

		contract.put("bankAccountDetails" ,[bankName : "BNP",
			accountName :"BNP account",
			accountNumber:"1234 5678 9101",
			bankSwift: "SWIFT 1234 5678 9101"])

		Instant instantOfNow = Instant.now()
		LocalDate localDate = LocalDateTime.ofInstant(instantOfNow, ZoneOffset.UTC).toLocalDate()
		LocalDateTime localDateTime=LocalDateTime.ofInstant(instantOfNow, ZoneOffset.UTC)


		contract.put("contract",  [contractAmount : 12453L,
			contractDate : localDate,
			contractName : "Contract name",
			dateCreation : localDateTime
		])

		//bdm inputs
		contractInputs.put("providerInput",contract)
		
		//files
		contractInputs.put("contractAtch",aPDF("Contract_Document.pdf"))
		contractInputs.put("badAtch",aPDF("Bank_Document.pdf"))

		def startProcess = processHelper.startProcess(newProviderDefinitionID,contractInputs)
	}

	def aContact() {
		def name = faker.name()
		def firstName = name.firstName()
		def lastName=name.lastName()
		[
			title:"Mr",
			firstname: firstName,
			surname:lastName,
			fonction:"Manager",
			phone:faker.phoneNumber().phoneNumber(),
			mobile:faker.phoneNumber().cellPhone(),
			contactType:"Director",
			email:"${name.username()}@acme.com" as String
		]
	}

	def aPDF(String documentName) {
		getFileInputValue( documentName,"lorem.pdf","application/pdf")
	}

	/**
	 * used for FILE contract input type
	 * @param fileName
	 * @param mimeType
	 * @return
	 */
	def getFileInputValue(String documentName,String fileName, String mimeType) {
		def stream = this.resourceProvider.getResourceAsStream(fileName)
		FileInputValue fileInputValue= new FileInputValue(documentName,mimeType,stream.getBytes())
		fileInputValue
	}

}
