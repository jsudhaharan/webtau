/*
 * Copyright 2020 webtau maintainers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.testingisdocumenting.webtau.db;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.testingisdocumenting.webtau.data.table.TableData;
import org.testingisdocumenting.webtau.data.table.header.TableDataHeader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class QueryRunnerUtils {
    static TableData runQuery(DataSource dataSource, String query) {
        QueryRunner run = new QueryRunner(dataSource);
        MapListHandler handler = new MapListHandler();

        try {
            List<Map<String, Object>> result = run.query(query, handler);
            if (result.isEmpty()) {
                return new TableData(Collections.emptyList());
            }

            List<String> columns = result.get(0).keySet().stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            TableDataHeader header = new TableDataHeader(columns.stream());
            TableData tableData = new TableData(header);
            result.forEach(row -> tableData.addRow(row.values().stream()));

            return tableData;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static int runUpdate(DataSource dataSource, String query) {
        QueryRunner run = new QueryRunner(dataSource);

        try {
            return run.update(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}