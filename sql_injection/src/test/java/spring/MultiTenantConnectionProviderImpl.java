package spring;

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class MultiTenantConnectionProviderImpl extends AbstractMultiTenantConnectionProvider {

    private static final long serialVersionUID = 7880791041378207899L;
    
    private static final MasterService MASTER_SERVICE = new MasterService();
    
    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return MASTER_SERVICE.getDataSourceHashMap().get("h2");
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenant) {
        return MASTER_SERVICE.getDataSourceHashMap().get(tenant);
    }

}