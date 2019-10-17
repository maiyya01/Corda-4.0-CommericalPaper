package com.example.state

import com.example.contract.CommercialPaperContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * Commercial Paper State *
// *********
@BelongsToContract(CommercialPaperContract::class)
data class CommercialPaperState(
        val issuer : Party,
        val paperNumber: Int,
        val issueDateTime: String,
        val maturityDateTime: String,
        val faceValue: Int,
        val currentState: Int,
        val owner : Party,
        val creator : String,
        val price : Double ?=null,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    // Participants is a list of all the parties who should
    // be notified of the creation or consumption of this state.

   override val participants: List<AbstractParty> = listOf(issuer, owner)

}
