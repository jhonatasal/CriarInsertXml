package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Program {

	public static void main(String[] args) throws IOException {
		FileReader chaves = (new FileReader("C:\\temp\\Chaves"));
		Scanner sc = new Scanner(chaves);
		File diretorio = new File("C:\\temp\\MG");
		File[] listFile = diretorio.listFiles();

		// File[] listDiretorios = diretorio.listFiles();
		FileWriter txtInsert = new FileWriter("C:\\temp\\inserts.sql");

		String line = sc.nextLine();
		String chaveArquivo[] = line.split(",");

		ArrayList<String> listaDeArquivosString = new ArrayList<>();
		ArrayList<String> listaDeDiretorioString = new ArrayList<>();

		for (int i = 0; i < listFile.length; i++) {
			listaDeDiretorioString.add(listFile[i].toString());
		}

		// for (int i = 0; i < listDiretorios.length; i++) {
		// listaDeDiretorioString.add(listDiretorios[i].toString());
		// }
		for (int i = 0; i < listaDeDiretorioString.size(); i++) {
			File diretorioEstado = new File(listaDeDiretorioString.get(i));
			File[] listaArquivos = diretorioEstado.listFiles();
			System.out.println(diretorioEstado);
			String name = "2019".concat("09").concat(diretorioEstado.getName().toString()).concat(".zip");
			File zip = new File(
					"C:\\temp\\MG\\".concat(diretorioEstado.getName().toString()).concat("\\").concat(name));

			if (zip.exists()) {
				processarEnvioArquivoZip(diretorioEstado, zip);
			}
			for (int j = 0; j < listaArquivos.length; j++) {
				if (listaArquivos[j].toString().endsWith("BPeRecepcao_E.xml")) {
					// System.out.println(listaArquivos[j].toString());
					listaDeArquivosString.add(listaArquivos[j].toString());
				}
			}
		}

		boolean ba = false;
		int qtdChaveEncontradas = 0;
		int qtdChaveNaoEncontradas = 0;
		for (int i = 0; i < chaveArquivo.length; i++) {
			for (int j = 0; j < listaDeArquivosString.size(); j++) {
				if (listaDeArquivosString.get(j).contains(chaveArquivo[i])
						&& listaDeArquivosString.get(j).endsWith("_BPeRecepcao_E.xml")) {
					String nomeArquivo = listaDeArquivosString.get(j);
					FileReader bf = (new FileReader(nomeArquivo));
					Scanner scc = new Scanner(bf);
					String xml = scc.nextLine();
					 txtInsert.write("insert into bpexml values('" + chaveArquivo[i] + "','" + xml
					 + "');\r\n");
//					txtInsert.write(
//							"update bpexml set bxdta = '" + xml + "'" + "where bxkey='" + chaveArquivo[i] + "';\r\n");
					ba = true;
				} else {
//					String nomeArquivo = listaDeArquivosString.get(j);
//					File bf = (new File(nomeArquivo));
//					bf.delete();
//					listaDeArquivosString.remove(nomeArquivo);
				}
			}
			if (ba == true) {
				qtdChaveEncontradas++;
			} else {
				qtdChaveNaoEncontradas++;
			}
		}
		txtInsert.close();
		System.out.println("Quantidade de Chaves encontradas: " + qtdChaveEncontradas);
		System.out.println("Quantidade de Chaves não encontradas: " + qtdChaveNaoEncontradas);
	}

	private static void processarEnvioArquivoZip(File diretorioUnzip, File arquivo) {
		try {
			byte[] buffer = new byte[1024];
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
