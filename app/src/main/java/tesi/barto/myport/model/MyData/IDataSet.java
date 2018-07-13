package tesi.barto.myport.model.MyData;

/**
 * Created by Valentina on 24/08/2017.
 */

import java.util.Set;

public interface IDataSet {

    void put(String typeConst, Object obj);

    Object getObject(String typeConst);

    Set<String> getKeys();
}

