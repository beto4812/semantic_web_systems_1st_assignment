package universityOfEdinburg.semanticWebSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;

public class MyParser {

	Map<String, Integer> boroughGeoNameId = new HashMap<>();
	Map<String, String> namespaces = new HashMap<>();
	String religions[] = { "Christian", "Buddhist", "Hindu", "Jewish", "Muslim", "Sikh", "Other", "Atheism",
			"" };
	GeoNamesClient c = new GeoNamesClient();
	Model m = ModelFactory.createDefaultModel();
	int rowId = 0;
	int popId = 0;
	PrintWriter writer = null;
	Resource temp;

	public static void main(String[] args) {

		new MyParser().parseCSV();

	}

	public void init() {
		namespaces.put("gn", "http://sws.geonames.org/");
		namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		namespaces.put("foaf", "http://xmlns.com/foaf/spec/");
		namespaces.put("my", "http://vocab.inf.ed.ac.uk/sws/s1568644/");
		namespaces.put("db", "http://dbpedia.org/resource/");
		namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema#");
	}

	public MyParser() {
		init();
	}

	public void parseCSV() {

		try {
			writer = new PrintWriter("religion-ward-2001.ttl", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		prefixes();
		//myDefinitions();
		mainContent();

		//RDFWriter rdfW = m.getWriter();
		//m.write(writer, "RDF/XML-ABBREV");
		m.write(writer, "TURTLE");
		writer.close();
	}

	public void prefixes() {

		Set<String> it = namespaces.keySet();
		for (String temp : it) {
			//writer.println("@prefix " + temp + ": " + namespaces.get(temp));
			System.out.println("temp: "+temp+" namespaces.get(temp): "+namespaces.get(temp));
			m.setNsPrefix(temp, namespaces.get(temp));
		}
	}

	public void myDefinitions() {
		// Area Code
		temp = m.createResource(namespaces.get("my")+"areaCode");
		temp.addProperty(m.createProperty(namespaces.get("rdf")+"type"), m.createProperty(namespaces.get("rdfs")+"Property"));
		temp.addProperty(m.createProperty(namespaces.get("rdfs")+"label"), "London area code of a feature");
		temp.addProperty(m.createProperty(namespaces.get("rdfs")+"domain"), m.createProperty(namespaces.get("gn")+"Feature"));

		// Population Percentage
		temp = m.createResource(namespaces.get("my")+"populationPercentage");
		temp.addProperty(m.createProperty(namespaces.get("rdf")+"type"), namespaces.get("rdfs")+"Property");
		temp.addProperty(m.createProperty(namespaces.get("rdfs")+"label"), "Population percentage of a group");
		temp.addProperty(m.createProperty(namespaces.get("rdfs")+"domain"), namespaces.get("foaf")+"Group");
	}

	/**
	 * 
	 */
	public void mainContent() {
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader("religion-ward-2001.csv"));
			//line = br.readLine();
			//line = br.readLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean firstLine = true;

		while (true) {

			rowId++;

			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (line == null) {
				break;
			}

			System.out.println(line);

			String parts[] = line.split(",");

			if (!firstLine) {
				System.out.println("parts[1]: " + parts[1]);
				if (boroughGeoNameId.get(parts[1]) == null) {
					boroughGeoNameId.put(parts[1], c.getBestMatch(parts[1]));
					if (boroughGeoNameId.get(parts[1]) != -1) {
						temp = m.createResource(namespaces.get("gn")+ boroughGeoNameId.get(parts[1]));
						temp.addProperty(m.createProperty(namespaces.get("rdfs")+"label"), m.createTypedLiteral(parts[1]));
					}
				}

				temp = m.createResource(namespaces.get("my")+"A" + rowId);

				temp.addProperty(m.createProperty(namespaces.get("rdfs")+"label"), m.createTypedLiteral(parts[2]));
				temp.addProperty(m.createProperty(namespaces.get("rdf")+"type"), m.createResource(namespaces.get("gn")+"Feature"));
				temp.addProperty(m.createProperty(namespaces.get("my")+"areaCode"), m.createTypedLiteral(parts[0]));

				for (int i = 0; i <= 8; i++) {
					createPopulationResource(i, parts[4 + i], parts[13 + i]);
					temp.addProperty(m.createProperty(namespaces.get("foaf")+"Group"), m.createResource(namespaces.get("my")+"P" + popId));
				}
				if (boroughGeoNameId.get(parts[1]) != -1) {
					temp.addProperty(m.createProperty(namespaces.get("gn")+"parentFeature"),
							m.createResource(namespaces.get("gn")+boroughGeoNameId.get(parts[1])));
				}

				temp.addProperty(m.createProperty(namespaces.get("gn")+"population"), m.createTypedLiteral(Integer.parseInt(parts[3])));
			}
			firstLine = false;
		}
	}

	/**
	 * 
	 * @param population
	 * @param percentage
	 */
	public void createPopulationResource(int relIndex, String population, String percentage) {

		Resource pop = m.createResource();
		popId++;
		pop = m.createResource(namespaces.get("my")+"P" + popId);

		if (religions[relIndex] != "") {
			pop.addProperty(m.createProperty(namespaces.get("db")+"religion"), m.createResource(namespaces.get("db")+ religions[relIndex]));
		} else {
			pop.addProperty(m.createProperty(namespaces.get("db")+"religion"), "");
		}

		pop.addProperty(m.createProperty(namespaces.get("my")+"populationPercentage"), m.createTypedLiteral(Double.parseDouble(percentage)));
		pop.addProperty(m.createProperty(namespaces.get("gn")+"population"), m.createTypedLiteral(Integer.parseInt(population)));
	}

}
