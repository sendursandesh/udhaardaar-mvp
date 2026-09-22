package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V7ToolsActivity : AppCompatActivity() {
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private fun input(h:String)=ArthSaathiV7Design.input(this,h)
    override fun onCreate(b:Bundle?){super.onCreate(b);render(intent.getStringExtra("tool")?:"PORTFOLIO")}
    private fun shell(title:String,sub:String):LinearLayout{
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(7),dp(10),dp(22));setBackgroundColor(ArthSaathiV7Design.CREAM)}
        val h=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=ArthSaathiV7Design.bg(this@V7ToolsActivity);setPadding(dp(10),dp(8),dp(10),dp(12))}
        h.addView(ArthSaathiV7Design.masthead(this));h.addView(ArthSaathiV7Design.text(this,title,21f,android.graphics.Color.WHITE,true));h.addView(ArthSaathiV7Design.text(this,sub,10.5f,ArthSaathiV7Design.GOLD_PALE))
        r.addView(h);return r
    }
    private fun render(tool:String){
        val r=when(tool){"PORTFOLIO"->portfolio();"OPPORTUNITY"->opportunity();"MARKET"->market();"ADDRESS"->address();"REVENUE"->revenue();"ADVOCATE"->advocate();"CLAIM"->claim();"AI"->ai();else->portfolio()}
        r.addView(ArthSaathiV7Design.goldButton(this,"Back"){finish()},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(12)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun portfolio():LinearLayout{
        val r=shell("Portfolio Intelligence","See what you own, how it is performing and what alternatives may mean.")
        val name=input("Portfolio name");val risk=input("Risk profile (Conservative / Moderate / Growth)");val constitution=input("Portfolio constitution / allocation")
        r.addView(ArthSaathiV7Design.section(this,"Portfolio Record","Portfolio feeds MIS, scenarios and opportunity-cost analysis."))
        listOf(name,risk,constitution).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Portfolio"){
            if(name.text.isBlank()){name.error="Required";return@goldButton}
            V7PortfolioEngine.createPortfolio(this,name.text.toString(),risk.text.toString(),constitution.text.toString());Toast.makeText(this,"Portfolio recorded.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Run Opportunity-Cost Scenario"){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool","OPPORTUNITY"))},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Record Market Data Snapshot"){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool","MARKET"))},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(7)})
        return r
    }
    private fun opportunity():LinearLayout{
        val r=shell("Opportunity Cost","Compare a recorded position with an alternative. Analysis only; no transaction is executed.")
        val amount=input("Current amount");val cr=input("Current annual return %");val ar=input("Alternative annual return %");val years=input("Period in years");val exit=input("Exit cost");val tax=input("Estimated tax cost")
        listOf(amount,cr,ar,years,exit,tax).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val out=ArthSaathiV7Design.text(this,"Enter assumptions and calculate.",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Calculate Opportunity Cost"){
            val x=V7PortfolioEngine.opportunityCost(amount.text.toString().toDoubleOrNull()?:0.0,cr.text.toString().toDoubleOrNull()?:0.0,ar.text.toString().toDoubleOrNull()?:0.0,years.text.toString().toDoubleOrNull()?:0.0,0.0,exit.text.toString().toDoubleOrNull()?:0.0,tax.text.toString().toDoubleOrNull()?:0.0)
            out.text="Current projected: ₹%.2f\nAlternative projected: ₹%.2f\nGross opportunity difference: ₹%.2f\nEstimated costs: ₹%.2f\nNet difference: ₹%.2f".format(x.getDouble("currentProjectedValue"),x.getDouble("alternativeProjectedValue"),x.getDouble("grossOpportunityCost"),x.getDouble("estimatedCosts"),x.getDouble("netOpportunityDifference"))
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)});return r
    }
    private fun market():LinearLayout{
        val r=shell("Market Data","Every market value is stored with source, timestamp and freshness.")
        val i=input("Instrument / product");val s=input("Source");val v=input("Observed value");val f=input("Freshness in minutes")
        listOf(i,s,v,f).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Market Snapshot"){
            val value=v.text.toString().toDoubleOrNull();if(i.text.isBlank()||s.text.isBlank()||value==null){Toast.makeText(this,"Instrument, source and value are required.",Toast.LENGTH_SHORT).show();return@goldButton}
            V7PortfolioEngine.recordMarketData(this,i.text.toString(),s.text.toString(),value,V7Core.now(),f.text.toString().toIntOrNull()?:60);Toast.makeText(this,"Market snapshot saved.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)});return r
    }
    private fun address():LinearLayout{
        val r=shell("Address & Location","PIN-assisted and map-assisted capture. Suggestions always require user confirmation.")
        val owner=input("Person / business ID");val label=input("Address label (Current / Permanent / Business)");val addr=input("Address");val pin=input("PIN code");val city=input("City / Town");val district=input("District");val state=input("State")
        listOf(owner,label,addr,pin,city,district,state).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})}
        val status=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.MUTED)
        r.addView(ArthSaathiV7Design.goldButton(this,"Validate PIN"){status.text=V7LocationEngine.resolvePin(pin.text.toString()).message},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Open Map to Select / Confirm"){try{startActivity(V7LocationEngine.mapIntent(this,addr.text.toString()))}catch(_:Exception){Toast.makeText(this,"No map application is available.",Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(6)})
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Address"){
            if(!V7LocationEngine.validatePin(pin.text.toString())){pin.error="Invalid PIN";return@goldButton}
            V7Records.address(this,owner.text.toString(),label.text.toString(),addr.text.toString(),pin.text.toString(),state.text.toString(),district.text.toString(),city.text.toString(),"USER_CONFIRMED");Toast.makeText(this,"Address saved.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(6)});r.addView(status,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});return r
    }
    private fun revenue():LinearLayout{
        val r=shell("Revenue & Payments","Service catalogue, invoice, configurable gateway record, payment status and reconciliation.")
        val service=input("Service name");val price=input("Price (₹)");val gateway=input("Gateway name");val ref=input("Payment reference")
        listOf(service,price,gateway,ref).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val status=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Service + Invoice"){
            val amount=price.text.toString().toDoubleOrNull();if(service.text.isBlank()||amount==null){Toast.makeText(this,"Service and price required.",Toast.LENGTH_SHORT).show();return@goldButton}
            val svc=V7RevenueEngine.service(this,service.text.toString(),amount);val inv=V7RevenueEngine.invoice(this,svc.getString("id"),amount,service.text.toString())
            status.text="Invoice "+inv.getString("id")+" created for ₹%.2f.".format(amount)
            val pay=V7RevenueEngine.payment(this,inv.getString("id"),amount,gateway.text.toString().ifBlank{"CONFIGURED_GATEWAY"},ref.text.toString(),V7RevenueEngine.PaymentStatus.CREATED)
            status.append("\nPayment record: "+pay.getString("id"))
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Mark Last Payment Successful"){
            val last=V7Core.all(this,V7Core.Keys.PAYMENTS).lastOrNull()?:return@outlineButton
            last.put("status",V7RevenueEngine.PaymentStatus.SUCCESS.name);V7Core.replace(this,V7Core.Keys.PAYMENTS,last);status.text="Payment marked SUCCESS. Receipt/reconciliation can now follow."
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(6)});r.addView(status,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)});return r
    }
    private fun advocate():LinearLayout{
        val r=shell("Advocate Directory","Search by city, domain, court, language and consultation mode. No arbitrary best/worst ranking.")
        val n=input("Name");val city=input("City");val state=input("State");val domain=input("Practice domain");val court=input("Court");val lang=input("Language");val mode=input("Consultation mode")
        listOf(n,city,state,domain,court,lang,mode).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})}
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Add Professional Profile"){
            if(n.text.isBlank()){n.error="Required";return@goldButton};V7LegalEngine.professional(this,n.text.toString(),city.text.toString(),state.text.toString(),domain.text.toString(),court.text.toString(),lang.text.toString(),mode.text.toString(),"USER_SUBMITTED");out.text="Professional profile recorded."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)});r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(9)});return r
    }
    private fun claim():LinearLayout{
        val r=shell("Claim Assistance","Prepare the evidence path from ownership to claim closure.")
        val asset=input("Asset ID");val claimant=input("Claimant");val nominee=input("Nominee / Heir");val institution=input("Institution");val amount=input("Claim amount")
        listOf(asset,claimant,nominee,institution,amount).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Claim Record"){V7LegalEngine.claim(this,asset.text.toString(),claimant.text.toString(),nominee.text.toString(),institution.text.toString(),amount.text.toString().toDoubleOrNull()?:0.0);Toast.makeText(this,"Claim record created.",Toast.LENGTH_SHORT).show()},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)});return r
    }
    private fun ai():LinearLayout{
        val r=shell("Ask ArthSaathi","Ask about recorded financial information, analytics and documented scenarios. No silent execution.")
        val q=input("Ask your financial question");val out=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY);r.addView(q)
        r.addView(ArthSaathiV7Design.goldButton(this,"Ask ArthSaathi"){
            val m=V7Core.metrics(this);out.text=when{
                q.text.contains("asset",true)->"Recorded current assets: ₹%.2f".format(m.optDouble("assets"))
                q.text.contains("liabil",true)->"Recorded liabilities: ₹%.2f".format(m.optDouble("liabilities"))
                q.text.contains("net worth",true)->"Recorded net position: ₹%.2f".format(m.optDouble("netWorth"))
                q.text.contains("credit",true)->"Active money relationships: "+m.optInt("activeCredits")
                q.text.contains("portfolio",true)->"Recorded portfolio value: ₹%.2f; gain/loss: ₹%.2f".format(m.optDouble("portfolioValue"),m.optDouble("portfolioGain"))
                else->"I can answer from your recorded ArthSaathi data: assets, liabilities, portfolio, credits, documents, protection and claims."
            }
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)});r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)});return r
    }
}