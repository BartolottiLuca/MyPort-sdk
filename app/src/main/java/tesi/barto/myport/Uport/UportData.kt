package tesi.barto.myport.Uport

import android.content.Context
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks
import me.uport.sdk.extensions.*
import me.uport.sdk.fuelingservice.FuelTokenProvider
import me.uport.sdk.identity.Account
import me.uport.sdk.jsonrpc.JsonRPC
import tesi.barto.myport.activities.MainActivity
import java.lang.reflect.Method

class UportData () {
    private var context: Context = MainActivity.getInstance()
    private var uportError: String = ""
    private var uportA: Account = Account.blank
    private var accountSetted: Boolean = false
    private var receipit: JsonRPC.TransactionReceipt? = null
    private var receiptSetted: Boolean = false
    private var job: Job? = null

    constructor(context: Context) : this() {
        this.context = context
        if (Uport.defaultAccount == null) {
            val config = Uport.Configuration().setApplicationContext(this.context).setFuelTokenProvider(FuelTokenProvider(this.context, "2ouNYyHP1yLjfVM4mVdYKwx3jGEUgpQHBya"))
            Uport.initialize(config)
            Uport.createAccount(network = Networks.rinkeby) { err, account ->
                // update UI to reflect the existence of a defaultAccount
                if (err == null) {
                    this.setAccount(account)
                    (this.context as MainActivity).setEnterButtonClickable(true)
                } else {
                    uportError = "ERROR: $err."
                }
            }
        } else {
            this.setAccount(Uport.defaultAccount as Account)
        }
    }

    constructor(applicationContext: Context, accountJson: String) : this() {
        this.context = applicationContext
        if (Uport.defaultAccount == null) {
            val config = Uport.Configuration().setApplicationContext(context).setFuelTokenProvider(FuelTokenProvider(context, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
            Uport.initialize(config)
            UportData(context,Account.fromJson(accountJson) ?: Account.blank)
        } else {
            this.setAccount(Uport.defaultAccount as Account)
        }
    }

    constructor(applicationContext: Context, account: Account) : this() { //non serve, ma l'ho fatto e lo lascio qua
        this.context = applicationContext
        if (Uport.defaultAccount == null) {
            this.setAccount(account)
        } else {
            this.setAccount(Uport.defaultAccount as Account)
        }
    }

    fun getAccount(): Account? {
        if (accountSetted)
            return uportA
        return null
    }

    private fun setAccount(acc: Account) {
        this.uportA = acc
        this.accountSetted = true
    }

    fun getReceipt(): JsonRPC.TransactionReceipt? {
        if (this.receiptSetted)
            return this.receipit
        return null //Severo ma Giusto
    }

    private fun setReceipt(rec: JsonRPC.TransactionReceipt) {
        this.receipit = rec
        this.receiptSetted = true
    }

    fun sendTransaction(callingclass:Context,transactionString:String, onSuccess: Method, onFailure: Method) {
        launch { launch {
            if (accountSetted && (job==null || (job as Job).isCompleted)) {
                var str = transactionString
                receipit = null
                receiptSetted = false
                uportA.send(this@UportData.context, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                setReceipt(receipt)
                                onSuccess.invoke(callingclass)
                                /*
                                UserProfileActivity.setButtonClickable("DisableButton", true)
                                UserProfileActivity.setButtonClickable("WithdrawButton", true)
                                */
                            } else {
                                uportError = "" + err
                                onFailure.invoke(callingclass)
                                //UserProfileActivity.setButtonClickable("ServiceButton", true)
                            }
                        }
                    } else {
                        uportError = "" + err
                        onFailure.invoke(callingclass)
                        // UserProfileActivity.setButtonClickable("ServiceButton", true)
                    }
                }.join()
            } else {
                receipit = null
                receiptSetted = false
                onFailure.invoke(callingclass)
                //UserProfileActivity.setButtonClickable("ServiceButton", true)
            }
        } }
    }

}


/*

    fun addService(controller: IController, service: IService, newAccountActivity: NewAccountActivity) {
        launch { launch {
               if (accountSetted && (job==null || (job as Job).isCompleted)) {
                   var str = "Confermare servizio: " + service.toString()
                   receipit = null
                   receiptSetted = false
                   uportA.send(this@UportData.context, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                       if (err == null) {
                           job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                               if (err == null) {
                                   setReceipt(receipt)
                                   //newAccountActivity.onConfirmedService(controller, service)
                                   */
/*
                                   UserProfileActivity.setButtonClickable("DisableButton", true)
                                   UserProfileActivity.setButtonClickable("WithdrawButton", true)
                                   *//*

                               } else {
                                   uportError = "" + err
                                   newAccountActivity.onFailedService()
                                   //UserProfileActivity.setButtonClickable("ServiceButton", true)
                               }
                           }
                       } else {
                           uportError = "" + err
                           newAccountActivity.onFailedService()
                           // UserProfileActivity.setButtonClickable("ServiceButton", true)
                       }
                   }.join()
               } else {
                   receipit = null
                   receiptSetted = false
                   newAccountActivity.onFailedService()
                   //UserProfileActivity.setButtonClickable("ServiceButton", true)
               }
        } }
    }

    fun changeConsentStatus(user:IUser, userSC:ServiceConsent, service: IService, editor: SharedPreferences.Editor, consentName:String, switch:Switch, activity: UserProfileActivity){
        launch{ launch {
            if (accountSetted && (job==null || (job as Job).isCompleted)) {
                var str = "Cambio di consent di " +consentName
                receipit = null
                receiptSetted = false
                uportA.send(this@UportData.context, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                editor.putBoolean(consentName, switch.isChecked)
                                editor.commit()
                                setReceipt(receipt)

                                val data = HashSet<String>()
                                if (switch.isChecked) {
                                    data.add(Metadata.DATOUNOPROVA_CONST )
                                    data.add(Metadata.DATODUEPROVA_CONST)
                                } else {
                                    data.add(Metadata.DATODUEPROVA_CONST)
                                }
                                val outputDataConsent = OutputDataConsent(data, userSC)
                                user.addDataConsent(outputDataConsent, service)
                            } else {
                                uportError = "" +err
                                //activity.checkSwitch(switch,!switch.isChecked)
                            }
                        }
                    } else {
                        uportError = "" + err
                        //activity.checkSwitch(switch,!switch.isChecked)
                    }
                }.join()
            } else {
                receipit = null
                receiptSetted = false
                //activity.checkSwitch(switch,!switch.isChecked)
            }
        } }
    }


    fun withdrawConsentForService(service:IService,userProfileActivity: UserProfileActivity){
        launch { launch {
            if (accountSetted && (job==null || (job as Job).isCompleted)) {
                var str = "Verrà revocato il consenso per il servizio "+service.toString()
                receipit = null
                receiptSetted = false
                uportA.send(this@UportData.context, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                userProfileActivity.onWithdrawnSuccess()
                                */
/*
                                controller.withdrawConsentForService(service)
                                editor.remove("LocationConsent")
                                editor.commit()
                                UserProfileActivity.setButtonClickable("ServiceButton", true)
*//*

                            } else {
                                uportError = "" + err
//                                UserProfileActivity.setButtonClickable("WithdrawButton", true)
//                                UserProfileActivity.setButtonClickable("DisableButton", true)
                                userProfileActivity.onWithdrawnFailure()
                            }
                        }
                    } else {
                        uportError = "" + err
//                        UserProfileActivity.setButtonClickable("WithdrawButton", true)
//                        UserProfileActivity.setButtonClickable("DisableButton", true)
                        userProfileActivity.onWithdrawnFailure()
                    }
                }.join()
            } else {
                receipit = null
                receiptSetted = false
//                UserProfileActivity.setButtonClickable("WithdrawButton", true)
//                UserProfileActivity.setButtonClickable("DisableButton", true)
                userProfileActivity.onWithdrawnFailure()
            }
        }
        }
    }

    fun swapDisableOption(button: Button,controller: IController,mLocationSwitch: Switch, mOtherSwitch: Switch, service: IService, activity: UserProfileActivity){
        launch { launch {
            if (accountSetted && (job==null || (job as Job).isCompleted)) {
                var str = "Verrà "
                if (button.text.toString().contains("Disabilita")){
                    str +="abilitato "
                }else{
                    str +="disabilitato "

                }
                str+="il consenso per il servizio "+service.toString()
                receipit = null
                receiptSetted = false
                uportA.send(this@UportData.context, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                if (button.text.toString().contains("Disabilita"))
                                {
                                    controller.toggleStatus(service, false)
                                    button.text = "Abilita\n" + "consenso"
                                    //activity.checkSwitch(mLocationSwitch,false)
                                    //activity.checkSwitch(mOtherSwitch,false)
                                }else{
                                    controller.toggleStatus(service, true)
                                    button.text = "Disabilita\n" + "consenso"
                                    //activity.checkSwitch(mLocationSwitch,true)
                                    //activity.checkSwitch(mOtherSwitch,true)
                                }
                                UserProfileActivity.setButtonClickable("DisableButton", true)
                            } else {
                                uportError = "" + err
                                UserProfileActivity.setButtonClickable("DisableButton", true)
                            }
                        }
                    } else {
                        uportError = "" + err
                        UserProfileActivity.setButtonClickable("DisableButton", true)
                    }
                }.join()
            } else {
                receipit = null
                receiptSetted = false
                UserProfileActivity.setButtonClickable("DisableButton", true)

            }
        } }
    }
*/

