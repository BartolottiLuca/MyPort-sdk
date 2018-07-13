package tesi.barto.myport.model.MyData;

/**
 * Created by Valentina on 24/08/2017.
 */

import java.util.Set;

public interface IPersonalDataVault {

    IDataSet getData(Set<String> typesConst);

    void saveData(IDataSet dataSet);
}

