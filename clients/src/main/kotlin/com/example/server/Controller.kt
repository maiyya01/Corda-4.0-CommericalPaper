package com.example.server

import com.example.flow.FlowInitiator
import com.example.flow.TradingFlowInitiator
import com.example.state.CommercialPaperState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

//val SERVICE_NAMES = listOf("Notary", "Network Map Service", "Oracle")


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun whoami() = mapOf("me" to myLegalName)


    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (listOf("Notary", "Network Map Service", "Oracle") + myLegalName.organisation) })
    }

    /**
     * Displays all Commercial Ihat exist in the node's vault.
     */
    @GetMapping(value = [ "commercialPapers" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCommercialPaper() : ResponseEntity<List<StateAndRef<CommercialPaperState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<CommercialPaperState>().states.filter{it.state.data.participants.contains(proxy.nodeInfo().legalIdentities.first())})
    }

    /**
     * Displays all historical Commercial Ihat exist in the node's vault.
     */
    @GetMapping(value = [ "historycommercialPapers" ], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getHistoricalCommercialPaper() : ResponseEntity<List<SignedTransaction>> {
        return ResponseEntity.ok(proxy.internalVerifiedTransactionsSnapshot())
    }

    /**
     * Post Commerical Paper to in the node's vault.
     */
    @PostMapping(value = [ "trade-commercialPapers" ])
    fun tradedCommercialPaper(request: HttpServletRequest): ResponseEntity<String>
    {
        val paperReference : String  = "a48b6dad-b1ec-4b23-be23-c120f6189c64"

        return try {
            val signedTx = proxy.startTrackedFlow(::TradingFlowInitiator,paperReference).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
            //proxy.startTrackedFlow(::AcceptanceFlow,paperReference).returnValue.getOrThrow()
            //ResponseEntity.status(HttpStatus.CREATED).body("Good")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Post Commerical Paper to in the node's vault.
     */
    @PostMapping(value = [ "post-commercialPapers" ])
    fun postCommercialPaper(request: HttpServletRequest): ResponseEntity<String>
    {
        val paperNumber = request.getParameter("paperNumber").toInt()
        val maturityDate = request.getParameter("maturityDate").toString()
        val issueDate = request.getParameter("issueDate").toString()
        val faceValue = request.getParameter("faceValue").toInt()

        return try {
            val signedTx = proxy.startTrackedFlow(::FlowInitiator, paperNumber, maturityDate,
                    issueDate, faceValue ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

}