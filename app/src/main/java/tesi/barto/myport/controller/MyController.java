package tesi.barto.myport.controller;

import android.content.Context;

import tesi.barto.myport.model.MyData.IMyData;
import tesi.barto.myport.model.MyData.MyData;
import tesi.barto.myport.model.consents.ConsentManager;
import tesi.barto.myport.model.consents.ConsentStatus;
import tesi.barto.myport.model.consents.DataConsent;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.services.IService;
import tesi.barto.myport.model.services.ServiceProva;
import tesi.barto.myport.model.users.IAccount;
import tesi.barto.myport.model.users.IUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Valentina on 31/08/2017.
 */

public class MyController implements IController {

	private IUser authenticatedUser;
	private IMyData myDataInstance;

	public MyController() {
		myDataInstance = MyData.getInstance();
	}

	public IUser getUser() {
		return authenticatedUser;
	}

	@Override
	public void createMyDataUser(String firstName, String lastName, Date dateOfBirth, String emailAddress, char[] password, Context context) {
		authenticatedUser = myDataInstance.createMyDataAccount(firstName, lastName, dateOfBirth, emailAddress, password, context);
	}

	@Override
	public void logInUser(String email, char[] password) {
		authenticatedUser = myDataInstance.loginUser(email, password);
	}

	@Override
	public void toggleStatus(IService selectedService, boolean status) {
		// l'user è già autenticato, in teoria non dovrei fare controlli su
		// authusr == null. poichè invocato da profile panel,l'account presso il
		// servizio dovrebbe sempre esistere TODO transizione a o da qui?

		if (status) {
			ConsentManager.changeServiceConsentStatusForService(authenticatedUser, selectedService, ConsentStatus.ACTIVE);
		} else {
			ConsentManager.changeServiceConsentStatusForService(authenticatedUser, selectedService, ConsentStatus.DISABLED);
		}
	}

	@Override
	public String getAllSConsents(IService selectedService) {
		StringBuilder sb = new StringBuilder();
		IAccount accountAtService = null;
		for (IAccount a : authenticatedUser.getAllAccounts()) {
			if (a.getService().equals(selectedService))
				accountAtService = a;
		}
		for (ServiceConsent sc : accountAtService.getAllServiceConsents())
			sb.append(sc.toString() + System.getProperty("line.separator"));
		return sb.toString();
	}

	@Override
	public void addService(IService service) {
		// Service Linking should be here

		//barbatrucco per mancanza service linking...
		if (service != null) {
			this.myDataInstance.createServiceAccount(authenticatedUser, service);
			//in qualche modo qui si dovrebbe aggiornare la mappa con il jpanel giusto x il servizio aggiunto
		} else {
			//TODO WTF
			IService sp = new ServiceProva();
			this.myDataInstance.createServiceAccount(authenticatedUser, sp);
		}
	}

	@Override
	public void withdrawConsentForService(IService selectedService) {
		ConsentManager.changeServiceConsentStatusForService(authenticatedUser, selectedService, ConsentStatus.WITHDRAWN);
	}

	@Override
	public List<IService> getAllActiveServicesForUser() {
		List<IService> activeServices = new ArrayList<IService>();
		for (IAccount a : authenticatedUser.getAllAccounts())
			if (a.getActiveDisabledSC() != null)
				activeServices.add(a.getService());
		return activeServices;
	}

	@Override
	public boolean getADStatusForService(IService selectedService) {
		for (IAccount a : authenticatedUser.getAllAccounts())
			if (a.getService().equals(selectedService))
				return a.getActiveDisabledSC().getConsentStatus() == ConsentStatus.ACTIVE;
		throw new IllegalArgumentException("Current user does not have an Active or Disabled account to the " + selectedService.toString() + " service.");
	}

	@Override
	public void addNewServiceConsent(IService selectedService) {
		this.myDataInstance.issueNewServiceConsent(selectedService, authenticatedUser);
	}

	@Override
	public String getAllDConsents(IService selectedService) {
		StringBuilder sb = new StringBuilder();
		IAccount accountAtService = null;
		for (IAccount a : authenticatedUser.getAllAccounts()) {
			if (a.getService().equals(selectedService))
				accountAtService = a;
		}
		ServiceConsent sc = accountAtService.getActiveDisabledSC();
		if (sc != null)
			for (DataConsent dc : accountAtService.getAllDataConsents(sc))
				sb.append(dc.toString() + System.getProperty("line.separator"));
		return sb.toString();
	}
}