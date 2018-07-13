package tesi.barto.myport.model.services;

/**
 * Created by Valentina on 24/08/2017.
 */

import tesi.barto.myport.model.MyData.IDataSet;
import tesi.barto.myport.model.security.ISecurityManager;
import tesi.barto.myport.model.users.IUser;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This interface describes a generic service in the context of MyData. Each
 * service must have a unique name, to be used in comparisons with other
 * services. It must have a security manager to implement the sign and verify
 * methods. Since each service share some common operations, it would be
 * advisable for each new concrete service to implement the abstract class
 * AbstractService instead of this interface.
 *
 * @author Giada
 *
 */

public interface IService {
    ISecurityManager getSecurityManager();

    boolean equals(Object obj);

    Object provideService(IUser user) throws IOException;

    String toString();

    void gatherData(IUser user, IDataSet dataSet);
}
