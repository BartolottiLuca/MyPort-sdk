package tesi.barto.myport.activities;

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
import me.uport.sdk.Uport;



import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static Button mEnterButton;
	private IController controller;
	private IUser user;
	//private AbstractService serviceProva;
	private AbstractService uportService;
	private static MainActivity instance=null;

	public static Context getAppContext(){
		return instance.getApplicationContext();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.instance = this;
		mEnterButton = (Button) findViewById(R.id.button_enter);
		mEnterButton.setClickable(false);
		mEnterButton.setOnClickListener(this);

		// (per ora inizializzato così in attesa del lavoro aggiornato dell'app in cui inserire,
		// eventualmente, l'utente nel modello)
		controller = new MyController();
		// servizio a cui si riferisce
		//serviceProva = new ServiceProva();
		uportService = new ServiceUport();
		if (!this.getIntent().hasExtra("EXTRA_CLOSED")) {
			controller.createMyDataUser("Nome", "Cognome", new Date(1995, 9, 22), "nomecognome@prova.it", "password".toCharArray(), this);

			// per test
			//controller.addService(serviceProva);
			//controller.withdrawConsentForService(serviceProva);
			//controller.addService(serviceProva);
			controller.addService(uportService);
		} else {
			// vengo dalla pressione di un pulsante Up, eventualmente saranno poi passate le credenziali
			controller.logInUser("nomecognome@prova.it", "password".toCharArray());
			Toast.makeText(this, this.getIntent().getStringExtra("EXTRA_CLOSED"), Toast.LENGTH_SHORT).show();
		}
		user = ((MyController) controller).getUser();
		//faccio iniziare il Login su Uport
		/*try {
			user.getActiveSCForService(uportService).getService().provideService(user);
		} catch (IOException e) {
				Toast.makeText(this, "Unable to start UportService",Toast.LENGTH_SHORT).show();
			}*/
	}

    @Override
    public void onClick(View view) {
        Intent i = null;
        switch(view.getId()){
            case R.id.button_enter:
				if (controller.getAllActiveServicesForUser().contains(uportService)) {
					// L'utente ha un account attivo/disabilitato presso il servizio: va avviata l'activity UserProfileActivity
					// per test
					try {
						uportService.provideService(user);
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

    public static void setEnterButtonClickable(boolean bol){
		mEnterButton.setBackgroundColor(bol?Color.DKGRAY:Color.RED);
		mEnterButton.setClickable(bol);
	}
}
