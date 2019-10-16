package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.CommercialPaperContract
import com.example.state.CommercialPaperState
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.slf4j.LoggerFactory

import net.corda.core.flows.FinalityFlow as FinalityFlow1

// *********
// * Flows *
// *********


@InitiatingFlow
@StartableByRPC
class TradingFlowInitiator(
        val commercialPaperReference: String


) : FlowLogic<SignedTransaction>() {

    companion object {
        private val logger = LoggerFactory.getLogger(TradingFlowInitiator::class.java)
    }

    override fun call(): SignedTransaction
    {

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(commercialPaperReference)))
        val inputStateAndRef = serviceHub.vaultService.queryBy<CommercialPaperState>(queryCriteria).states.single()
        val input = inputStateAndRef.state.data
        val cpOutputState = input.copy(faceValue= 1000)
        val cpOutputStateAndContract = StateAndContract(cpOutputState, CommercialPaperContract.ID)
        val endAuctionCommand = Command(CommercialPaperContract.Commands.Trading(), input.issuer.owningKey)

        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // Build, sign and record the transaction.
        val utx = TransactionBuilder(notary = notary).withItems(
                cpOutputStateAndContract, // Output
                inputStateAndRef, // Input
                endAuctionCommand  // Command
        )
        val stx = serviceHub.signInitialTransaction(utx)
        val sessions = (input.participants  - ourIdentity).map { initiateFlow(it as Party) }
        // Broadcast this transaction to all parties on this business network.
        return subFlow(FinalityFlow1(stx, sessions))
    }
}
