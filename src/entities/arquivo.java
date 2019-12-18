package entities;

public class arquivo {
	private String xml;
	private String chave;

	public arquivo() {

	}

	public arquivo(String xml, String chave) {
		this.xml = xml;
		this.chave = chave;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}
	
	
}
