package tesi.barto.myport.model.users;

/**
 * Created by Valentina on 24/08/2017.
 */
import tesi.barto.myport.model.MyData.IPersonalDataVault;
import tesi.barto.myport.model.consents.DataConsent;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.security.ISecurityManager;
import tesi.barto.myport.model.services.IService;

import java.util.Date;
import java.util.List;

public interface IUser {

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    Date getDateOfBirth();

    void setDateOfBirth(Date dateOfBirth);

    String getEmailAddress();

    void setEmailAddress(String emailAddress);

    ISecurityManager getSecurityManager();

    int hashCode();

    //public LoginUport getUport();


    /**
     * This function compares two users on their emailAddress. As a design
     * choice, the emailAddress field must be unique.
     *
     * @param other
     * @return true if the email is the same, false otherwise.
     */
    boolean equals(Object other);

    /**
     * This function creates a new Account for the service specified only if the
     * user does not already have one. In order to do this, it asks the Consent
     * Manager to issue a new Service Consent, which is necessary to create a
     * new account.
     *
     * @param service
     *            The service the user wants to register at.
     * @throws IllegalArgumentException
     *             if the account already exists.
     */
    void newAccountAtService(IService service);

    List<IAccount> getAllAccounts();

    boolean checkIfPasswordEqual(char[] password);

    boolean hasAccountAtService(IService service);

    /**
     * This method returns the instance of ServiceConsent which has Active
     * Status and is registered for the specified service.
     *
     * @param service
     * @throws IllegalArgumentException
     *             if this user does not have an account at the specified
     *             service
     * @return the corresponding ServiceConsent if there is one with Active
     *         status, null otherwise.
     */
    ServiceConsent getActiveSCForService(IService service);

    void addDataConsent(DataConsent dataConsent, IService service);

    void addServiceConsent(IService selectedService);

    IPersonalDataVault getPersonalDataVault();

}
