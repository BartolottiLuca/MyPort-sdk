package tesi.barto.myport.model.services;

import tesi.barto.myport.model.MyData.IDataSet;
import tesi.barto.myport.model.registry.Metadata;
import tesi.barto.myport.model.registry.ServiceRegistry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Valentina on 29/08/2017.
 */

public class ServiceProva extends AbstractService {
	private final String name = "Servizio di prova";
	private final Set<String> identifiers = new HashSet<String>();

	public ServiceProva() {
		super();
		this.registerService();
	}

	// ??????????????
	@Override
	protected Object concreteService(IDataSet dataSet) {
		return null;
	}

	/**
	 * In this function, each concrete service specifies which of the allowed
	 * types it is going to use. The Service Registry must be informed at the
	 * end of this process.
	 */
	@Override
	protected void registerService() {
		identifiers.add(Metadata.DATOUNOPROVA_CONST);
		identifiers.add(Metadata.DATODUEPROVA_CONST);
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
		ServiceProva other = (ServiceProva) obj;
		if (name == null) {
            return other.name == null;
		} else return name.equals(other.name);
    }

	@Override
	public String toString() {
		return this.name;
	}
}
