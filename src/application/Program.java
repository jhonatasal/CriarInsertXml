package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import conexao.conecta;

public class Program {

	public static void main(String[] args) throws IOException, SQLException {
		
		conecta con = new conecta();
		String ip = "";
		String vta = "";
		LocalDate data = LocalDate.now().plusDays(-1);
		System.out.println(data);
		String usuario = "";
		String senha = "";
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT B3KEY FROM BOLTO3 ");
		sb.append("WHERE B3KEY NOT IN (SELECT BXKEY FROM BPEXML WHERE BXKEY = B3KEY) ");
//		sb.append("SELECT B3KEY,B3KEYC,B3DA FROM BOLTO3 WHERE B3KEY = '' AND B3KEYC <> ''  AND B3DA >= 191024 AND B3DA <=191031 ");
		sb.append("AND B3DA =191030");
		PreparedStatement pstm = con.conecta(ip, vta, usuario, senha).prepareStatement(sb.toString());
		ResultSet rs = pstm.executeQuery();
		ArrayList<String> chaveArquivo = new ArrayList<>();

		while (rs.next()) {
			chaveArquivo.add(rs.getString("b3key"));
		}
		con.fecharConexao();
		rs.close();
		pstm.close();


		ArrayList<String> listaDeArquivosString = new ArrayList<>();
		ArrayList<String> listaDeDiretorioString = new ArrayList<>();
		ArrayList<String> listaDeCNPJString = new ArrayList<>();
		ArrayList<String> ChaveJaUsada = new ArrayList<>();
		File CNPJ = new File("C:\\temp\\CNPJ");

		File[] listCNPJ = CNPJ.listFiles();
		preencherListaDeCaminhosdeDiretorios(listaDeDiretorioString, listaDeCNPJString, listCNPJ);

//			deletarArquivos(listaDeDiretorioString);
		preencherListaDeCaminhosDeArquivos(listaDeArquivosString, listaDeDiretorioString);

		boolean ba = false;
        int contadorQuery =0;
        int qtdChaveEncontradas = 0;
        int qtdChaveNaoEncontradas = 0;
        ArrayList<String> ListaDeInserts = new ArrayList<>();
        StringBuilder queryInsert = new StringBuilder();
        for (int i = 0; i < chaveArquivo.size(); i++) {
            for (int j = 0; j < listaDeArquivosString.size(); j++) {
                if (listaDeArquivosString.get(j).contains(chaveArquivo.get(i)) && listaDeArquivosString.get(j).endsWith("_BPeRecepcao_E.xml") && !ChaveJaUsada.contains(chaveArquivo.get(i))) {
                    boolean insertSucess = false;
                    int contador = 0;
                    while (insertSucess == false && contador <= 4) {
                        contador++;
                        try {
                            String nomeArquivo = listaDeArquivosString.get(j);
                            FileReader bf = (new FileReader(nomeArquivo));
                            Scanner scc = new Scanner(bf);
                            StringBuilder sbba = new StringBuilder();
                            while(scc.hasNext()) {
                            	sbba.append(scc.nextLine());
                            }
							if (!sbba.toString().isEmpty()) {
                            String xml = sbba.toString();
                             if(contadorQuery == 0) {
                            queryInsert.append("insert into bpexml values('" + chaveArquivo.get(i) + "','" + replaceXML(xml)+ "')");
                            contadorQuery++;
                            }else if(contadorQuery > 0 && contadorQuery < 1) {
                                queryInsert.append(",('" + chaveArquivo.get(i) + "','" + replaceXML(xml)+ "')");    
                                contadorQuery++;
                            }  if(contadorQuery == 1) {
                                ListaDeInserts.add(queryInsert.toString());
                                contadorQuery = 0;
                                queryInsert.setLength(0);
                            }
                            
                            System.out.println("Chave " + chaveArquivo.get(i) + " encontrada!");
                            ba = true;
                            ChaveJaUsada.add(chaveArquivo.get(i));
                            scc.close();
                            }
                            insertSucess = true;
                        } catch (Exception e) {
                            System.out.println("Houve um erro no processo de insert!");
                            System.out.println("Tentando novamente!");
                        }
                    }
//                    listaDeArquivosString.remove(j);
//                    chaveArquivo.remove(i);
                } else {
                }
            }
            if (ba == true) {
                qtdChaveEncontradas++;
            } else {
                qtdChaveNaoEncontradas++;
            }
        }
        conecta conexao2 = new conecta();
        for (String query : ListaDeInserts) {
        	 PreparedStatement insert = conexao2.conecta(ip, vta, usuario, senha).prepareStatement(query);
             insert.executeUpdate();
             System.out.println(query);
        }
        conexao2.fecharConexao();
        //deletarArquivos(listaDeDiretorioString);
        System.out.println("Quantidade de Chaves encontradas: " + qtdChaveEncontradas);
        System.out.println("Quantidade de Chaves nao encontradas: " + qtdChaveNaoEncontradas);
    }

	private static String replaceXML(String xml) {
		return xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "").replace("<tbpe>", "").replace("</tbpe>", "").replace("<bPe>", "<BPe xmlns=\"http://www.portalfiscal.inf.br/bpe\">").replace("</bPe>", "</BPe>").replace("<vBP>", " <vBP>").replace("</vBP>", "</vBP>");
	}

	private static void preencherListaDeCaminhosDeArquivos(ArrayList<String> listaDeArquivosString,	ArrayList<String> listaDeDiretorioString) {
		
		for (int i = 0; i < listaDeDiretorioString.size(); i++) {
			File diretorioEstado = new File(listaDeDiretorioString.get(i));
		
			String name = "2019".concat(diretorioEstado.toString().substring(36, 38)).concat(diretorioEstado.getName().toString()).concat(".zip");
			File zip = new File(diretorioEstado.toString().concat("\\").concat(name));

//			if (zip.exists() ) {
//				System.out.println("Dezipando arquivo : " + zip.getName());
//				processarEnvioArquivoZip(diretorioEstado, zip);
//			
//			}
			
			File[] listaArquivos = diretorioEstado.listFiles();

			for (int j = 0; j < listaArquivos.length; j++) {
				if (listaArquivos[j].toString().endsWith("BPeRecepcao_E.xml")) {
					System.out.println(listaArquivos[j].toString());
					listaDeArquivosString.add(listaArquivos[j].toString());
				}
			}
		}
	}

	private static void preencherListaDeCaminhosdeDiretorios(ArrayList<String> listaDeDiretorioString, ArrayList<String> listaDeCNPJString, File[] listCNPJ) {
		
		for (int j = 0; j < listCNPJ.length; j++) {
			
			listaDeCNPJString.add(listCNPJ[j].toString());
			
			
			File diretorio = new File(listaDeCNPJString.get(j));
			File[] listFile = diretorio.listFiles();
			
			for (int i = 0; i < listFile.length; i++) {

				File data = new File(listFile[i].toString().concat("\\2019"));
				
				File[] listData = data.listFiles();
				
				if (listData != null) {
					
					for (int k = 0; k < listData.length; k++) {
						
						File Datas = new File(listData[k].toString());
						
						File[] listDatas = Datas.listFiles();
						
						for (int z = 0; z < listDatas.length; z++) {
							listaDeDiretorioString.add(listDatas[z].toString());
						}
					}
				}

			}
		}
	}

	private static void deletarArquivos(ArrayList<String> listaDeDiretorioString) {
		for (int i = 0; i < listaDeDiretorioString.size(); i++) {
			File diretorioEstado = new File(listaDeDiretorioString.get(i));
			File[] listaArquivos = diretorioEstado.listFiles();
			for (int p = 0; p < listaArquivos.length; p++) {
				if (listaArquivos[p].toString().contains(".xml")) {
					System.out.println("Deletando arquivo: " + listaArquivos[p].toString());
					listaArquivos[p].delete();
				}
			}
		}
	}

	private static void processarEnvioArquivoZip(File diretorioUnzip, File arquivo) {
		try {
			byte[] buffer = new byte[4024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(arquivo));
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {
				File newFile = newFile(diretorioUnzip, zipEntry);
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
