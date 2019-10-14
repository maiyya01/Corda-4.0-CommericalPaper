package com.example.contract


import com.example.state.CommercialPaperState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

// ************
// * Contract *
// ************
class CommercialPaperContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.example.contract.CommercialPaperContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        // Issue a new EUC.
        class Issue : Commands
        class Trading : TypeOnlyCommandData(), Commands
        class Redeemed : TypeOnlyCommandData(), Commands
    }


    override fun verify(tx: LedgerTransaction) {
//        val issuerCommand = tx.commands.requireSingleCommand<Commands>()
//        val setOfSigners = issuerCommand.signers.toSet()
//
//        when (issuerCommand.value) {
//            is Commands.Start -> verifyStart(tx, setOfSigners)
//            is Commands.End -> verifyEnd(tx, setOfSigners)
//            is Commands.AcceptBid -> verifyBid(tx, setOfSigners)
//            else -> throw IllegalArgumentException("Unrecognised command.")
//        }
    }

    private fun verifyStart(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {

    }

    private fun verifyEnd(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {

    }

    private fun verifyBid(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {

    }


}