/* 
 * Copyright (C) Creactiviti LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Arik Cohen <arik@creactiviti.com>, Apr 2017
 */
package com.creactiviti.piper.core.context;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.creactiviti.piper.json.JsonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Arik Cohe
 * @since Apt 7, 2017
 */
@Component
public class JdbcContextRepository implements ContextRepository<Context> {

  private JdbcTemplate jdbc;
  private ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  public void push(String aStackId, Context aContext) {
    jdbc.update("insert into context (id,stack_id,serialized_context,create_time) values (?,?,?,?)",aContext.getId(),aStackId,JsonHelper.writeValueAsString(objectMapper, aContext), new Date());
  }

//  @Override
//  @Transactional
//  public Context pop(String aStackId) {
//    Context context = peek(aStackId);
//    jdbc.update("delete from context where id = ?",context.getId());
//    return context;
//  }

  @Override
  public Context peek (String aStackId) {
    try {
      String sql = "select id,serialized_context from context where stack_id = ? order by create_time desc limit 1";
      return jdbc.queryForObject(sql,new Object[]{aStackId},this::contextRowMapper);
    }
    catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
  
  @Override
  public int stackSize(String aStackId) {
    String sql = "select count(*) from context where stack_id = ?";
    return jdbc.queryForObject(sql, Integer.class,aStackId);
  }
  
  @Override
  public List<Context> getStack (String aStackId) {
    String sql = "select id,serialized_context from context where stack_id = ? order by create_time desc";
    return jdbc.query(sql, this::contextRowMapper,aStackId);
  }
  
  private Context contextRowMapper (ResultSet aResultSet, int aIndex) throws SQLException {
    String serialized = aResultSet.getString(2);
    return new MapContext(JsonHelper.readValue(objectMapper, serialized, Map.class));    
  }

  public void setJdbcTemplate (JdbcTemplate aJdbcTemplate) {
    jdbc = aJdbcTemplate;
  }
  
  public void setObjectMapper(ObjectMapper aObjectMapper) {
    objectMapper = aObjectMapper;
  }

}
