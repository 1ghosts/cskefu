/*
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2018- Jun. 2023 Chatopera Inc, <https://www.chatopera.com>,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Copyright (C) 2017 优客服-多渠道客服系统,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.cskefu.cc.util.metadata;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UKDatabaseMetadata{
	private final Connection connection ;
	public UKDatabaseMetadata(Connection connection)
			throws SQLException {
		this.connection = connection ;
        meta = connection.getMetaData();
	}
	
	

	private final List<UKTableMetaData> tables = new ArrayList<>();
	private DatabaseMetaData meta;
	public Properties properties ;
	private static final String[] TYPES = { "TABLE", "VIEW" };
	/**
	 * 
	 * @return
	 */
	public List<UKTableMetaData> getTables() {
		return this.tables;
	}
	/**
	 * 
	 * @param name
	 * @param schema
	 * @param catalog
	 * @param isQuoted
	 * @return
	 * @throws Exception
	 */
	public List<UKTableMetaData> loadTables(String name, String schema, String catalog,
			boolean isQuoted) throws Exception {
		boolean upcase = false ;
		try {
			if(properties!=null && properties.get("schema")!=null && schema==null){
				schema = properties.get("upcase")!=null?((String)properties.get("schema")).toUpperCase():(String)properties.get("schema") ;
			}
			if(properties!=null && properties.get("upcase")!=null){
				upcase = properties.get("upcase")!=null &&  properties.get("upcase").toString().toLowerCase().equals("true");
			}
			UKTableMetaData table = null;
			Statement statement = null;
			ResultSet rs = null ;
			try {
				if ((isQuoted && meta.storesMixedCaseQuotedIdentifiers())) {
					rs = meta.getTables(catalog, schema, name, TYPES);
				} else if ((isQuoted && meta.storesUpperCaseIdentifiers() && meta.storesUpperCaseQuotedIdentifiers())
						|| (!isQuoted && meta.storesUpperCaseIdentifiers())) {
					rs = meta.getTables(StringUtils.upperCase(catalog),
							StringUtils.upperCase(schema), StringUtils.upperCase(name), TYPES);
				} else if ((isQuoted && meta.storesLowerCaseQuotedIdentifiers())
						|| (!isQuoted && meta.storesLowerCaseIdentifiers())) {
					rs = meta.getTables(StringUtils.lowerCase(catalog),
							StringUtils.lowerCase(schema), StringUtils.lowerCase(name), TYPES);
				}else if(schema!=null && schema.equals("hive")){
					statement = this.connection.createStatement() ;
					if(properties.get("database")!=null){
						statement.execute("USE "+properties.get("database")) ;
					}
					rs = statement.executeQuery("SHOW TABLES") ;
				} else {
					rs = meta.getTables(catalog, schema, name, TYPES);
				}

				while (rs.next()) {
					String tableName = null ;
					if(schema!=null && schema.equals("hive")){
						tableName = rs.getString("tab_name") ;
					}else{
						tableName = rs.getString("TABLE_NAME");
					}
					
					if(tableName.matches("[\\da-zA-Z_-\u4e00-\u9fa5]+")){
						table = new UKTableMetaData(rs, meta, true , upcase , false , schema);
						tables.add(table);
					}
				}

			}catch(Exception ex){
				ex.printStackTrace();
			} finally {
				if (rs != null){
					rs.close();
				}
				if(statement!=null){
					statement.close();
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		}
		return tables ;
	}
	/**
	 * 
	 * @param name
	 * @param schema
	 * @param catalog
	 * @param isQuoted
	 * @return
	 * @throws Exception
	 */
	public UKTableMetaData loadTable(String name, String schema, String catalog,
			boolean isQuoted) throws Exception {
		UKTableMetaData table = null;
		boolean upcase = false ;
		try {
			if(properties!=null && properties.get("schema")!=null && schema==null){
				schema = (String)properties.get("schema") ;
			}
			if(properties!=null && properties.get("upcase")!=null){
				upcase = properties.get("upcase")!=null &&  properties.get("upcase").toString().toLowerCase().equals("true");
			}
			ResultSet rs = null;
			try {
				if ((isQuoted && meta.storesMixedCaseQuotedIdentifiers())) {
					rs = meta.getTables(catalog, schema, name, TYPES);
				} else if ((isQuoted && meta.storesUpperCaseQuotedIdentifiers())
						|| (!isQuoted && meta.storesUpperCaseIdentifiers())) {
					rs = meta.getTables(StringUtils.upperCase(catalog),
							StringUtils.upperCase(schema), StringUtils.upperCase(name), TYPES);
				} else if ((isQuoted && meta.storesLowerCaseQuotedIdentifiers())
						|| (!isQuoted && meta.storesLowerCaseIdentifiers())) {
					rs = meta.getTables(StringUtils.lowerCase(catalog),
							StringUtils.lowerCase(schema), StringUtils.lowerCase(name), TYPES);
				} else {
					rs = meta.getTables(catalog, schema, name, TYPES);
				}

				while (rs.next()) {
					table = new UKTableMetaData(rs, meta, true , upcase , true , schema);
					break ;
				}

			} finally {
				if (rs != null)
					rs.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace() ;
			throw sqle;
		}
		return table ;
	}
	
	/**
	 * 
	 * @param name
	 * @param schema
	 * @param catalog
	 * @param isQuoted
	 * @return
	 * @throws Exception
	 */
	public UKTableMetaData loadSQL(Statement statement ,String datasql, String tableName, String schema, String catalog,
			boolean isQuoted) throws Exception {
		UKTableMetaData table = null;
		if(properties!=null && properties.get("schema")!=null){
			schema = (String)properties.get("schema") ;
		}
		try {
			if(properties!=null && properties.get("schema")!=null && schema==null){
				schema = (String)properties.get("schema") ;
			}
			ResultSet rs = statement.executeQuery(datasql);
			try {
				table = new UKTableMetaData(tableName , schema , catalog , rs.getMetaData() , true);
			}catch(Exception ex){
				ex.printStackTrace() ;
			} finally {
				rs.close() ;
			}
		} catch (SQLException sqle) {
//			sqle.printStackTrace();
			throw sqle;
		}
		return table ;
	}

}
