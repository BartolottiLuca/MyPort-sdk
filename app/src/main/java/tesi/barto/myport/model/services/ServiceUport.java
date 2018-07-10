package tesi.barto.myport.model.services;


import java.util.HashSet;
import java.util.Set;

import me.uport.sdk.identity.Account;
import tesi.barto.myport.Uport.LoginUport;
import tesi.barto.myport.activities.MainActivity;
import tesi.barto.myport.model.MyData.IDataSet;
import tesi.barto.myport.model.registry.Metadata;
import tesi.barto.myport.model.registry.ServiceRegistry;

public class ServiceUport extends AbstractService {

    private final String name = "Uport_Service";
    private final Set<String> identifiers = new HashSet<String>();

    public ServiceUport() {
        super();
        this.registerService();
    }

    @Override
    protected Object concreteService(IDataSet dataSet){ //return LoginUport instance

        LoginUport login;
        try{
            login= (LoginUport) dataSet.getObject(Metadata.LOGINUPORT_CONST);
        }catch (Exception e){  //dovrei farlo solo su illegal argument exception
            e.printStackTrace();
            return new LoginUport(MainActivity.getAppContext()); //TODO sono entrato qua. perchè? la chiave è giusta, ll'oggetto è nel data set e dovrebbe essere tutto ok
        }
        return login;
    }

    @Override
    protected void registerService() {
        identifiers.add(Metadata.LOGINUPORT_CONST);
        ServiceRegistry.registerService(this, identifiers);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ServiceUport other = (ServiceUport) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
