package tesi.barto.myport.activities;

/**
 * Edited by Luca Bartolotti on 12/07/2018.
 */


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import tesi.barto.myport.R;
import tesi.barto.myport.Uport.UportData;
import tesi.barto.myport.controller.IController;
import tesi.barto.myport.controller.MyController;
import tesi.barto.myport.model.MyData.MyData;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.services.AbstractService;
import tesi.barto.myport.model.services.IService;
import tesi.barto.myport.model.services.ServiceProva;
import tesi.barto.myport.model.services.ServiceUport;
import tesi.barto.myport.model.users.IUser;
import tesi.barto.myport.utilities.VoiceSupport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class NewAccountActivity extends AppCompatActivity implements View.OnClickListener {

	private ImageButton mNewAccountButton;
	private SharedPreferences sharedPreferences;
	private boolean voiceSupport;
	private TextToSpeech tts;
	private String email;
	private String password;

	private NewAccountActivity instance=null;

	private ServiceUport serviceUport;
    private ServiceProva serviceProva;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_account);

		this.instance=this;

		mNewAccountButton = findViewById(R.id.button_add);
		mNewAccountButton.setOnClickListener(this);


		mNewAccountButton.setClickable(true);
		mNewAccountButton.setBackgroundColor(Color.parseColor("#00897b"));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

		if (this.getIntent().hasExtra(Intent.EXTRA_EMAIL)) {
			email = this.getIntent().getStringExtra(Intent.EXTRA_EMAIL);
		}
		if (this.getIntent().hasExtra(Intent.EXTRA_TEXT)) {
			password = this.getIntent().getStringExtra(Intent.EXTRA_TEXT);
		}
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(NewAccountActivity.this, MainActivity.class);
		i.putExtra("EXTRA_CLOSED", "Nessun account creato");
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

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.button_add:
				newAccount(view);
				break;
		}
	}

	public void newAccount(View v) {
		if(voiceSupport)
			if(!VoiceSupport.isTalkBackEnabled(this)){
				tts.speak("Sta per essere creato un nuovo account MyData per questo servizio. Procedere?", TextToSpeech.QUEUE_FLUSH, null);
			}
		Toast.makeText(this, "Sta per essere creato un nuovo account MyData per questo servizio. Procedere?", Toast.LENGTH_SHORT).show();

		new AlertDialog.Builder(NewAccountActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Nuovo account servizioProva")
				.setMessage("Procedere?")
				.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						// viene creato un account per l'utente
						// vengo reindirizzato alla schermata di gestione dei consent
						//  fare sì che tornando indietro dalla successiva schermata non si possa ritornare a questa (finish()?)
						serviceProva = new ServiceProva();
						//LUCA
						serviceUport = new ServiceUport();
						UportData account;
						IUser user = MyData.getInstance().loginUser("nomecognome@prova.it", "password".toCharArray());
						ServiceConsent userSC = user.getActiveSCForService(serviceUport);
						try {
							account=(UportData) userSC.getService().provideService(user); //probabilmente non necessario
						}catch (IOException e){
							//ERRORE IO, probabilmente lettura file
							account= new UportData(MainActivity.getInstance());
						}
						setAccountButtonClickable(false);
                            try {
                                Method onSuccess = NewAccountActivity.class.getMethod("onConfirmedService");
                                Method onFailure = NewAccountActivity.class.getMethod("onFailedService");
                                String trString="Sta per essere creato un nuovo account MyData per il servizio: "+serviceProva.toString();
                                account.sendTransaction(instance,trString,onSuccess,onFailure);
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                                onFailedService();
                            }
						//account.addService(controller,serviceProva,instance);
						//FINE LUCA
/*
						controller.addService(serviceProva);

						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean("LocationConsent", true);
						editor.commit();
						Intent i = new Intent(NewAccountActivity.this, UserProfileActivity.class);
						i.putExtra(EXTRA_MESSAGE, "Account creato con successo");
						i.putExtra(Intent.EXTRA_EMAIL, email);
						i.putExtra(Intent.EXTRA_TEXT, password);
						startActivity(i);*/
					}
					})
				.setNegativeButton("No",  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onFailedService();
					}
				})
				.show();
	}

	public void setAccountButtonClickable(boolean bool){
		mNewAccountButton.setBackgroundColor(bool?Color.parseColor("#00897b"):Color.DKGRAY);
		mNewAccountButton.setClickable(bool);
	}

	public void onConfirmedService(){
        IController controller = new MyController();
        controller.logInUser("nomecognome@prova.it", "password".toCharArray());
		controller.addService(serviceProva); // se ci arrivo da DataConsentActivity e ho già l'account settato espode tutto, giustamente.
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("LocationConsent", true);
		editor.commit();
		Intent i = new Intent(NewAccountActivity.this, UserProfileActivity.class);
		i.putExtra(EXTRA_MESSAGE, "Account creato con successo");
		i.putExtra(Intent.EXTRA_EMAIL, email);
		i.putExtra(Intent.EXTRA_TEXT, password);
		startActivity(i);
	}

	public void onFailedService() {
		setAccountButtonClickable(true);
	}
}
