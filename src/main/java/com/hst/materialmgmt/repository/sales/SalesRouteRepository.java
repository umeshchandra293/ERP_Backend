package com.hst.materialmgmt.repository;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.hst.materialmgmt.entity.SalesRouteEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesRouteRowMapper;

@Repository
public class SalesRouteRepository extends ParentRepositoryImpl {

    @Autowired private SalesRouteRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_route_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) {
        return Map.of("route_id", id);
    }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesRouteEntity.class; }
}
