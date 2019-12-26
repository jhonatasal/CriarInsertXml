package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class conecta {

	Connection conexao;
	int validacao = 1;

	public Connection conecta(String ip, String vta, String usuario, String senha) throws SQLException {

		if (this.validacao == 2) {
			if (!this.conexao.isClosed()) {
				System.out.println("Retornando conexão!");
				return this.conexao;
			} else {
				this.validacao = 1;
			}

		}  else if (this.validacao == 1) {
			System.out.println("Inicio ConexaoDB2");
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
			Connection conexao = DriverManager.getConnection("jdbc:as400://" + ip + "/" + vta, usuario, senha);
			this.conexao = conexao;
			this.validacao = 2;
			return conexao;
		}
		return this.conexao;
	}

	public void fecharConexao() throws SQLException {
		this.validacao = 1;
		conexao.close();

	}
}