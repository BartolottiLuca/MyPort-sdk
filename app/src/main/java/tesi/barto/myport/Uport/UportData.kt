package tesi.barto.myport.Uport

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Button
import android.widget.Switch
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks
import me.uport.sdk.extensions.*
import me.uport.sdk.fuelingservice.FuelTokenProvider
import me.uport.sdk.identity.Account
import me.uport.sdk.jsonrpc.JsonRPC
import tesi.barto.myport.activities.MainActivity
import tesi.barto.myport.activities.NewAccountActivity
import tesi.barto.myport.activities.UserProfileActivity
import tesi.barto.myport.controller.IController
import tesi.barto.myport.model.consents.OutputDataConsent
import tesi.barto.myport.model.consents.ServiceConsent
import tesi.barto.myport.model.registry.Metadata
import tesi.barto.myport.model.services.IService
import tesi.barto.myport.model.users.IUser

class UportData () {
    private var con: Context = MainActivity.getAppContext()
    private var uportError: String = ""
    private var uportA: Account = Account.blank
    private var accountSetted: Boolean = false
    private var receipit: JsonRPC.TransactionReceipt? = null
    private var receiptSetted: Boolean = false
    private var job: Job? = null

    constructor(applicationContext: Context) : this() {
        con = applicationContext
        if (Uport.defaultAccount == null) {
            val config = Uport.Configuration().setApplicationContext(con).setFuelTokenProvider(FuelTokenProvider(con, "2ouNYyHP1yLjfVM4mVdYKwx3jGEUgpQHBya"))
            Uport.initialize(config)
            Uport.createAccount(network = Networks.rinkeby) { err, account ->
                // update UI to reflect the existence of a defaultAccount
                if (err == null) {
                    this.setAccount(account)
                    MainActivity.setEnterButtonClickable(true)
                } else {
                    uportError = "ERROR: $err."
                }
            }
        } else {
            this.setAccount(Uport.defaultAccount as Account)
        }
    }

    constructor(applicationContext: Context, accountJson: String) : this() {
        this.con = applicationContext
        if (Uport.defaultAccount == null) {
            val config = Uport.Configuration().setApplicationContext(con).setFuelTokenProvider(FuelTokenProvider(con, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
            Uport.initialize(config)
            this.setAccount(Account.fromJson(accountJson) ?: Account.blank)
        } else {
            this.setAccount(Uport.defaultAccount as Account)
        }
    }

    constructor(applicationContext: Context, account: Account) : this() { //non serve, ma l'ho fatto e lo lascio qua
        this.con = applicationContext
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

    fun addService(controller: IController, service: IService, newAccountActivity: NewAccountActivity) {
        launch { launch {
               if (accountSetted && (job==null || (job as Job).isCompleted)) {
                   var str = "Confermare servizio: " + service.toString()
                   receipit = null
                   receiptSetted = false
                   uportA.send(con, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                       if (err == null) {
                           job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                               if (err == null) {
                                   setReceipt(receipt)
                                   controller.addService(service) // se ci arrivo da DataConsentActivity e ho già l'account settato espode tutto, giustamente.
                                   newAccountActivity.onConfirmedService()
                                   /*
                                   UserProfileActivity.setButtonClickable("DisableButton", true)
                                   UserProfileActivity.setButtonClickable("WithdrawButton", true)
                                   */
                               } else {
                                   uportError = "" + err
                                   newAccountActivity.onFailureService()
                                   //UserProfileActivity.setButtonClickable("ServiceButton", true)
                               }
                           }
                       } else {
                           uportError = "" + err
                           newAccountActivity.onFailureService()
                           // UserProfileActivity.setButtonClickable("ServiceButton", true)
                       }
                   }.join()
               } else {
                   receipit = null
                   receiptSetted = false
                   newAccountActivity.onFailureService()
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
                uportA.send(con, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
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
                                activity.checkSwitch(switch,!switch.isChecked)
                            }
                        }
                    } else {
                        uportError = "" + err
                        activity.checkSwitch(switch,!switch.isChecked)
                    }
                }.join()
            } else {
                receipit = null
                receiptSetted = false
                activity.checkSwitch(switch,!switch.isChecked)
            }
        } }
    }


    fun withdrawConsentForService(service:IService,userProfileActivity: UserProfileActivity){
        launch { launch {
            if (accountSetted && (job==null || (job as Job).isCompleted)) {
                var str = "Verrà revocato il consenso per il servizio "+service.toString()
                receipit = null
                receiptSetted = false
                uportA.send(con, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                userProfileActivity.onWithdrawnSuccess()
                                /*
                                controller.withdrawConsentForService(service)
                                editor.remove("LocationConsent")
                                editor.commit()
                                UserProfileActivity.setButtonClickable("ServiceButton", true)
*/
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
                if (button.getText().toString().contains("Disabilita")){
                    str +="abilitato "
                }else{
                    str +="disabilitato "

                }
                str+="il consenso per il servizio "+service.toString()
                receipit = null
                receiptSetted = false
                uportA.send(con, getAccount()?.proxyAddress as String, str.toByteArray()) { err, txHash ->
                    if (err == null) {
                        job = Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
                            if (err == null) {
                                if (button.getText().toString().contains("Disabilita"))
                                {
                                    controller.toggleStatus(service, false)
                                    button.setText("Abilita\n" + "consenso")
                                    activity.checkSwitch(mLocationSwitch,false)
                                    activity.checkSwitch(mOtherSwitch,false)
                                }else{
                                    controller.toggleStatus(service, true)
                                    button.setText("Disabilita\n" + "consenso")
                                    activity.checkSwitch(mLocationSwitch,true)
                                    activity.checkSwitch(mOtherSwitch,true)
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

}