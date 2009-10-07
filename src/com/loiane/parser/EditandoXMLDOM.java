package com.loiane.parser;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.loiane.agenda.Contato;

/**
 * Classe que faz o parser/leitura de um arquivo XML e o modifica
 * 
 * @author Loiane Groner
 *
 */
public class EditandoXMLDOM {
	
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document doc;
	
	public EditandoXMLDOM() throws ParserConfigurationException{
		//fazer o parse do arquivo e criar o documento XML
		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
	}
	
	/**
	 * Realiza a leitura do arquivo XML informado como par�metro, faz as modifica��es,
	 * e grava o arquivo informado como par�metro
	 * @param arquivoLeitura arquivo XML de entrada
	 * @param arquivoSaida arquivo XMl de sa�da 
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void parse(String arquivoLeitura, String arquivoSaida) throws SAXException, IOException, TransformerException{

			doc = db.parse(arquivoLeitura);

			//Passo 1: obter o elemento raiz
			Element raiz = doc.getDocumentElement();
			
			//Passo 2: localizar os elementos filhos da agenda
			NodeList listaContatos = raiz.getElementsByTagName("contato");

			//Passo 3: obter os elementos de cada elemento contato
			for (int i=0; i<listaContatos.getLength(); i++){
				
				//como cada elemento do NodeList � um n�, precisamos fazer o cast
				Element elementoContato = (Element) listaContatos.item(i);
				
				//cria um objeto Contato com as informa��es do elemento contato
				Contato contato = criaContato(elementoContato);
				System.out.println(contato);
				
				//remove o elemento do XML se contato j� est� gravado
				//caso contr�rio, marca como gravado
				if (contato.isGravado()){
					raiz.removeChild(elementoContato);
					i--; //atualiza, j� que alterou o lenght
				} else{
					elementoContato.setAttribute("gravado", "SIM");
				}
				
				//subtitui o telefone do Pedro da Silva
				if (contato.getId() == 3){
					Element tel = doc.createElement("telefone");
					tel.appendChild(doc.createTextNode("11 99999999"));
					Node telefone = elementoContato.getElementsByTagName("telefone").item(0);
					elementoContato.replaceChild(tel, telefone);
				}
				
			}
			
			//Cria um novo elemento
			Element novoContato = criaElementoAPartirContato(criaContatoFicticio());
			//adiciona um novo elemento no XML
			raiz.appendChild(novoContato);
			
			
			//grava o documento XML editado
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(arquivoSaida));
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform(source, result);

		}
	
	/**
	 * Obt�m o valor do Text Node de um determinado elemento (se este 
	 * possuir um valor)
	 * @param elemento objeto que deseja-se obter o valor
	 * @param nomeElemento nome da tag cujo valor deseja-se obter
	 * @return valor da tag/elemento se esta existir ou null caso n�o exista
	 */
	public static String obterValorElemento(Element elemento, String nomeElemento){
		//obt�m a lista de elementos
		NodeList listaElemento = elemento.getElementsByTagName(nomeElemento);
		if (listaElemento == null){
			return null;
		}
		//obt�m o elemento
		Element noElemento = (Element) listaElemento.item(0);
		if (noElemento == null){
			return null;
		}
		//obt�m o n� com a informa��o
		Node no = noElemento.getFirstChild();
		return no.getNodeValue();
	}
	
	/**
	 * Cria um objeto Contato a partir das informa��es obtidas
	 * no arquivo XML
	 * @param elemento que se dejesa extrair o conte�do
	 * @return Contato
	 */
	public Contato criaContato(Element elemento){
		Contato contato = new Contato();
		contato.setId(Integer.parseInt(elemento.getAttributeNode("id").getNodeValue()));
		contato.setGravado(elemento.getAttributeNode("gravado").getNodeValue());
		contato.setNome(obterValorElemento(elemento,"nome"));
		contato.setEndereco(obterValorElemento(elemento,"endereco"));
		contato.setTelefone(obterValorElemento(elemento,"telefone"));
		contato.setEmail(obterValorElemento(elemento,"email"));
		return contato;
	}
	
	/**
	 * Cria um objeto Element a partir das informa��es
	 * fornecidas pelo objeto Contato
	 * @param contato objeto que v�o ser extra�das as informa��es
	 * @return elemento contato pronto para ser inserido na �rvore DOM
	 */
	public Element criaElementoAPartirContato(Contato contato){
		//cria um elemento contato
		Element element = doc.createElement("contato");

		//cria o atributo id e gravado
		element.setAttribute("id", "04");
		element.setAttribute("gravado", "SIM");
		
		//cria os elementos
		Element nome = criaElementoGenerico("nome", contato.getNome());
		Element endereco = criaElementoGenerico("endereco", contato.getEndereco());
		Element telefone = criaElementoGenerico("telefone", contato.getTelefone());
		Element email = criaElementoGenerico("email", contato.getEmail());
		
		//adiciona os elementos ao contato
		element.appendChild(nome);
		element.appendChild(endereco);
		element.appendChild(telefone);
		element.appendChild(email);
		
		return element;
	}
	
	/**
	 * Cria um elemento gen�rico do tipo [elemento]valor[/elemento]
	 * @param nomeElemento nome do elemento a ser criado
	 * @param valorElemento valor do TextNode do elemento a ser criado
	 * @return elemento gen�rico criado
	 */
	public Element criaElementoGenerico(String nomeElemento, String valorElemento){
		//cria o elemento com o nome do par�metro
		Element elementoGenerico = doc.createElement(nomeElemento);
		Text valorElementoGenerico = doc.createTextNode(valorElemento);
		//adiciona o valorElemento ao elemento:
		elementoGenerico.appendChild(valorElementoGenerico);
		
		return elementoGenerico;
	}
	
	/**
	 * Cria um contato com alguma informa��o fic�tica para teste
	 * @return contato
	 */
	private Contato criaContatoFicticio(){
		Contato contato = new Contato();
		contato.setId(5);
		contato.setGravado(true);
		contato.setNome("Tiago da Silva");
		contato.setEndereco("Endere�o qualquer");
		contato.setTelefone("27 - 65237894");
		contato.setEmail("tiago.silva@provedor.com.br");
		return contato;
	}
	
	public static void main(String[] args){
		try {
			EditandoXMLDOM editingXMLDOM = new EditandoXMLDOM();
			editingXMLDOM.parse("contato.xml", "contato_modificado.xml");
		} catch (ParserConfigurationException e) {
			System.out.println("O parser n�o foi configurado corretamente.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("Problema ao fazer o parse do arquivo.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("O arquivo n�o pode ser lido.");
			e.printStackTrace();
		} catch (TransformerException e) {
			System.out.println("Problema ao fazer a serializa��o do arquivo.");
			e.printStackTrace();
		}
	}

}
