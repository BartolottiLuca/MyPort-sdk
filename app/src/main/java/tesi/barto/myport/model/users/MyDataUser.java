package tesi.barto.myport.model.users;

/**
 * Created by Valentina on 24/08/2017.
 */

import android.content.Context;

import tesi.barto.myport.Uport.LoginUport;
import tesi.barto.myport.model.MyData.IPersonalDataVault;
import tesi.barto.myport.model.MyData.PersonalDataVault;
import tesi.barto.myport.model.consents.ConsentManager;
import tesi.barto.myport.model.consents.ConsentStatus;
import tesi.barto.myport.model.consents.DataConsent;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.security.ISecurityManager;
import tesi.barto.myport.model.services.IService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This class describes a MyData user. Its fields follow a generic guideline
 * specified in the "MyData Stack" specification. Other than personal data
 * fields (as first name, last name, etc.), every user must have a
 * SecurityManager in order to manage his KeyPair and perform sign and verify
 * operations.
 *
 * Each MyData user may have one account for each Service. Accounts can't be
 * accessed outside this class.
 *
 * @author Giada
 *
 */

public class MyDataUser implements IUser {

    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String emailAddress;
    private char[] password;
    private ISecurityManager securityManager;
    private List<IAccount> accounts;
    private IPersonalDataVault personalDataVault;
    private String uportToken;
    private String uportAddress;
    private String uportNetwork;

    public MyDataUser(String firstName, String lastName, Date dateOfBirth, String emailAddress, char[] password, Context context) {
        super();
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() || dateOfBirth == null
                || emailAddress == null || emailAddress.isEmpty() || password == null || password.length == 0)
            throw new IllegalArgumentException("Parameters must not be null or empty.");
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.emailAddress = emailAddress;
        this.password = password;
        this.securityManager = new tesi.barto.myport.model.security.SecurityManager();
        this.accounts = new ArrayList<IAccount>();
        this.personalDataVault = new PersonalDataVault();
        LoginUport uport= new LoginUport(context);
        this.uportToken= uport.getToken();
        this.uportAddress= uport.getAddressAccount();
        this.uportNetwork=uport.getNetwork();
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    @Override
    public IPersonalDataVault getPersonalDataVault() {
        return personalDataVault;
    }

    @Override
    public List<IAccount> getAllAccounts() {
        return this.accounts;
    }

    @Override
    public String getUportToken(){ return this.uportToken; }

    @Override
    public String getUportAddress() { return this.uportAddress; }

    @Override
    public String getUportNetwork() {return this.uportNetwork; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyDataUser other = (MyDataUser) obj;
        if (emailAddress == null) {
            if (other.emailAddress != null)
                return false;
        } else if (!emailAddress.equals(other.emailAddress))
            return false;
        return true;
    }

    @Override
    public void newAccountAtService(IService service) {
        if (this.hasAccountAtService(service))
            throw new IllegalArgumentException(
                    "User " + this.toString() + " already has an account at service " + service.toString() + ".");
		Account toAdd = new Account(service, ConsentManager.askServiceConsent(this, service));
        boolean add = this.accounts.add(toAdd);
		// ritorna FALSO quando provo ad aggiungere un nuovo account dopo un account revocato, perché..?
		// TODO controlla logica: posso aggiungere un nuovo account dopo uno revocato? Secondo me sì
    }

    @Override
    public boolean checkIfPasswordEqual(char[] givenPsw) {
        return Arrays.equals(this.password, givenPsw);
    }

    public String toString() {
        return this.emailAddress;
    }

    @Override
    public boolean hasAccountAtService(IService service) {
        for (IAccount a : this.accounts)
            if (a.getService().equals(service) && a.getActiveDisabledSC() != null)
                return true;
        return false;
    }

    @Override
    public ServiceConsent getActiveSCForService(IService service) {
        if (!this.hasAccountAtService(service))
            throw new IllegalArgumentException(
                    "User " + this.toString() + " does not have an account at " + service.toString() + ".");
        for (IAccount a : this.accounts)
            if (a.getService().equals(service) && a.getActiveDisabledSC() != null)
                if (a.getActiveDisabledSC().getConsentStatus() == ConsentStatus.ACTIVE)
                    return a.getActiveDisabledSC();
        return null;
    }

    @Override
    public void addDataConsent(DataConsent dataConsent, IService service) {
        for (IAccount a : this.accounts)
            if (a.getService().equals(service) && a.getActiveDisabledSC() != null)
                a.addDataConsent(dataConsent);
    }

    @Override
    public void addServiceConsent(IService selectedService) {
        if (!this.hasAccountAtService(selectedService))
            throw new IllegalArgumentException(
                    "User " + this.toString() + " does not have an account at service " + selectedService.toString());
        for (IAccount a : this.accounts)
            if (a.getService().equals(selectedService))
                a.addServiceConsent(ConsentManager.askServiceConsent(this, selectedService));
    }
}

