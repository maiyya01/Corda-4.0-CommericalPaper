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


//    @Suspendable
//    override fun call() : SignedTransaction {
//
//        // Initiator flow logic goes here.
//        val notary = serviceHub.networkMapCache.notaryIdentities.first()
//
//        // Stage 1.
//        logger.info("1");
//
//        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(commercialPaperReference)))
//
//        logger.info("2");
//        val commercialPaperSateAndRef = serviceHub.vaultService.queryBy<CommercialPaperState>(queryCriteria).states.single()
//
//        // Get the CommercialPaper state corresponding to the provided ID from our vault.
//
//        //val commercialPaperSateAndRef = serviceHub.vaultService.queryBy<CommercialPaperState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(linearId.id = commercialPaperReference)))).states.single()
//        val commercialPaperInputState = commercialPaperSateAndRef.state.data
//
//        logger.info("3" + commercialPaperInputState );
//
//        val commercialPaperOutputState = commercialPaperInputState.copy(faceValue=1000)
//
//        logger.info("42");
//
//        val commercialPaperOutputStateAndContract = StateAndContract(commercialPaperOutputState, CommercialPaperContract.ID)
//
//        logger.info("5");
//        val tradingCommercialPaperCommand = Command(CommercialPaperContract.Commands.Trading(),
//                serviceHub.myInfo.legalIdentities[0].owningKey)
//
//        logger.info("6");
//
//        // Build, sign and record the transaction.
//        val utx = TransactionBuilder(notary = notary).withItems(
//                commercialPaperOutputStateAndContract, // Output
//                commercialPaperSateAndRef, // Input
//                tradingCommercialPaperCommand  // Command
//        )
//
//        logger.info("7");
//
//        val signedTx = serviceHub.signInitialTransaction(utx)
//        val sessions = (commercialPaperInputState.participants  - ourIdentity).map { initiateFlow(it as Party) }
//
//        logger.info("8");
//        return subFlow(FinalityFlow1(signedTx, sessions))
//    }

//    @Suspendable
//    override fun call(): SignedTransaction {
//        // Pick a notary. Don't care which one.
//        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
//
//        // Stage 1.
//
//        // Get the Auction state corresponding to the provided ID from our vault.
//        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(commercialPaperReference)))
//        val auctionInputStateAndRef = serviceHub.vaultService.queryBy<CommercialPaperState>(queryCriteria).states.single()
//        val auctionState = auctionInputStateAndRef.state.data
//        val auctionOutputState = auctionState.copy(faceValue= 100)
//
//        val auctionOutputStateAndContract = StateAndContract(auctionOutputState, CommercialPaperContract.ID)
//
//        val endAuctionCommand = Command(CommercialPaperContract.Commands.Trading(), auctionState.issuer.owningKey)
//
//        // Build, sign and record the transaction.
//        val utx = TransactionBuilder(notary = notary).withItems(
//                auctionOutputStateAndContract, // Output
//                auctionInputStateAndRef, // Input
//                endAuctionCommand  // Command
//        )
//        val stx = serviceHub.signInitialTransaction(utx)
//        val sessions = (auctionState.participants  - ourIdentity).map { initiateFlow(it as Party) }
//
//        // Broadcast this transaction to all parties on this business network.
//        return subFlow(FinalityFlow1(stx, sessions))
//
//        //return ftx
//    }

    override fun call(): SignedTransaction {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Stage 1.
        //progressTracker.currentStep = GENERATING_TRANSACTION

        val command = Command(CommercialPaperContract.Commands.Issue(), listOf(ourIdentity.owningKey))

        val commercialPaperState = CommercialPaperState(
                serviceHub.myInfo.legalIdentities.first(),
                111,
                "20190101",
                "20190101",
                10000, "TRADING")


        val txBuilder = TransactionBuilder(notary)
                .addOutputState(commercialPaperState, CommercialPaperContract.ID)
                .addCommand(command)

        txBuilder.verify(serviceHub)
        val tx = serviceHub.signInitialTransaction(txBuilder)

        val sessions = (commercialPaperState.participants - ourIdentity).map { initiateFlow(it as Party) }
        val stx = subFlow(CollectSignaturesFlow(tx, sessions))
        return subFlow(FinalityFlow1(stx, sessions))
    }
}

//@InitiatedBy(TradingFlowInitiator::class)
//class TradingFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
//
//    @Suspendable
//    override fun call() : SignedTransaction  {
//        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession)
//        {
//            override fun checkTransaction(stx: SignedTransaction) = requireThat {
//                val output = stx.tx.outputs.single().data
//                "The output must be a CommercialPaperState" using (output is CommercialPaperState)
//            }
//        }
//        val txId = subFlow(signedTransactionFlow).id
//
//        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
//    }
//}