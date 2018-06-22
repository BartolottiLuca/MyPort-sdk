package tesi.barto.myport.Uport

import android.content.Context
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks
import me.uport.sdk.fuelingservice.FuelTokenProvider


class LoginUport(context: Context) {

    private var uportToken:String=""
    private var uportAddress:String=""
    private var uportNetwork:String=""

    init {
        if (Uport.defaultAccount == null) {

            val config = Uport.Configuration().setApplicationContext(context).setFuelTokenProvider(FuelTokenProvider(context, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
            Uport.initialize(config)

            Uport.createAccount(network = Networks.rinkeby) { err, account ->
                // update UI to reflect the existence of a defaultAccount
                if (err == null) {
                    uportToken = account.fuelToken
                    uportAddress = account.address
                    uportNetwork = account.network
                    //defaultAccountView.text = acc.toJson(true)
                } else {
                    //defaultAccountView.text = "ERROR: $err."
                }
            }
        }else{
            uportToken=Uport.defaultAccount?.fuelToken.toString()
            uportNetwork=Uport.defaultAccount?.network.toString()
            uportAddress=Uport.defaultAccount?.address.toString()
        }
    }

    fun getToken(): String{
        return Uport.defaultAccount?.fuelToken.toString()
    }

    fun setToken(token:String){
        this.uportToken=token
    }

    fun getAddressAccount():String{
        return Uport.defaultAccount?.address.toString()
    }

    fun setAddress(address:String){
        this.uportAddress=address
    }
    fun getNetwork():String{
        return Uport.defaultAccount?.network.toString()
    }

    fun setNetwork(network:String){
        this.uportNetwork=network
    }


}

    /*private fun populateConsentTextView() {
        mConsentTextView!!.text = ""
        val allDConsents = this.intent.getStringExtra(Intent.EXTRA_TEXT)
        if (allDConsents != null) {
            val st = StringTokenizer(allDConsents, System.getProperty("line.separator"))
            val count = st.countTokens()
            for (i in 0 until count) {
                mConsentTextView!!.append("• ")
                mConsentTextView!!.append(st.nextToken())
                mConsentTextView!!.append(System.getProperty("line.separator"))
                mConsentTextView!!.append(System.getProperty("line.separator"))
            }
        }
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/


