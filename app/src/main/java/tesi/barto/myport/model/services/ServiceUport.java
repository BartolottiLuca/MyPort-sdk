package tesi.barto.myport.model.services;


import java.util.HashSet;
import java.util.Set;

import tesi.barto.myport.Uport.UportData;
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
    protected Object concreteService(IDataSet dataSet){ //return UportData instance

        UportData account;
        try{
            account= (UportData) dataSet.getObject(Metadata.UPORTDATA_CONST);
        }catch (Exception e){  //dovrei farlo solo su illegal argument exception
            e.printStackTrace();
            return new UportData(MainActivity.getInstance());
        }
        return account;
    }

    @Override
    protected void registerService() {
        identifiers.add(Metadata.UPORTDATA_CONST);
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
            return other.name == null;
        } else return name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
