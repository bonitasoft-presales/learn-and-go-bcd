package com.bonitasoft.presales

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.DayOfWeek

import com.company.model.BankAccountDetails
import com.company.model.Contact
import com.company.model.Contract
import com.company.model.Provider

class ProviderHelper {

	static def Instance = new ProviderHelper()

	def aProvider(Long processInstanceId){
		Provider provider=new Provider()
		provider.country="France"
		provider.name="UPS"
		provider.alias = "ups"
		provider.provType = "Transporter"
		provider.provPriority = "Mail Order Selling"
		provider.address1 = "32 rue Gustave Eiffel"
		provider.address2 = "4° étage"
		provider.city = "Grenoble"
		provider.country = "France"
		provider.telephone = "0123456789"
		provider.fax = "234567890"
		provider.email = "ups@amce.com"
		provider.currency = "Euro"
		provider.paymentDelay = 45

		provider.idCreator = 4 //walter
		provider.idProdEditor = 3 // helen
		provider.idAccountEdit = 3 // helen
		provider.idProdEditor = 3 // helen
		
		provider.dateCreation=LocalDateTime.now()
		provider.dateAcountEdit=LocalDateTime.now()
		provider.dateProdEdit=LocalDateTime.now()
		
		
		provider.contact= [aContact()]
		provider.bankAccountDetails = aBankAccountDetails()
		provider.contract = aContract()

		provider.pkid = processInstanceId as int

		provider
	}

	def aContact(){
		Contact contact= new Contact()
		contact.firstname="John"
		contact.title="Mr"
		contact.surname="Doe"
		contact.fonction="Manager"
		contact.phone="123456789"
		contact.mobile="064578932"
		contact.contactType="Director"
		contact.email="john.doe@acme.com"

		contact
	}

	def aContract(){
		Contract contract= new Contract()
		contract.contractAmount=12453

		Instant instantOfNow = Instant.now();
		LocalDate localDate = LocalDateTime.ofInstant(instantOfNow, ZoneOffset.UTC).toLocalDate();

		contract.contractDate = localDate
		contract.contractName ="Contract name"
		contract.dateCreation=LocalDateTime.ofInstant(instantOfNow, ZoneOffset.UTC)
		
		contract
	}

	def aBankAccountDetails(){
		BankAccountDetails bank = new BankAccountDetails()
		bank.bankName = "BNP"
		bank.accountName = "BNP account"
		bank.accountNumber = "1234 5678 9101"
		bank.bankSwift = "SWIFT 1234 5678 9101"

		bank
	}
}
