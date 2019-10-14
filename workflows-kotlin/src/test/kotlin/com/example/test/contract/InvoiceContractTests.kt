package com.example.test.contract

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import java.time.LocalDate

class InvoiceContractTests {
    private val ledgerServices = MockServices(listOf("com.example.contract", "com.example.flow"))
    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val contractor = TestIdentity(CordaX500Name("Contractor", "New York", "US"))
    private val oracle = TestIdentity(CordaX500Name("Oracle", "New York", "US"))
    private val signers = listOf(megaCorp.publicKey, contractor.publicKey, oracle.publicKey)
    private val date = LocalDate.now()
    private val rate = 10.0
    private val invoiceValue = 1



}