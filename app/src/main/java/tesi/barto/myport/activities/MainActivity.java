package tesi.barto.myport.activities;

/**
 * Edited by Luca Bartolotti on 01/07/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tesi.barto.myport.R;
import tesi.barto.myport.controller.IController;
import tesi.barto.myport.controller.MyController;
import tesi.barto.myport.model.services.AbstractService;
import tesi.barto.myport.model.services.ServiceProva;
import tesi.barto.myport.model.services.ServiceUport;
import tesi.barto.myport.model.users.IUser;


import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mEnterButton;
	private IController controller;
	private IUser user;
	private AbstractService serviceUport;
	private AbstractService serviceProva;
	private static MainActivity instance=null;

	public static Context getInstance(){ return instance; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this;
		mEnterButton = findViewById(R.id.button_enter);
		setEnterButtonClickable(false);
		mEnterButton.setOnClickListener(this);

		// (per ora inizializzato così in attesa del lavoro aggiornato dell'app in cui inserire,
		// eventualmente, l'utente nel modello)
		controller = new MyController();
		// servizio a cui si riferisce
		serviceProva = new ServiceProva();
		serviceUport = new ServiceUport();
		if (!this.getIntent().hasExtra("EXTRA_CLOSED")) {
			controller.createMyDataUser("Nome", "Cognome", new Date(1995, 9, 22), "nomecognome@prova.it", "password".toCharArray(), this);

			// per test
			//controller.addService(serviceProva);
			//controller.withdrawConsentForService(serviceProva);
			//controller.addService(serviceProva);
			controller.addService(serviceUport);
		} else {
			// vengo dalla pressione di un pulsante Up, eventualmente saranno poi passate le credenziali
			controller.logInUser("nomecognome@prova.it", "password".toCharArray());
			Toast.makeText(this, this.getIntent().getStringExtra("EXTRA_CLOSED"), Toast.LENGTH_SHORT).show();
		}
		user = ((MyController) controller).getUser();
	}

    @Override
    public void onClick(View view) {
        Intent i = null;
        switch(view.getId()){
            case R.id.button_enter:
				if (controller.getAllActiveServicesForUser().contains(serviceProva)) {
					// L'utente ha un account attivo/disabilitato presso il servizio: va avviata l'activity UserProfileActivity
					// per test
					try {
						serviceUport.provideService(user);
					} catch (IOException e) {
						e.printStackTrace();
					}
					i = new Intent(MainActivity.this, UserProfileActivity.class);
				} else {
					// No account presso il servizio: va avviata l'activity NewAccountActivity
					i = new Intent(MainActivity.this, NewAccountActivity.class);
				}
        }
		i.putExtra(Intent.EXTRA_EMAIL, "nomecognome@prova.it");
		i.putExtra(Intent.EXTRA_TEXT, "password");
        startActivity(i);
    }

    public void setEnterButtonClickable(boolean bool){
		mEnterButton.setBackgroundColor(bool?Color.DKGRAY:Color.RED);
		mEnterButton.setClickable(bool);
	}
}
