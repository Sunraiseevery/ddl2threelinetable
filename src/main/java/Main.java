import com.alibaba.druid.pool.DruidDataSource;
import entity.Result;
import entity.TableDetail;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.jdbc.core.JdbcTemplate;
import util.ExportWord;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wzy
 * @date 2021/5/2
 */

public class Main {

    private static final String SQL = "SELECT c.table_schema, c.table_name, c.column_name, " +
            "c.column_type, c.column_key, c.is_nullable, c.column_default, c.column_comment, " +
            "c.character_set_name, c.EXTRA, " +
            "kcu.constraint_name AS foreign_key_name, " +
            "kcu.referenced_table_name AS referenced_table, " +
            "kcu.referenced_column_name AS referenced_column " +
            "FROM information_schema.columns AS c " +
            "LEFT JOIN information_schema.key_column_usage AS kcu " +
            "ON c.table_schema = kcu.table_schema " +
            "AND c.table_name = kcu.table_name " +
            "AND c.column_name = kcu.column_name " +
            "AND kcu.referenced_table_name IS NOT NULL " +
            "WHERE c.table_schema = ? " +
            "ORDER BY c.table_name, c.ORDINAL_POSITION;";

    public static void main(String[] args) throws Exception {
        ExportWord ew = new ExportWord();
        Main main = new Main();

        String databaseName = "gym_management";
        List<Result> results = main.getTableDetails(databaseName);
        XWPFDocument document = ew.createXWPFDocument(results);
        ew.exportCheckWord(results, document, "expWordTest.docx");
        System.out.println("文档生成成功");
    }

    private List<Result> getTableDetails(String databaseName) throws IOException {
        InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("dbinfo.properties");
        Properties pro = new Properties();
        pro.load(resourceAsStream);

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(pro.getProperty("url"));
        druidDataSource.setUsername(pro.getProperty("username"));
        druidDataSource.setPassword(pro.getProperty("pass"));
        druidDataSource.setDriverClassName(pro.getProperty("driver"));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(druidDataSource);
        final HashMap<String, Result> tableMap = new HashMap<>();

        return jdbcTemplate.query(SQL, (resultSet, rowNum) -> {
            String tableName = resultSet.getString("TABLE_NAME");
            Result result = tableMap.get(tableName);
            if (result == null) {
                result = new Result();
                tableMap.put(tableName, result);
                result.setTableSchema(resultSet.getString("TABLE_SCHEMA"));
                result.setTableName(tableName);
                List<TableDetail> tableDetails = result.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("COLUMN_DEFAULT"));
                tableDetail.setForeignKeyName(resultSet.getString("foreign_key_name"));
                tableDetail.setReferencedTable(resultSet.getString("referenced_table"));
                tableDetail.setReferencedColumn(resultSet.getString("referenced_column"));
                tableDetails.add(tableDetail);
                return result;
            } else {
                List<TableDetail> tableDetails = result.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("COLUMN_DEFAULT"));
                tableDetail.setForeignKeyName(resultSet.getString("foreign_key_name"));
                tableDetail.setReferencedTable(resultSet.getString("referenced_table"));
                tableDetail.setReferencedColumn(resultSet.getString("referenced_column"));
                tableDetails.add(tableDetail);

                return null;
            }
        }, databaseName).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}