package tesi.barto.myport.activities;

/**
 * Edited by Luca Bartolotti on 12/07/2018.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import tesi.barto.myport.R;
import tesi.barto.myport.Uport.UportData;
import tesi.barto.myport.controller.IController;
import tesi.barto.myport.controller.MyController;
import tesi.barto.myport.model.MyData.MyData;
import tesi.barto.myport.model.consents.OutputDataConsent;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.registry.Metadata;
import tesi.barto.myport.model.services.ServiceProva;
import tesi.barto.myport.model.services.ServiceUport;
import tesi.barto.myport.model.users.IUser;
import tesi.barto.myport.utilities.VoiceSupport;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.lang.reflect.Method;
import java.util.Set;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/* Questa classe rappresenta l'interfaccia grafica che va inserita all'interno dell'applicazione
 * di GoBoBus (aggiornata con l'interfaccia p2p) per consentire all'utente (si scelga se modellare
 * l'utente, mantenendone ad esempio un database in cloud che a questo livello risulterebbe ancora
 * abbastanza inutile, oppure farne a meno gestendo solamente le preferenze dell'utente a livello
 * locale) di gestire nel dettaglio i dati condivisi con gli altri utenti della rete di dispositivi
 * che devono scambiarsi i dati.
 *
 * Allo stato attuale i dati qui inseriti sono di prova, non avendo sotto mano i veri dati di cui
 * dover regolare lo scambio, ma il meccanismo è quello corretto: ho la possibilità di scegliere
 * quali dati condividere e quali no, oppure di disattivare lo scambio dei dati, oppure di revocare
 * del tutto il consenso allo scambio dei dati. In questi ultimi due casi è ancora da stabilire
 * cosa ciò comporti nell'utilizzo dell'applicazione (probabilmente nulla? Oppure si può scegliere
 * di impedire la fruizione dei dati in tempo reale forniti dalla rete degli utenti se non si
 * condividono i propri?).
 *
 * Nello specifico questa classe si appoggia ad un controller (MyController) che regola la creazione
 * e l'accesso dell'utente (per ora hard cabled in attesa di sapere se possa essere di interesse,
 * come indicato sopra) e l'eventuale creazione di un account utente presso il servizio, l'unico
 * per ora, che dovrebbe essere GoBoBus (attualmente servizio di prova).
 * Inizializza poi l'interfaccia grafica con le attuali preferenze dell'utente: in questo caso
 * cerca se l'utente desideri l'assistente vocale (in tal caso lo inizializza) e dovrebbe cercare
 * se esiste ed è attivo, disabilitato o revocato il service consent e quali dei dati l'utente abbia
 * interesse a condividere e quali meno, presumibilmente attingendo per il primo caso dai service
 * consent dell'utente, e nel secondo caso nei data consent..? Nel dubbio ho inserito la preferenza
 * tra le sharedpreferences come tutto il resto.
 */

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private Switch mLocationSwitch;
    private static Button mDisableButton;  //fatti statici così da poter chiamare setButtonClickable
    private static Button mWithdrawButton;
	private TextView uportView;
	private TextToSpeech tts;
	private boolean voiceSupport;
	private SharedPreferences sharedPreferences;
	private IUser user;
	private boolean locationConsent;
	private ServiceProva serviceProva;
	private ServiceConsent userSC;
	private IController controller;
	private String email;
	private String pass;
	private Switch mOtherSwitch;

	//LUCA
	private ServiceUport serviceUport;
	private UportData account;
	private UserProfileActivity activityInstance =null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityInstance=this;
        setContentView(R.layout.activity_user_profile);

		mLocationSwitch = findViewById(R.id.locationSwitch);
		mOtherSwitch = findViewById(R.id.otherSwitch);
		mDisableButton = findViewById(R.id.button_disable);
		mDisableButton.setOnClickListener(this);
		setButtonClickable("DisableButton",true);

		mWithdrawButton = findViewById(R.id.button_withdraw);
		mWithdrawButton.setOnClickListener(this);
		setButtonClickable("WithdrawButton",true);

		setTitle("Gestione Consent");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		serviceUport = new ServiceUport();
		serviceProva= new ServiceProva();
		email = "nomecognome@prova.it";
		pass = "password";
		if (this.getIntent().hasExtra(Intent.EXTRA_EMAIL)) {
			email = this.getIntent().getStringExtra(Intent.EXTRA_EMAIL);
		}
		if (this.getIntent().hasExtra(Intent.EXTRA_TEXT)) {
			pass = this.getIntent().getStringExtra(Intent.EXTRA_TEXT);
		}

		controller = new MyController();
		controller.logInUser(email, pass.toCharArray());
		user = MyData.getInstance().loginUser(email, pass.toCharArray());

		// controllo se l'utente preferisce l'assistente vocale o meno
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		voiceSupport = sharedPreferences.getBoolean("VoiceSupport", true);

		if(voiceSupport) {
			tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status != TextToSpeech.ERROR) {
						tts.setLanguage(Locale.getDefault());
					}
				}
			});
		}
		userSC = user.getActiveSCForService(serviceUport);
		try {
			account = (UportData) userSC.getService().provideService(user);
		}catch (IOException e){
			account = new UportData(MainActivity.getInstance());
		}
		//TODO eliminare TextView
		uportView = findViewById(R.id.uportView);
		uportView.setText("•Address: "+ account.getAccount().getDeviceAddress()+"\n•Network: "+ account.getAccount().getNetwork()+"\n•Token: "+ account.getAccount().getFuelToken()+"\n");

		// ottengo l'attuale preferenza per la condivisione della posizione
		// NOTA: per adesso c'è solo la posizione come dato, ed ho evitato di farlo,
		// ma nel caso tutti i dati non avessero il consenso ad essere condivisi è come aver
		// disabilitato il service consent del tutto
		locationConsent = sharedPreferences.getBoolean("LocationConsent", true); // corretto salvarlo nelle preferences?
		// dovrei recuperarlo dal DataConsent per questo servizio..?

		mLocationSwitch.setChecked(false);
		mLocationSwitch.setTextOn("ON");
		mLocationSwitch.setTextOff("OFF");
		mLocationSwitch.setOnClickListener(this);

		mLocationSwitch.setChecked(false);
		mLocationSwitch.setTextOn("ON");
		mLocationSwitch.setTextOff("OFF");
		mLocationSwitch.setOnClickListener(this);

		// se arrivo in questa schermata dopo aver creato un nuovo account:
		if (this.getIntent().hasExtra(EXTRA_MESSAGE))
			Toast.makeText(this, this.getIntent().getStringExtra(EXTRA_MESSAGE), Toast.LENGTH_SHORT).show();
    }//oncreate

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("EXTRA_CLOSED", "Sessione terminata");
		startActivity(i);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Handler di tutti i pulsanti e gli switch che avvia il metodo corretto
	 */

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_disable:
            	disableOption(view);
                break;
            case R.id.button_withdraw:
            	withdrawOption(view);
                break;
			case R.id.locationSwitch:
				changeLocationConsent(view);
				break;
        }
    }

    /* Questo metodo viene invocato al cambio della preferenza della condivisione della posizione
     * (switch). Esso chiede conferma della decisione dell'utente, con supporto vocale, e se
     * confermato, modifica la preferenza dell'utente nella condivisione di quel dato nelle
     * sharedpreferences. Contestualmente deve anche creare un nuovo dataconsent? Direi di no..
     * Se non viene confermato, lo switch torna nella posizione precedente e nulla cambia.
     */
	public void changeLocationConsent(View view) {
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Il consenso per la posizione sarà " + (mLocationSwitch.isChecked() ? "attivato" : "disattivato") + ". Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Il consenso per la posizione sarà " + (mLocationSwitch.isChecked() ? "attivato" : "disattivato") + ". Procedere?", Toast.LENGTH_SHORT).show();

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(mLocationSwitch.isChecked() ? "Attiva" : "Disattiva")
				.setMessage("Il consenso per la posizione sarà " + (mLocationSwitch.isChecked() ? "attivato" : "disattivato") + ".\nProcedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cambia la preferenza nelle sharedPreferences
						try {
							Method onSuccess = UserProfileActivity.class.getMethod("onChangeConsentSuccess");
							Method onFailure = UserProfileActivity.class.getMethod("onChangeConsentFailure");
							account.sendTransaction(activityInstance,"Il consenso per la posizione sarà " + (mLocationSwitch.isChecked() ? "attivato" : "disattivato") ,onSuccess,onFailure);
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
							onChangeConsentFailure();
						}
						// cambia lo status del consent per la posizione
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onChangeConsentFailure();
					}
				})
				.show();
	}

	public void onChangeConsentSuccess(){
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("LocationConsent", mLocationSwitch.isChecked());
		editor.commit();

		// cambia lo status del consent per la posizione
		Set<String> data = new HashSet<String>();
		if (mLocationSwitch.isChecked()) {
			data.add(Metadata.DATOUNOPROVA_CONST);
			data.add(Metadata.DATODUEPROVA_CONST);
		} else {
			data.add(Metadata.DATODUEPROVA_CONST);
		}
		OutputDataConsent outputDataConsent = new OutputDataConsent(data, userSC);
		user.addDataConsent(outputDataConsent, serviceProva);
	}

	public void onChangeConsentFailure(){
		mLocationSwitch.setChecked(!mLocationSwitch.isChecked());
	}

	//public void checkSwitch(Switch switc, boolean bool){
		/*final Switch sw=switc;
		final Boolean bo=bool;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sw.setChecked(bo);
			}
		}, 100);*/
	//}
/*	made by valentina
	public void changeServiceConsent(View view) {
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Il consenso per il servizio sarà " + (mOtherSwitch.isChecked() ? "attivato" : "disattivato") + ". Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Il consenso per il servizio sarà " + (mOtherSwitch.isChecked() ? "attivato" : "disattivato") + ". Procedere?", Toast.LENGTH_SHORT).show();
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(mOtherSwitch.isChecked() ? "Attiva" : "Disattiva")
				.setMessage("Il consenso per la posizione sarà " + (mOtherSwitch.isChecked() ? "attivato" : "disattivato") + ".\nProcedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cambia la preferenza nelle sharedPreferences
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean("ServiceConsent", mOtherSwitch.isChecked());
						editor.commit();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//devo mandare una transizione
						mOtherSwitch.setChecked(!mOtherSwitch.isChecked());
					}
				})
				.show();
	}
*/

	/* Questo metodo viene invocato alla pressione del pulsante "Revoca consenso".
	 * Anch'esso chiede conferma della decisione dell'utente, con supporto vocale, e se
	 * confermato, cambia opportunamente lo stato del service consent. (NO: In questo caso dovrebbe
	 * completamente lanciare una nuova schermata, la stessa della creazione dell'account presso
	 * (uesto servizio che ho messo nel to do in alto, perché il consent è stato revocato e non
	 * è più attivabile da questo status (withdrawn)).
	 * rimane questa schermata, se si vuole aggiungere di nuovo il servizio si premerà sul bottone "aggiungi servizio"
	 * Se non viene confermato, nulla cambia.
	 */
	public void withdrawOption(final View view) {
		setButtonClickable("WithdrawButton", false);
		setButtonClickable("DisableButton", false);
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Il consenso sarà revocato. Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Il consenso sarà revocato. Procedere?", Toast.LENGTH_SHORT).show();
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Revoca")
				.setMessage("Verrà eliminato l'account presso questo servizio.\nProcedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cambia lo stato del consent a withdrawn
						try {
							Method onSuccess = UserProfileActivity.class.getMethod("onWithdrawnSuccess");
							Method onFailure = UserProfileActivity.class.getMethod("onWithdrawnFailure");
							account.sendTransaction(activityInstance,"Verrà revocato il consenso per il servizio "+serviceProva.toString(),onSuccess,onFailure);
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
							onWithdrawnFailure();
						}
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onWithdrawnFailure();
					}
				})
				.show();
	}

	public void onWithdrawnSuccess(){
		//faccio partire newAccountActivity
		controller.withdrawConsentForService(serviceProva);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove("LocationConsent");
		editor.commit();
		// avvia l'activity per creare un nuovo account MyData
		Intent i = new Intent (UserProfileActivity.this, NewAccountActivity.class);
		i.putExtra(EXTRA_MESSAGE, "Account eliminato con successo");
		i.putExtra(Intent.EXTRA_EMAIL, email);
		i.putExtra(Intent.EXTRA_TEXT, pass);
		startActivity(i);
	}

	public void onWithdrawnFailure(){
		//riabilito i pulsanti
		setButtonClickable("WithdrawButton", true);
		setButtonClickable("DisableButton", true);
	}

	/* Questo metodo viene invocato alla pressione del pulsante "Disabilita/Abilita consenso".
     * Anch'esso chiede conferma della decisione dell'utente, con supporto vocale, e se
     * confermato, cambia opportunamente lo stato del service consent. Disattiva o attiva inoltre
     * tutti gli switch e cambia il testo del pulsante in modo opportuno.
     * Se non viene confermato, nulla cambia.
     */
	public void disableOption(View v){
		setButtonClickable("WithdrawButton", false);
		setButtonClickable("DisableButton", false);
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Il consenso sarà " + ((mDisableButton.getText().toString().contains("Disabilita")) ? "disattivato" : "attivato") + ". Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Il consenso sarà " + ((mDisableButton.getText().toString().contains("Disabilita")) ? "disattivato" : "attivato") + ". Procedere?", Toast.LENGTH_SHORT).show();

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(mDisableButton.getText().toString().contains("Disabilita") ? "Disabilita" : "Abilita")
				.setMessage("Procedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cambia lo stato del consent enabled>disabled o disabled>enabled
						// cambia il pulsante ad "Attiva"/"Disattiva"
						try {
							Method onSuccess = UserProfileActivity.class.getMethod("onDisableSuccess");
							Method onFailure = UserProfileActivity.class.getMethod("onDisableFailure");
							String trString="Verrà "+ (mDisableButton.getText().toString().contains("Disabilita")?"Abilitato":"Disabilitato")+" il consenso per il Servizio "+serviceProva.toString();
							account.sendTransaction(activityInstance,trString,onSuccess,onFailure);
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
							onDisableFailure();
						}
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onDisableFailure();
					}
				})
				.show();
	}

	public void onDisableSuccess(){
		if (mDisableButton.getText().toString().contains("Disabilita")) {
			controller.toggleStatus(serviceProva, false);
			mDisableButton.setText("Abilita\n" + "consenso");
//			mLocationSwitch.setEnabled(false);
//			mOtherSwitch.setEnabled(false);
		} else {
			controller.toggleStatus(serviceProva, true);
			mDisableButton.setText("Disabilita\n" + "consenso");
//			mLocationSwitch.setEnabled(true);
//			mOtherSwitch.setEnabled(true);
		}
		UserProfileActivity.setButtonClickable("DisableButton", true);
		setButtonClickable("WithdrawButton",true);
	}

	public void onDisableFailure(){
		setButtonClickable("WithdrawButton", true);
		setButtonClickable("DisableButton", true);
	}

	/* Questo metodo viene invocato alla pressione del pulsante "Mostra Data Consents".
	 * Esso apre una schermata che stampa a video tutti i Data Consent relativi a tutti gli eventuali
	 * account che l'utente ha presso questo servizio.
	 * Nella versione finale probabilmente non avrà ragione d'essere, ma per testing è utile.
	 */
	public void viewDataConsents(View v) {
		String allDConsents="";
		try{
			userSC = user.getActiveSCForService(serviceProva);
			if (userSC != null) {
				// l'utente ha un account ma non è attivo: inizializzo di conseguenza la gui
				allDConsents = controller.getAllDConsents(serviceProva);
			}
		}catch(Exception e){
			allDConsents="";
		}
		allDConsents += controller.getAllDConsents(serviceUport);
		Intent i = new Intent(this,DataConsentActivity.class);
		i.putExtra(Intent.EXTRA_TEXT, allDConsents);
		startActivity(i);
	}

	public static void setButtonClickable(String button,boolean bol){
		if(button.compareToIgnoreCase("DisableButton")==0) {
			mDisableButton.setClickable(bol);
			mDisableButton.setBackgroundColor(bol?Color.parseColor("#00897b"):Color.DKGRAY);
		}
		if(button.compareToIgnoreCase("WithdrawButton")==0){
			mWithdrawButton.setClickable(bol);
			mWithdrawButton.setBackgroundColor(bol?Color.parseColor("#00897b"):Color.DKGRAY);
		}/*
		if(button.compareToIgnoreCase("ServiceButton")==0){
			mServiceButton.setClickable(bol);
			mServiceButton.setBackgroundColor(bol?Color.parseColor("#00897b"):Color.DKGRAY);
		}*/
	}

	/*public void aggiungiServizioProva(View v) {
		setButtonClickable("ServiceButton",false);
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Verrà aggiunto il servizio Prova. Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Verrà aggiunto il servizio Prova. Procedere?", Toast.LENGTH_SHORT).show();
		final UserProfileActivity context=this;
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Aggiungi")
				.setMessage("Verrà aggiunto il servizio Prova. Procedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dobbiamo aggiungere il servizio
						userSC = user.getActiveSCForService(serviceUport);
						try {
							account=(UportData) userSC.getService().provideService(user); //probabilmente non necessario
						}catch (IOException e){
							//ERRORE IO, probabilmente lettura file
							account= new UportData(MainActivity.getInstance());
						}
						account.addService(controller,serviceProva);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setButtonClickable("ServiceButton",true);
					}
				})
				.show();
	}*/
}
