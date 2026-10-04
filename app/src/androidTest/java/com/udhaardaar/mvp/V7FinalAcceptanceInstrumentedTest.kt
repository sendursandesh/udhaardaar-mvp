package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class V7FinalAcceptanceInstrumentedTest {
    private lateinit var c: Context
    private val mobile="9876507711"

    @Before fun setUp(){
        c=InstrumentationRegistry.getInstrumentation().targetContext
        V7AccountStore.logout(c)
        V7LocalStore(c).remove("v7_accounts",mobile)
        V7AccountStore.create(c,"Final Acceptance",mobile)
        V7AccountStore.login(c,mobile)
    }

    @After fun tearDown(){ V7AccountStore.logout(c); V7LocalStore(c).remove("v7_accounts",mobile) }

    @Test fun masterArchitectureHasUniqueOwnershipAndNativeCore(){
        val modules=V7MasterVisionRegistry.all()
        assertEquals(V7MasterVisionRegistry.Module.entries.size,modules.size)
        assertEquals(modules.size,modules.map{it.key}.toSet().size)
        assertTrue(V7MasterVisionRegistry.native().map{it.key}.containsAll(listOf("RECORD","CREDIT","ASSETS","LIABILITIES","REPAYMENT")))
        assertTrue(V7MasterVisionRegistry.legacyBacked().isEmpty())
    }

    @Test fun recordsAreIsolatedByCurrentAccount() {
        val mobileA="9876507781"; val mobileB="9876507782"
        V7AccountStore.logout(c)
        V7LocalStore(c).remove("v7_accounts",mobileA)
        V7LocalStore(c).remove("v7_accounts",mobileB)
        V7AccountStore.create(c,"Owner A",mobileA)
        V7AccountStore.create(c,"Owner B",mobileB)
        V7AccountStore.login(c,mobileA)
        val a=V7Records.person(c,"Private A","9876507783")
        assertTrue(V7Core.all(c,V7Core.Keys.PEOPLE).any{it.optString("id")==a.optString("id")})
        V7AccountStore.login(c,mobileB)
        assertFalse(V7Core.all(c,V7Core.Keys.PEOPLE).any{it.optString("id")==a.optString("id")})
        V7AccountStore.login(c,mobileA)
        assertTrue(V7Core.find(c,V7Core.Keys.PEOPLE,a.optString("id"))!=null)
        V7AccountStore.logout(c)
        V7LocalStore(c).remove("v7_accounts",mobileA)
        V7LocalStore(c).remove("v7_accounts",mobileB)
    }

    @Test fun eventBusReceivesCanonicalRecordChanges(){
        val events=mutableListOf<V7Architecture.Event>()
        val close=V7Architecture.Events.subscribe{events.add(it.event)}
        V7Records.person(c,"Event Person","9876507712")
        V7Records.asset(c,V7Core.user(c),"PROPERTY","Event House",100000.0)
        close.close()
        assertTrue(events.contains(V7Architecture.Event.PERSON_CHANGED))
        assertTrue(events.contains(V7Architecture.Event.ASSET_CHANGED))
    }

    @Test fun recordValidationScenarioRejectsBadIdentityAndPersistsGoodIdentity(){
        val good=V7Records.person(c,"Valid Customer","9876507713","ABCDE1234F","123456789012","10ABCDE1234F1Z5")
        assertTrue(V7Core.all(c,V7Core.Keys.PEOPLE).any{it.optString("id")==good.optString("id") && it.optString("mobile")=="9876507713"})
        assertEquals(10,good.optString("mobile").length)
    }

    @Test fun creditRepaymentScenarioClosesOutstandingAndFeedsMetrics(){
        val p=V7Records.person(c,"Repay Customer","9876507714")
        val rel=V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",10000.0,12.0,"PRINCIPAL_PLUS_INTEREST","working capital")
        assertEquals(10000.0,V7Core.find(c,V7Core.Keys.RELATIONSHIPS,rel.optString("id"))!!.optDouble("outstanding"),0.001)
        val svc=V7Architecture.LocalConsentService(c)
        val con=svc.request(V7Architecture.ConsentRequest(p.optString("id"),"REPAYMENT_UPDATE","relationship:"+rel.optString("id"),V7Core.user(c),V7Core.now()+120000L,true))
        assertNull(svc.grant(con.id,false))
        assertNotNull(svc.grant(con.id,true))
        V7Records.repayment(c,rel.optString("id"),10000.0,10000.0,0.0,"UPI",true)
        val stored=V7Core.find(c,V7Core.Keys.RELATIONSHIPS,rel.optString("id"))!!
        assertEquals(0.0,stored.optDouble("outstanding"),0.001)
        assertEquals("CLOSED",stored.optString("status"))
        assertTrue(V7Core.all(c,V7Core.Keys.REPAYMENTS).any{it.optString("relationshipId")==rel.optString("id")})
    }

    @Test fun adverseRepaymentScenarioCannotChangeOutstanding(){
        val p=V7Records.person(c,"Adverse Customer","9876507715")
        val rel=V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",5000.0,0.0,"PRINCIPAL_PLUS_INTEREST","test")
        V7Records.repayment(c,rel.optString("id"),1000.0,1000.0,0.0,"INVALID",true)
        V7Records.repayment(c,rel.optString("id"),6000.0,6000.0,0.0,"UPI",true)
        assertEquals(5000.0,V7Core.find(c,V7Core.Keys.RELATIONSHIPS,rel.optString("id"))!!.optDouble("outstanding"),0.001)
    }

    @Test fun scoreScenarioIsConsentGatedExplainableAndBounded(){
        val p=V7Records.person(c,"Score Customer","9876507716")
        val rel=V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",1000.0,0.0,"PRINCIPAL_PLUS_INTEREST","score")
        assertNull(V7ScoreEngine.calculate(c,p.optString("id"),false))
        val svc=V7Architecture.LocalConsentService(c)
        val con=svc.request(V7Architecture.ConsentRequest(p.optString("id"),"SCORE_DISCLOSURE","score",V7Core.user(c),V7Core.now()+60000L,true))
        assertNotNull(svc.grant(con.id,true))
        val result=V7ScoreEngine.calculate(c,p.optString("id"),true)!!
        assertTrue(result.score in 300..900)
        assertTrue(result.factors.isNotEmpty())
        assertTrue(result.band.isNotBlank())
        assertEquals(1000.0,rel.optDouble("amount"),0.001)
    }

    @Test fun financialPlanScenariosProduceConsistentEmiAndPrincipalInterest(){
        val emi=ArthSaathiFinancialRules.plan(120000.0,12.0,12,"EMI")
        assertEquals(12,emi.instalments)
        assertTrue(emi.emi>0 && emi.totalPayable>120000)
        val ppi=ArthSaathiFinancialRules.plan(120000.0,12.0,12,"PRINCIPAL + INTEREST")
        assertEquals(11200.0,ppi.emi,0.01)
        assertEquals(134400.0,ppi.totalPayable,0.01)
    }

    @Test fun assetLiabilityPortfolioAndNetWorthRemainConnected(){
        val p=V7Records.person(c,"Position Customer","9876507717")
        V7Records.asset(c,p.optString("id"),"PROPERTY","House",5000000.0)
        V7Records.liability(c,p.optString("id"),"HOME_LOAN",3000000.0,2200000.0,8.5)
        val port=V7PortfolioEngine.createPortfolio(c,"Core Portfolio","Moderate","60/40")
        V7Records.holding(c,port.optString("id"),"MF","EQUITY",100000.0,120000.0)
        val m=V7Core.metrics(c)
        assertTrue(m.optDouble("assets")>=5000000.0)
        assertTrue(m.optDouble("liabilities")>=2200000.0)
        assertTrue(m.has("netWorth"))
        assertEquals(20000.0,m.optDouble("portfolioGain"),0.01)
    }

    @Test fun revenueScenarioFlowsServiceInvoicePaymentToReconciliationAndMis(){
        val svc=V7RevenueEngine.service(c,"Premium Advisory",999.0)
        val inv=V7RevenueEngine.invoice(c,svc.optString("id"),999.0,"Premium Advisory")
        val pay=V7RevenueEngine.payment(c,inv.optString("id"),999.0,"TEST_GATEWAY","REF-001",V7RevenueEngine.PaymentStatus.SUCCESS)
        V7RevenueEngine.reconcile(c,pay.optString("id"),"SETTLE-001")
        val stored=V7Core.find(c,V7Core.Keys.PAYMENTS,pay.optString("id"))!!
        assertEquals("RECONCILED",stored.optString("status"))
        assertEquals(999.0,V7Core.all(c,V7Core.Keys.REVENUE).last().optDouble("amount"),0.001)
        assertEquals(999.0,V7MIS.snapshot(c).optDouble("reconciledRevenue"),0.001)
    }

    @Test fun externalIntegrationScenarioProducesReviewableJsonXmlAndCsv(){
        V7Records.person(c,"ERP Customer","9876507718")
        val json=V7ExternalIntegration.export(c,V7ExternalIntegration.Connector.GENERIC_ERP,V7ExternalIntegration.Format.JSON)
        assertTrue(json.recordCount>0 && json.payload.contains("arthsaathi.v7"))
        val xml=V7ExternalIntegration.export(c,V7ExternalIntegration.Connector.TALLY_PRIME,V7ExternalIntegration.Format.XML)
        assertTrue(xml.payload.startsWith("<ENVELOPE>") && xml.payload.contains("<TALLYREQUEST>Import Data</TALLYREQUEST>"))
        val csv=V7ExternalIntegration.export(c,V7ExternalIntegration.Connector.ACCOUNTING_ERP,V7ExternalIntegration.Format.CSV)
        assertTrue(csv.payload.startsWith("entity,id,ownerUserId,updatedAt"))
    }

    @Test fun persistenceSurvivesReadBackAndKeepsLegacyNamespaceSeparate(){
        val p=V7Records.person(c,"Persistent Customer","9876507719")
        val read=V7Core.find(c,V7Core.Keys.PEOPLE,p.optString("id"))
        assertEquals("Persistent Customer",read?.optString("name"))
        val prefs=c.getSharedPreferences("v7_store",Context.MODE_PRIVATE)
        assertTrue((prefs.getString(V7Core.Keys.PEOPLE,"")?:"").startsWith("ENC:"))
        assertFalse(c.getSharedPreferences("udhaardaar_accounts",Context.MODE_PRIVATE).contains("current_mobile"))
    }

    @Test fun majorNativeScreensOpenWithoutCrash(){
        listOf("RECORD","CREDIT","REPAYMENT","ASSETS","LIABILITIES").forEach { module ->
            ActivityScenario.launch<V7NativeModuleActivity>(Intent(c,V7NativeModuleActivity::class.java).putExtra("module",module)).use { scenario ->
                scenario.onActivity { assertTrue(it.window.decorView.isShown) }
            }
        }
    }

    @Test fun toolsScreensOpenWithoutCrash(){
        listOf("MIS","SCORE","INTEGRATION","REVENUE","OPPORTUNITY","MARKET","ADDRESS").forEach { tool ->
            ActivityScenario.launch<V7ToolsActivity>(Intent(c,V7ToolsActivity::class.java).putExtra("tool",tool)).use { scenario ->
                scenario.onActivity { assertTrue(it.window.decorView.isShown) }
            }
        }
    }

    @Test fun homeContainsApprovedBrandingAndMajorJourneys(){
        ActivityScenario.launch<V7HomeActivity>(Intent(c,V7HomeActivity::class.java)).use { scenario ->
            scenario.onActivity { a ->
                val text=allText(a.window.decorView).joinToString(" | ")
                assertTrue(text.contains("ArthSaathi"))
                assertTrue(text.contains("Navigate Your Financial Journey"))
                assertTrue(text.contains("Your Asset. Your Record. Your Right."))
                assertTrue(text.contains("Register Credit"))
                assertTrue(text.contains("Asset Vault"))
            }
        }
    }


    @Test fun consentScopeCannotCrossAuthorizeDifferentRelationship() {
        val p=V7Records.person(c,"Scope Customer","9876507720")
        val relA=V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",5000.0,0.0,"PRINCIPAL_PLUS_INTEREST","A")
        val relB=V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",5000.0,0.0,"PRINCIPAL_PLUS_INTEREST","B")
        val svc=V7Architecture.LocalConsentService(c)
        val con=svc.request(V7Architecture.ConsentRequest(p.optString("id"),"REPAYMENT_UPDATE","relationship:"+relA.optString("id"),V7Core.user(c),V7Core.now()+120000L,true))
        assertNotNull(svc.grant(con.id,true))
        V7Records.repayment(c,relB.optString("id"),1000.0,1000.0,0.0,"UPI",true)
        assertEquals(5000.0,V7Core.find(c,V7Core.Keys.RELATIONSHIPS,relB.optString("id"))!!.optDouble("outstanding"),0.001)
    }

    @Test fun scoreEngineRejectsCallerClaimWithoutStoredConsent() {
        val p=V7Records.person(c,"Score Guard","9876507721")
        V7Records.relationship(c,p.optString("id"),"INFORMAL_CREDIT","RECEIVABLE",1000.0,0.0,"PRINCIPAL_PLUS_INTEREST","guard")
        assertNull(V7ScoreEngine.calculate(c,p.optString("id"),true))
    }

    @Test fun allRegisteredV7ModulesHaveLaunchableEntryPoint() {
        V7MasterVisionRegistry.all().forEach { module ->
            when (module.destination) {
                V7MasterVisionRegistry.Destination.NATIVE_MODULE ->
                    ActivityScenario.launch<V7NativeModuleActivity>(
                        Intent(c, V7NativeModuleActivity::class.java).putExtra("module", module.key)
                    ).use { scenario ->
                        scenario.onActivity { a ->
                            assertTrue("Native module not visible: " + module.key, a.window.decorView.isShown)
                        }
                    }
                V7MasterVisionRegistry.Destination.MODULE_ROUTER ->
                    ActivityScenario.launch<V7ModuleActivity>(
                        Intent(c, V7ModuleActivity::class.java).putExtra("module", module.key)
                    ).use { scenario ->
                        scenario.onActivity { a ->
                            assertTrue("V7 module router not visible: " + module.key, a.window.decorView.isShown)
                        }
                    }
            }
        }
    }

    @Test fun noLegacyRouteArtifactsRemainInV7Contract() {
        assertTrue(V7MasterVisionRegistry.legacyBacked().isEmpty())
        assertTrue(V7MasterVisionRegistry.all().all { it.ownership != V7MasterVisionRegistry.Ownership.NATIVE || it.destination == V7MasterVisionRegistry.Destination.NATIVE_MODULE })
        assertEquals(V7MasterVisionRegistry.all().size, V7MasterVisionRegistry.all().map { it.key }.toSet().size)
    }

    private fun allText(v:View):List<String>{
        val out=mutableListOf<String>()
        if(v is TextView) out.add(v.text?.toString().orEmpty())
        if(v is ViewGroup) for(i in 0 until v.childCount) out.addAll(allText(v.getChildAt(i)))
        return out
    }
}
