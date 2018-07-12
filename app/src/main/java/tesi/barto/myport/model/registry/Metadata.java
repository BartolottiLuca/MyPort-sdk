package tesi.barto.myport.model.registry;

import tesi.barto.myport.Uport.UportData;

/**
 * Created by Valentina on 25/08/2017.
 */

public final class Metadata implements IMetadata {

    public static final String DATOUNOPROVA_CONST = String.class.getCanonicalName();
    public static final String DATODUEPROVA_CONST = int.class.getCanonicalName();
    public static final String UPORTDATA_CONST = UportData.class.getCanonicalName();

    private Metadata() {
    }
}

