package tesi.barto.myport.model.MyData;


import android.content.res.AssetManager;

import tesi.barto.myport.Uport.LoginUport;
import tesi.barto.myport.activities.MainActivity;
import tesi.barto.myport.model.registry.Metadata;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * Created by Valentina on 25/08/2017.
 */

public class PersonalDataVault implements IPersonalDataVault {
    String datoUno;
    int datoDue;
    LoginUport login;

    public PersonalDataVault() {
        this.datoUno = readDatoUno();
        this.datoDue = readDatoDue();
        this.login = readlogin();
    }

    private int readDatoDue() {
        String fileName = "DatiProva.txt";
        int result = 0;
        try {
            AssetManager assetManager = MainActivity.getAppContext().getResources().getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                if (line == null || line.length() == 0)
                    throw new IllegalArgumentException("line must be initialized");
                result = Integer.parseInt(line.split(" ")[0]);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String readDatoUno() {
        String fileName = "DatiProva.txt";
        String result = new String();
        try {
            AssetManager assetManager = MainActivity.getAppContext().getResources().getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                if (line == null || line.length() == 0)
                    throw new IllegalArgumentException("line must be initialized");
                result = line.split(" ")[1];
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private LoginUport readlogin(){
        String fileName = "Account.json";
        LoginUport result=null;
        try{ //thanks to StackOverflow
            AssetManager assetManager = MainActivity.getAppContext().getResources().getAssets();
            InputStream inputStream = assetManager.open(fileName);
            //to get the content of the file as String
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
            result= new LoginUport(MainActivity.getAppContext(),out.toString());

        } catch (Exception e) {
            e.printStackTrace();
            result=new LoginUport(MainActivity.getAppContext());
        }
        //if (result==null)
            //result=new LoginUport(MainActivity.getAppContext());
        return result;
    }

    @Override
    public IDataSet getData(Set<String> typesConst) {  //perchè valentina aveva fatto return NULL ?! misteri del cazzo
        DataSet set = new DataSet();
        for (String typeConst : typesConst) {
            if (typeConst.equals(Metadata.DATOUNOPROVA_CONST))
                set.put(typeConst,this.datoUno);
            if (typeConst.equals(Metadata.DATODUEPROVA_CONST))
                set.put(typeConst,this.datoDue);
            if (typeConst.equals(Metadata.LOGINUPORT_CONST))
                set.put(typeConst,this.login);
        }
        return set;
    }

    @Override
    public void saveData(IDataSet dataSet) {
        for (String typeConst : dataSet.getKeys()) {
            if (typeConst.equals(Metadata.DATOUNOPROVA_CONST))
                this.setDatoUno((String) dataSet.getObject(typeConst));
            if (typeConst.equals(Metadata.DATODUEPROVA_CONST))
                this.setDatoDue((int) dataSet.getObject(typeConst));
            if (typeConst.equals(Metadata.LOGINUPORT_CONST))
                this.setUportLogin((LoginUport) dataSet.getObject(typeConst));
        }
    }

    public void setDatoUno(String datoUno) {
        this.datoUno = datoUno;
    }

    public void setDatoDue(int datoDue) {
        this.datoDue = datoDue;
    }

    public void setUportLogin(LoginUport uportL) {this.login = uportL;}
}
