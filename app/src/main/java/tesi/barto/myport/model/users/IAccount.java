package tesi.barto.myport.model.users;

/**
 * Created by Valentina on 24/08/2017.
 */

import tesi.barto.myport.model.consents.DataConsent;
import tesi.barto.myport.model.consents.ServiceConsent;
import tesi.barto.myport.model.services.IService;

import java.util.List;
import java.util.Set;

public interface IAccount {

    public IService getService();

    /**
     * There can be only one ServiceConsent Active or Disabled at a time for a
     * service. If there is no such consent, this method returns null
     *
     * @return The only Active or Disabled ServiceConsent for
     *         this service, null otherwise.
     */
    public ServiceConsent getActiveDisabledSC();

    public Set<ServiceConsent> getAllServiceConsents();

    public void addDataConsent(DataConsent dataConsent);

    public void addServiceConsent(ServiceConsent serviceConsent);

    public List<DataConsent> getAllDataConsents(ServiceConsent sc);
}
