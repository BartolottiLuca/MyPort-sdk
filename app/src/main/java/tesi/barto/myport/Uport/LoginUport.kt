package tesi.barto.myport.Uport

import android.content.Context
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.Transactions
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks
import me.uport.sdk.did.UportRegistry
import me.uport.sdk.extensions.awaitConfirmation
import me.uport.sdk.extensions.send
import me.uport.sdk.extensions.waitForTransactionToMine
import me.uport.sdk.fuelingservice.FuelTokenProvider
import me.uport.sdk.identity.Account
import me.uport.sdk.identity.SignerType
import me.uport.sdk.jsonrpc.JsonRPC
import tesi.barto.myport.activities.MainActivity


class LoginUport () {
    private var con:Context= MainActivity.getInstance()
    private var uportToken:String = ""
    private var uportSigner:SignerType = SignerType.MetaIdentityManager
    private var uportNetwork:String = ""
    private var uportHandle:String = ""
    private var uportDevice:String = ""
    private var uportProxy:String = ""
    private var uportIdentity:String = ""
    private var uportTX:String = ""
    private var uportA:Account= Account.blank
    private var exception:Exception?= Exception()
    private var receipit:JsonRPC.TransactionReceipt? =null
    //private var con =context

    constructor(context: Context):this(){
        if (Uport.defaultAccount == null) {
            val config = Uport.Configuration().setApplicationContext(context.applicationContext).setFuelTokenProvider(FuelTokenProvider(context.applicationContext, "2ouNYyHP1yLjfVM4mVdYKwx3jGEUgpQHBya"))
            Uport.initialize(config)

            Uport.createAccount(network = Networks.rinkeby) { err, account ->
                // update UI to reflect the existence of a defaultAccount
                if (err == null) {
                    uportToken = account.fuelToken
                    uportSigner = account.signerType
                    uportNetwork = account.network
                    uportHandle = account.handle
                    uportDevice = account.deviceAddress
                    uportProxy = account.proxyAddress
                    uportIdentity = account.identityManagerAddress
                    uportTX = account.txRelayAddress

                    var uportAddress:String = account.address
                    //defaultAccountView.text = acc.toJson(true)
                    //uportA.copy(uportHandle,uportDevice,uportNetwork,uportProxy,uportIdentity,uportTX,uportToken,uportSigner)
                    uportA= Account(uportHandle,uportDevice,uportNetwork,uportProxy,uportIdentity,uportTX,uportToken,uportSigner)
                } else {
                    uportToken="ERROR: $err."
                    //defaultAccountView.text = "ERROR: $err."
                }
            }
        }else{
            uportToken = Uport.defaultAccount?.fuelToken as String
            uportSigner = Uport.defaultAccount?.signerType as SignerType
            uportNetwork = Uport.defaultAccount?.network as String
            uportHandle = Uport.defaultAccount?.handle as String
            uportDevice = Uport.defaultAccount?.deviceAddress as String
            uportProxy = Uport.defaultAccount?.proxyAddress as String
            uportIdentity = Uport.defaultAccount?.identityManagerAddress as String
            uportTX = Uport.defaultAccount?.txRelayAddress as String
            uportA= Account(uportHandle,uportDevice,uportNetwork,uportProxy,uportIdentity,uportTX,uportToken,uportSigner)
        }
    }

    constructor(context: Context, accountJson: String):this(){
        if (Uport.defaultAccount == null) {

            val config = Uport.Configuration().setApplicationContext(context.applicationContext).setFuelTokenProvider(FuelTokenProvider(context.applicationContext, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
            Uport.initialize(config)
            uportA= (Account.fromJson(accountJson) ?: Account.blank )
        }else{
            uportToken = Uport.defaultAccount?.fuelToken as String
            uportSigner = Uport.defaultAccount?.signerType as SignerType
            uportNetwork = Uport.defaultAccount?.network as String
            uportHandle = Uport.defaultAccount?.handle as String
            uportDevice = Uport.defaultAccount?.deviceAddress as String
            uportProxy = Uport.defaultAccount?.proxyAddress as String
            uportIdentity = Uport.defaultAccount?.identityManagerAddress as String
            uportTX = Uport.defaultAccount?.txRelayAddress as String
            uportA= Account(uportHandle,uportDevice,uportNetwork,uportProxy,uportIdentity,uportTX,uportToken,uportSigner)
        }
    }

    constructor(context: Context, account: Account):this(){
        if (Uport.defaultAccount == null) {

            val config = Uport.Configuration().setApplicationContext(context.applicationContext).setFuelTokenProvider(FuelTokenProvider(context.applicationContext, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
            Uport.initialize(config)
            uportA= account
        }else{
            uportToken = Uport.defaultAccount?.fuelToken as String
            uportSigner = Uport.defaultAccount?.signerType as SignerType
            uportNetwork = Uport.defaultAccount?.network as String
            uportHandle = Uport.defaultAccount?.handle as String
            uportDevice = Uport.defaultAccount?.deviceAddress as String
            uportProxy = Uport.defaultAccount?.proxyAddress as String
            uportIdentity = Uport.defaultAccount?.identityManagerAddress as String
            uportTX = Uport.defaultAccount?.txRelayAddress as String
            uportA= Account(uportHandle,uportDevice,uportNetwork,uportProxy,uportIdentity,uportTX,uportToken,uportSigner)
        }
    }

    fun getAccount():Account?{
        return uportA
    }


    fun sendTransaction(){
            uportA.send(con.applicationContext,uportTX,"transazione_prova".toByteArray()){ err, txHash ->
                // Update UI to indicate that transaction has been sent and is confirming
                Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                    // Complete operation in UX
                    this.receipit=receipt
                    var contract:String? = this.receipit?.contractAddress
                }

            }
    }

    fun GetReceipit() :JsonRPC.TransactionReceipt? {
        return this.receipit
    }
}