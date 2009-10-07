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
	 * Realiza a leitura do arquivo XML informado como parâmetro, faz as modificações,
	 * e grava o arquivo informado como parâmetro
	 * @param arquivoLeitura arquivo XML de entrada
	 * @param arquivoSaida arquivo XMl de saída 
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
				
				//como cada elemento do NodeList é um nó, precisamos fazer o cast
				Element elementoContato = (Element) listaContatos.item(i);
				
				//cria um objeto Contato com as informações do elemento contato
				Contato contato = criaContato(elementoContato);
				System.out.println(contato);
				
				//remove o elemento do XML se contato já está gravado
				//caso contrário, marca como gravado
				if (contato.isGravado()){
					raiz.removeChild(elementoContato);
					i--; //atualiza, já que alterou o lenght
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
	 * Obtém o valor do Text Node de um determinado elemento (se este 
	 * possuir um valor)
	 * @param elemento objeto que deseja-se obter o valor
	 * @param nomeElemento nome da tag cujo valor deseja-se obter
	 * @return valor da tag/elemento se esta existir ou null caso não exista
	 */
	public static String obterValorElemento(Element elemento, String nomeElemento){
		//obtém a lista de elementos
		NodeList listaElemento = elemento.getElementsByTagName(nomeElemento);
		if (listaElemento == null){
			return null;
		}
		//obtém o elemento
		Element noElemento = (Element) listaElemento.item(0);
		if (noElemento == null){
			return null;
		}
		//obtém o nó com a informação
		Node no = noElemento.getFirstChild();
		return no.getNodeValue();
	}
	
	/**
	 * Cria um objeto Contato a partir das informações obtidas
	 * no arquivo XML
	 * @param elemento que se dejesa extrair o conteúdo
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
	 * Cria um objeto Element a partir das informações
	 * fornecidas pelo objeto Contato
	 * @param contato objeto que vão ser extraídas as informações
	 * @return elemento contato pronto para ser inserido na árvore DOM
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
	 * Cria um elemento genérico do tipo [elemento]valor[/elemento]
	 * @param nomeElemento nome do elemento a ser criado
	 * @param valorElemento valor do TextNode do elemento a ser criado
	 * @return elemento genérico criado
	 */
	public Element criaElementoGenerico(String nomeElemento, String valorElemento){
		//cria o elemento com o nome do parâmetro
		Element elementoGenerico = doc.createElement(nomeElemento);
		Text valorElementoGenerico = doc.createTextNode(valorElemento);
		//adiciona o valorElemento ao elemento:
		elementoGenerico.appendChild(valorElementoGenerico);
		
		return elementoGenerico;
	}
	
	/**
	 * Cria um contato com alguma informação ficítica para teste
	 * @return contato
	 */
	private Contato criaContatoFicticio(){
		Contato contato = new Contato();
		contato.setId(5);
		contato.setGravado(true);
		contato.setNome("Tiago da Silva");
		contato.setEndereco("Endereço qualquer");
		contato.setTelefone("27 - 65237894");
		contato.setEmail("tiago.silva@provedor.com.br");
		return contato;
	}
	
	public static void main(String[] args){
		try {
			EditandoXMLDOM editingXMLDOM = new EditandoXMLDOM();
			editingXMLDOM.parse("contato.xml", "contato_modificado.xml");
		} catch (ParserConfigurationException e) {
			System.out.println("O parser não foi configurado corretamente.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("Problema ao fazer o parse do arquivo.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("O arquivo não pode ser lido.");
			e.printStackTrace();
		} catch (TransformerException e) {
			System.out.println("Problema ao fazer a serialização do arquivo.");
			e.printStackTrace();
		}
	}

}
