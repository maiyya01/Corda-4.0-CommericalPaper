package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.CommercialPaperContract
import com.example.state.CommercialPaperState
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

    @InitiatingFlow
    @StartableByRPC
    class AcceptanceFlow(val proposalId: String) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call()   {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(proposalId)))
            val inputStateAndRef = serviceHub.vaultService.queryBy<CommercialPaperState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            val output = CommercialPaperState(input.issuer, input.paperNumber, input.issueDate, input.maturityDate, 5000, "TRADING")

            // Creating the command.
            //val requiredSigners = listOf(input.proposer.owningKey, input.proposee.owningKey)
            val command = Command(CommercialPaperContract.Commands.Trading(), input.issuer.owningKey)

            // Building the transaction.
            val notary = inputStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, CommercialPaperContract.ID)
            txBuilder.addCommand(command)


            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
            val sessions = (input.participants  - ourIdentity).map { initiateFlow(it as Party) }

            val stx = subFlow(CollectSignaturesFlow(partStx, sessions))

            subFlow(FinalityFlow(stx, sessions))

            //val counterpartySession = initiateFlow(counterparty)
            //val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            //subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
        }
    }



