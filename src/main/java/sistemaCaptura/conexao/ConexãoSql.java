package sistemaCaptura.conexao;


import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ConexãoSql {
    private JdbcTemplate connection;

    public ConexãoSql() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://52.3.127.92:1433;" +
                "database=magister;" +
                "user=sa;" +
                "password=Magister123@;" +
                "trustServerCertificate=true;");
        dataSource.setUsername("sa");
        dataSource.setPassword("Magister123@");
        this.connection = new JdbcTemplate(dataSource);
    }

    public JdbcTemplate getConnection() {
        return this.connection;
    }
}
