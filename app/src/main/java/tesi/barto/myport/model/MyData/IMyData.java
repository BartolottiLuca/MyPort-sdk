package tesi.barto.myport.model.MyData;

/**
 * Created by Valentina on 25/08/2017.
 */


import android.content.Context;

import tesi.barto.myport.model.consents.InputDataConsent;
import tesi.barto.myport.model.consents.OutputDataConsent;
import tesi.barto.myport.model.services.IService;
import tesi.barto.myport.model.users.IUser;

import java.util.Date;

public interface IMyData {

    IUser loginUser(String email, char[] password);

    IUser createMyDataAccount(String firstName, String lastName, Date dateOfBirth, String emailAddress,
                              char[] password, Context context);

    IDataSet getDataSetForOutputDataConsent(OutputDataConsent outputDataConsent);

    void saveDataSet(IDataSet dataSet, InputDataConsent inDataConsent);

    void issueNewServiceConsent(IService selectedService, IUser authenticatedUser);

    void createServiceAccount(IUser user, IService service);

}

